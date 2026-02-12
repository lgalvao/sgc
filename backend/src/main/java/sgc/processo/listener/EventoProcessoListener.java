package sgc.processo.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import sgc.alerta.AlertaFacade;
import sgc.notificacao.NotificacaoEmailService;
import sgc.notificacao.NotificacaoModelosService;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.dto.UnidadeResponsavelDto;
import sgc.organizacao.dto.UsuarioDto;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.processo.eventos.EventoProcessoFinalizado;
import sgc.processo.eventos.EventoProcessoIniciado;
import sgc.processo.model.Processo;
import sgc.processo.service.ProcessoFacade;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoFacade;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static sgc.organizacao.model.TipoUnidade.*;

/**
 * Listener assíncrono para eventos de processo.
 *
 * <p>
 * Processa eventos de processo iniciado e finalizado, criando alertas e
 * enviando e-mails para as unidades participantes de forma diferenciada, conforme o tipo de unidade.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventoProcessoListener {
    private final AlertaFacade servicoAlertas;
    private final NotificacaoEmailService notificacaoEmailService;
    private final NotificacaoModelosService notificacaoModelosService;
    private final UnidadeFacade unidadeService;
    private final UsuarioFacade usuarioService;
    private final ProcessoFacade processoFacade;
    private final SubprocessoFacade subprocessoFacade;

    /**
     * Escuta e processa o evento {@link EventoProcessoIniciado}, disparado quando
     * um novo processo
     * de mapeamento ou revisão é iniciado.
     *
     * <p>
     * Este método orquestra a criação de alertas e o envio de emails para todos os
     * participantes
     * do processo. A lógica diferencia o conteúdo das notificações com base no tipo
     * de unidade
     * (Operacional, Intermediária, etc.), garantindo que cada participante receba
     * instruções
     * relevantes para sua função.
     *
     * <p>
     * Executado de forma assíncrona para não bloquear a transação principal do
     * workflow.
     *
     * @param evento O evento contendo os detalhes do processo que foi iniciado.
     */
    @TransactionalEventListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void aoIniciarProcesso(EventoProcessoIniciado evento) {
        try {
            processarInicioProcesso(evento);
        } catch (Exception e) {
            log.error("Erro ao processar evento de processo iniciado: {}", e.getClass().getSimpleName(), e);
        }
    }

    /**
     * Escuta e processa o evento {@link EventoProcessoFinalizado}, disparado quando
     * um processo
     * é concluído.
     *
     * <p>
     * Executado de forma assíncrona para não bloquear a transação principal do
     * workflow.
     *
     * @param evento O evento contendo os detalhes do processo que foi finalizado.
     */
    @TransactionalEventListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void aoFinalizarProcesso(EventoProcessoFinalizado evento) {
        try {
            processarFinalizacaoProcesso(evento);
        } catch (Exception e) {
            log.error("Erro ao processar evento de processo finalizado: {}", e.getClass().getSimpleName(), e);
        }
    }

    private void processarInicioProcesso(EventoProcessoIniciado evento) {
        Processo processo = processoFacade.buscarEntidadePorId(evento.getCodProcesso());
        List<Subprocesso> subprocessos = subprocessoFacade.listarEntidadesPorProcesso(evento.getCodProcesso());

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

        Map<Long, UnidadeResponsavelDto> responsaveis = unidadeService.buscarResponsaveisUnidades(todosCodigosUnidades);

        List<String> todosTitulos = new ArrayList<>();
        responsaveis.values().forEach(r -> {
            todosTitulos.add(r.titularTitulo());
            if (r.substitutoTitulo() != null)
                todosTitulos.add(r.substitutoTitulo());
        });

        Map<String, UsuarioDto> usuarios = usuarioService.buscarUsuariosPorTitulos(todosTitulos);

        // 3. Enviar e-mails para cada subprocesso
        for (Subprocesso subprocesso : subprocessos) {
            enviarEmailProcessoIniciado(processo, subprocesso, responsaveis, usuarios);
        }
    }

    private void processarFinalizacaoProcesso(EventoProcessoFinalizado evento) {
        Processo processo = processoFacade.buscarEntidadePorId(evento.getCodProcesso());

        List<Long> codigosParticipantes = processo.getCodigosParticipantes();

        if (codigosParticipantes.isEmpty()) {
            log.warn("Nenhuma unidade participante encontrada para notificar ao finalizar processo {}",
                    processo.getCodigo());
            return;
        }

        List<Unidade> unidadesParticipantes = unidadeService.buscarEntidadesPorIds(codigosParticipantes);

        List<Long> todosCodigosUnidades = unidadesParticipantes.stream().map(Unidade::getCodigo).toList();
        Map<Long, UnidadeResponsavelDto> responsaveis = unidadeService.buscarResponsaveisUnidades(todosCodigosUnidades);

        Map<String, UsuarioDto> usuarios = usuarioService.buscarUsuariosPorTitulos(responsaveis.values().stream()
                .map(UnidadeResponsavelDto::titularTitulo)
                .filter(Objects::nonNull)
                .distinct()
                .toList());

        for (Unidade unidade : unidadesParticipantes) {
            enviarNotificacaoFinalizacao(processo, unidade, responsaveis, usuarios, unidadesParticipantes);
        }
    }

    private void enviarNotificacaoFinalizacao(Processo processo, Unidade unidade,
            Map<Long, UnidadeResponsavelDto> responsaveis,
            Map<String, UsuarioDto> usuarios,
            List<Unidade> subordinadas) {
        try {
            UnidadeResponsavelDto responsavel = responsaveis.get(unidade.getCodigo());
            UsuarioDto titular = usuarios.get(responsavel.titularTitulo());
            String emailTitular = titular.email();
            TipoUnidade tipoUnidade = unidade.getTipo();

            if (tipoUnidade == OPERACIONAL || tipoUnidade == INTEROPERACIONAL || tipoUnidade == RAIZ) {
                enviarEmailUnidadeFinal(processo, unidade, emailTitular);
            } else if (tipoUnidade == INTERMEDIARIA) {
                enviarEmailUnidadeIntermediaria(processo, unidade, emailTitular, subordinadas);
            }
        } catch (Exception e) {
            log.error("Falha ao preparar notificação para unidade {} no processo {}: {}",
                    unidade.getSigla(), processo.getCodigo(), e.getMessage(), e);
        }
    }

    private void enviarEmailUnidadeFinal(Processo processo, Unidade unidade, String email) {
        String assunto = String.format("SGC: Finalização do processo %s", processo.getDescricao());
        String html = notificacaoModelosService.criarEmailProcessoFinalizadoPorUnidade(
                unidade.getSigla(),
                processo.getDescricao());
        notificacaoEmailService.enviarEmailHtml(email, assunto, html);
        log.info("E-mail de finalização enviado para {}", unidade.getSigla());
    }

    private void enviarEmailUnidadeIntermediaria(Processo processo, Unidade unidade, String email,
            List<Unidade> subordinadas) {
        List<String> siglasSubordinadas = subordinadas.stream()
                .filter(u -> u.getUnidadeSuperior() != null
                        && u.getUnidadeSuperior().getCodigo().equals(unidade.getCodigo()))
                .map(Unidade::getSigla)
                .sorted()
                .toList();

        if (siglasSubordinadas.isEmpty()) {
            log.warn("Nenhuma unidade subordinada encontrada para notificar a unidade intermediária {}",
                    unidade.getSigla());
            return;
        }

        String assunto = String.format("SGC: Finalização do processo %s em unidades subordinadas",
                processo.getDescricao());
        String html = notificacaoModelosService.criarEmailProcessoFinalizadoUnidadesSubordinadas(unidade.getSigla(),
                processo.getDescricao(), siglasSubordinadas);

        notificacaoEmailService.enviarEmailHtml(email, assunto, html);
        log.info("E-mail de finalização (unidade intermediaria) enviado para {}", unidade.getSigla());
    }

    private void enviarEmailProcessoIniciado(
            Processo processo,
            Subprocesso subprocesso,
            Map<Long, UnidadeResponsavelDto> responsaveis,
            Map<String, UsuarioDto> usuarios) {

        Unidade unidade = subprocesso.getUnidade();
        Long codigoUnidade = unidade.getCodigo();

        try {
            UnidadeResponsavelDto responsavel = responsaveis.get(codigoUnidade);
            String nomeUnidade = unidade.getNome();
            UsuarioDto titular = usuarios.get(responsavel.titularTitulo());
            String assunto = switch (unidade.getTipo()) {
                case OPERACIONAL, INTEROPERACIONAL, RAIZ -> "Processo Iniciado - %s".formatted(processo.getDescricao());
                case INTERMEDIARIA ->
                    "Processo Iniciado em Unidades Subordinadas - %s".formatted(processo.getDescricao());
                case SEM_EQUIPE -> "Notificação não enviada para unidade (N/A)";
            };

            String corpoHtml = criarCorpoEmailPorTipo(unidade.getTipo(), processo, subprocesso);
            
            if (titular != null && titular.email() != null && !titular.email().isBlank()) {
                notificacaoEmailService.enviarEmailHtml(titular.email(), assunto, corpoHtml);
                log.info("E-mail enviado para unidade {}", unidade.getSigla());
            } else {
                log.warn("E-mail não enviado para unidade {}: titular ou e-mail inválido", unidade.getSigla());
            }

            if (responsavel.substitutoTitulo() != null) {
                enviarEmailParaSubstituto(responsavel.substitutoTitulo(), usuarios, assunto, corpoHtml, nomeUnidade);
            }
        } catch (Exception e) {
            log.error("Erro ao enviar e-mail para a unidade {}: {}", codigoUnidade, e.getClass().getSimpleName(), e);
        }
    }

    String criarCorpoEmailPorTipo(TipoUnidade tipoUnidade, Processo processo, Subprocesso subprocesso) {
        return switch (tipoUnidade) {
            case OPERACIONAL, INTEROPERACIONAL, INTERMEDIARIA, RAIZ ->
                    notificacaoModelosService.criarEmailProcessoIniciado(
                            subprocesso.getUnidade().getNome(),
                            processo.getDescricao(),
                            processo.getTipo().name(),
                            subprocesso.getDataLimiteEtapa1());
            case SEM_EQUIPE ->
                    throw new IllegalArgumentException("Tipo de unidade não suportado para geração de e-mail: " + tipoUnidade);
        };
    }

    void enviarEmailParaSubstituto(String tituloSubstituto, Map<String, UsuarioDto> usuarios, String assunto,
            String corpoHtml, String nomeUnidade) {
        try {
            UsuarioDto substituto = usuarios.get(tituloSubstituto);
            if (substituto != null && substituto.email() != null && !substituto.email().isBlank()) {
                notificacaoEmailService.enviarEmailHtml(substituto.email(), assunto, corpoHtml);
                log.info("E-mail enviado para o substituto da unidade {}.", nomeUnidade);
            }
        } catch (Exception e) {
            log.warn("Erro ao enviar e-mail para o substituto da unidade {}: {}", nomeUnidade,
                    e.getClass().getSimpleName());
        }
    }
}
