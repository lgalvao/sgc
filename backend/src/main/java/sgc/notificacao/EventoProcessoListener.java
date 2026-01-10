package sgc.notificacao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.AlertaService;
import sgc.comum.erros.ErroEstadoImpossivel;
import sgc.organizacao.UsuarioService;
import sgc.organizacao.dto.ResponsavelDto;
import sgc.organizacao.dto.UsuarioDto;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.processo.eventos.EventoProcessoFinalizado;
import sgc.processo.eventos.EventoProcessoIniciado;
import sgc.processo.model.Processo;
import sgc.processo.service.ProcessoFacade;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static sgc.organizacao.model.TipoUnidade.*;

/**
 * Listener para eventos de processo.
 *
 * <p>Processa eventos de processo iniciado e finalizado, criando alertas e enviando e-mails para as unidades
 * participantes de forma diferenciada, conforme o tipo de unidade.
 *
 * <p><strong>Pré-requisito:</strong> As invariantes de dados organizacionais são validadas na inicialização
 * do sistema pelo {@link sgc.organizacao.ValidadorDadosOrganizacionais}. Este listener assume que os dados
 * são válidos (toda unidade tem titular, todo titular tem email, etc.).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventoProcessoListener {
    private final AlertaService servicoAlertas;
    private final NotificacaoEmailService notificacaoEmailService;
    private final NotificacaoModelosService notificacaoModelosService;
    private final UsuarioService usuarioService;
    private final ProcessoFacade processoFacade;
    private final SubprocessoService subprocessoService;

    /**
     * Escuta e processa o evento {@link EventoProcessoIniciado}, disparado quando um novo processo
     * de mapeamento ou revisão é iniciado.
     *
     * <p>Este método orquestra a criação de alertas e o envio de emails para todos os participantes
     * do processo. A lógica diferencia o conteúdo das notificações com base no tipo de unidade
     * (Operacional, Intermediária, etc.), garantindo que cada participante receba instruções
     * relevantes para sua função.
     *
     * @param evento O evento contendo os detalhes do processo que foi iniciado.
     */
    @EventListener
    @Transactional
    public void aoIniciarProcesso(EventoProcessoIniciado evento) {
        try {
            processarInicioProcesso(evento);
        } catch (Exception e) {
            log.error("Erro ao processar evento de processo iniciado: {}", e.getClass().getSimpleName(), e);
        }
    }

    /**
     * Escuta e processa o evento {@link EventoProcessoFinalizado}, disparado quando um processo
     * é concluído.
     *
     * @param evento O evento contendo os detalhes do processo que foi finalizado.
     */
    @EventListener
    @Transactional
    public void aoFinalizarProcesso(EventoProcessoFinalizado evento) {
        try {
            processarFinalizacaoProcesso(evento);
        } catch (Exception e) {
            log.error("Erro ao processar evento de processo finalizado: {}", e.getClass().getSimpleName(), e);
        }
    }

    private void processarInicioProcesso(EventoProcessoIniciado evento) {
        Processo processo = processoFacade.buscarEntidadePorId(evento.getCodProcesso());

        List<Subprocesso> subprocessos =
                subprocessoService.listarEntidadesPorProcesso(evento.getCodProcesso());

        if (subprocessos.isEmpty()) {
            log.warn("Nenhum subprocesso encontrado para o processo {}", evento.getCodProcesso());
            return;
        }

        // 1. Criar alertas diferenciados por tipo de unidade
        List<Unidade> unidadesParticipantes = subprocessos.stream()
                .map(Subprocesso::getUnidade)
                .toList();
        servicoAlertas.criarAlertasProcessoIniciado(processo, unidadesParticipantes);

        // 2. Pré-carregar responsáveis e usuários para evitar N+1
        List<Long> todosCodigosUnidades = subprocessos.stream()
                .map(s -> s.getUnidade().getCodigo())
                .toList();

        Map<Long, ResponsavelDto> responsaveis = usuarioService.buscarResponsaveisUnidades(todosCodigosUnidades);

        List<String> todosTitulos = new ArrayList<>();
        responsaveis.values().forEach(r -> {
            if (r.getTitularTitulo() != null) todosTitulos.add(r.getTitularTitulo());
            if (r.getSubstitutoTitulo() != null) todosTitulos.add(r.getSubstitutoTitulo());
        });

        Map<String, UsuarioDto> usuarios = usuarioService.buscarUsuariosPorTitulos(todosTitulos);

        // 3. Enviar e-mails para cada subprocesso
        for (Subprocesso subprocesso : subprocessos) {
            try {
                enviarEmailProcessoIniciado(processo, subprocesso, responsaveis, usuarios);
            } catch (Exception e) {
                log.error("Erro ao enviar e-mail referente a subprocesso {}: {}", subprocesso.getCodigo(), e.getClass().getSimpleName(), e);
            }
        }
    }

    private void processarFinalizacaoProcesso(EventoProcessoFinalizado evento) {
        Processo processo = processoFacade.buscarEntidadePorId(evento.getCodProcesso());

        List<Unidade> unidadesParticipantes = new ArrayList<>(processo.getParticipantes());

        if (unidadesParticipantes.isEmpty()) {
            log.warn("Nenhuma unidade participante encontrada para notificar ao finalizar processo {}", processo.getCodigo());
            return;
        }

        List<Long> todosCodigosUnidades = unidadesParticipantes.stream().map(Unidade::getCodigo).toList();
        Map<Long, ResponsavelDto> responsaveis = usuarioService.buscarResponsaveisUnidades(todosCodigosUnidades);

        Map<String, UsuarioDto> usuarios = usuarioService.buscarUsuariosPorTitulos(responsaveis.values().stream()
                .map(ResponsavelDto::getTitularTitulo)
                .distinct()
                .toList());

        // Filtrar subordinadas uma vez para uso nas intermediárias
        List<Unidade> todasSubordinadas = unidadesParticipantes.stream()
                .filter(u -> u.getUnidadeSuperior() != null)
                .toList();

        for (Unidade unidade : unidadesParticipantes) {
            try {
                ResponsavelDto responsavel = responsaveis.get(unidade.getCodigo());
                UsuarioDto titular = usuarios.get(responsavel.getTitularTitulo());
                String emailTitular = titular.getEmail();

                TipoUnidade tipoUnidade = unidade.getTipo();
                if (tipoUnidade == OPERACIONAL || tipoUnidade == INTEROPERACIONAL) {
                    enviarEmailUnidadeFinal(processo, unidade, emailTitular);
                } else if (tipoUnidade == INTERMEDIARIA) {
                    enviarEmailUnidadeIntermediaria(processo, unidade, emailTitular, todasSubordinadas);
                }
            } catch (Exception e) {
                log.error("Falha ao preparar notificação para unidade {} no processo {}: {}", 
                        unidade.getSigla(), processo.getCodigo(), e.getMessage(), e);
            }
        }
    }

    private void enviarEmailUnidadeFinal(Processo processo, Unidade unidade, String email) {
        String assunto = String.format("SGC: Finalização do processo %s", processo.getDescricao());
        String html = notificacaoModelosService.criarEmailProcessoFinalizadoPorUnidade(unidade.getSigla(), processo.getDescricao());
        notificacaoEmailService.enviarEmailHtml(email, assunto, html);
        log.info("E-mail de finalização enviado para {}", unidade.getSigla());
    }

    private void enviarEmailUnidadeIntermediaria(Processo processo, Unidade unidade, String email, List<Unidade> subordinadas) {
        List<String> siglasSubordinadas = subordinadas.stream()
                .filter(u -> u.getUnidadeSuperior().getCodigo().equals(unidade.getCodigo()))
                .map(Unidade::getSigla)
                .sorted()
                .toList();

        if (siglasSubordinadas.isEmpty()) {
            log.warn("Nenhuma unidade subordinada encontrada para notificar a unidade intermediária {}", unidade.getSigla());
            return;
        }

        String assunto = String.format("SGC: Finalização do processo %s em unidades subordinadas", processo.getDescricao());
        String html = notificacaoModelosService.criarEmailProcessoFinalizadoUnidadesSubordinadas(unidade.getSigla(), processo.getDescricao(), siglasSubordinadas);

        notificacaoEmailService.enviarEmailHtml(email, assunto, html);
        log.info("E-mail de finalização enviado para {}", unidade.getSigla());
    }

    private void enviarEmailProcessoIniciado(
            Processo processo,
            Subprocesso subprocesso,
            Map<Long, ResponsavelDto> responsaveis,
            Map<String, UsuarioDto> usuarios) {

        Unidade unidade = subprocesso.getUnidade();
        Long codigoUnidade = unidade.getCodigo();

        try {
            ResponsavelDto responsavel = responsaveis.get(codigoUnidade);
            String nomeUnidade = unidade.getNome();

            UsuarioDto titular = usuarios.get(responsavel.getTitularTitulo());

            String corpoHtml = criarCorpoEmailPorTipo(unidade.getTipo(), processo, subprocesso);
            String assunto = switch (unidade.getTipo()) {
                case OPERACIONAL, INTEROPERACIONAL -> "Processo Iniciado - %s".formatted(processo.getDescricao());
                case INTERMEDIARIA -> "Processo Iniciado em Unidades Subordinadas - %s".formatted(processo.getDescricao());
                default -> throw new ErroEstadoImpossivel("Tipo de unidade desconhecido ao definir assunto: " + unidade.getTipo());
            };

            notificacaoEmailService.enviarEmailHtml(titular.getEmail(), assunto, corpoHtml);
            log.info("E-mail enviado para unidade {}", unidade.getSigla());

            if (responsavel.getSubstitutoTitulo() != null) {
                enviarEmailParaSubstituto(responsavel.getSubstitutoTitulo(), usuarios, assunto, corpoHtml, nomeUnidade);
            }
        } catch (Exception e) {
            log.error("Erro ao enviar e-mail para a unidade {}: {}", codigoUnidade, e.getClass().getSimpleName(), e);
        }
    }

    String criarCorpoEmailPorTipo(TipoUnidade tipoUnidade, Processo processo, Subprocesso subprocesso) {
        return switch (tipoUnidade) {
            case OPERACIONAL, INTEROPERACIONAL, INTERMEDIARIA -> notificacaoModelosService.criarEmailProcessoIniciado(
                    subprocesso.getUnidade().getNome(),
                    processo.getDescricao(),
                    processo.getTipo().name(),
                    subprocesso.getDataLimiteEtapa1()
            );
            default -> throw new ErroEstadoImpossivel("Tipo de unidade não suportado para geração de e-mail: " + tipoUnidade);
        };
    }

    private void enviarEmailParaSubstituto(String tituloSubstituto, Map<String, UsuarioDto> usuarios, String assunto, String corpoHtml, String nomeUnidade) {
        try {
            UsuarioDto substituto = usuarios.get(tituloSubstituto);
            if (substituto != null && substituto.getEmail() != null && !substituto.getEmail().isBlank()) {
                notificacaoEmailService.enviarEmailHtml(substituto.getEmail(), assunto, corpoHtml);
                log.info("E-mail enviado para o substituto da unidade {}.", nomeUnidade);
            }
        } catch (Exception e) {
            log.warn("Erro ao enviar e-mail para o substituto da unidade {}: {}", nomeUnidade, e.getClass().getSimpleName());
        }
    }
}
