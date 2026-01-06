package sgc.notificacao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.AlertaService;
import sgc.organizacao.UsuarioService;
import sgc.organizacao.dto.ResponsavelDto;
import sgc.organizacao.dto.UsuarioDto;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.processo.eventos.EventoProcessoFinalizado;
import sgc.processo.eventos.EventoProcessoIniciado;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoService;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static sgc.organizacao.model.TipoUnidade.*;

/**
 * Listener para eventos de processo.
 *
 * <p>Processa eventos de processo iniciado e finalizado, criando alertas e enviando e-mails para as unidades
 * participantes de forma diferenciada, conforme o tipo de unidade.
 *
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventoProcessoListener {
    private final AlertaService servicoAlertas;
    private final NotificacaoEmailService notificacaoEmailService;
    private final NotificacaoModelosService notificacaoModelosService;
    private final UsuarioService usuarioService;
    private final ProcessoService processoService;
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
        } catch (RuntimeException e) {
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
        } catch (RuntimeException e) {
            log.error("Erro ao processar evento de processo finalizado: {}", e.getClass().getSimpleName(), e);
        }
    }

    private void processarInicioProcesso(EventoProcessoIniciado evento) {
        Processo processo = processoService.buscarEntidadePorId(evento.getCodProcesso());

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
            } catch (RuntimeException e) {
                log.error("Erro ao enviar e-mail referente a subprocesso {}: {}", subprocesso.getCodigo(), e.getClass().getSimpleName(), e);
            }
        }
    }

    private void processarFinalizacaoProcesso(EventoProcessoFinalizado evento) {
        Processo processo = processoService.buscarEntidadePorId(evento.getCodProcesso());

        List<Unidade> unidadesParticipantes = new ArrayList<>(processo.getParticipantes());

        // TODO isso é um erro interno gravíssimo -- nunca deveria ocorrer e deve ser tratado diferente!
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

        for (Unidade unidade : unidadesParticipantes) {
            try {

                // TODO Nenhum desses três erros poderiam ocorrer. As unidades são importadas de uma view. Se os dados retornados vierem com esse nivel de inconsistência,  o sistema nao deve nem caarregar!

                // TODO toda unidade TEM que ter um responsável cadastrado!
                ResponsavelDto responsavel = Optional.ofNullable(responsaveis.get(unidade.getCodigo()))
                        .orElseThrow(() -> new IllegalStateException("Responsável não encontrado para a unidade %s".formatted(unidade.getSigla())));

                // TODO toda unidade TEM que ter um titular
                UsuarioDto titular = Optional.ofNullable(usuarios.get(responsavel.getTitularTitulo()))
                        .orElseThrow(() -> new IllegalStateException("Usuário titular não encontrado: %s".formatted(responsavel.getTitularTitulo())));

                // TODO todo titular tem que ter um e-mail cadastrado
                String emailTitular = Optional.ofNullable(titular.getEmail())
                        .filter(e -> !e.isBlank())
                        .orElseThrow(() -> new IllegalStateException("E-mail não cadastrado para o titular %s".formatted(titular.getNome())));

                TipoUnidade tipoUnidade = unidade.getTipo();
                if (tipoUnidade == OPERACIONAL || tipoUnidade == INTEROPERACIONAL) {
                    enviarEmailUnidadeFinal(processo, unidade, emailTitular);
                } else if (tipoUnidade == INTERMEDIARIA) {
                    enviarEmailUnidadeIntermediaria(processo, unidade, emailTitular, unidadesParticipantes);
                }
            } catch (RuntimeException ex) { // TODO Exceçao muito genérica!
                log.error("Falha ao preparar notificação para unidade {} no processo {}: {}", unidade.getSigla(), processo.getCodigo(), ex.getMessage(), ex);
            }
        }
    }

    private void enviarEmailUnidadeFinal(Processo processo, Unidade unidade, String email) {
        String assunto = String.format("SGC: Conclusão do processo %s", processo.getDescricao());
        String html = notificacaoModelosService.criarEmailProcessoFinalizadoPorUnidade(unidade.getSigla(), processo.getDescricao());
        notificacaoEmailService.enviarEmailHtml(email, assunto, html);
        log.info("E-mail de finalização enviado para {}", unidade.getSigla());
    }

    // TODO por que passar todas as unidades se podemos passar apenas as subordinadas?
    private void enviarEmailUnidadeIntermediaria(Processo processo, Unidade unidade, String email, List<Unidade> todasUnidades) {
        List<String> siglasSubordinadas = todasUnidades.stream()
                .filter(u -> u.getUnidadeSuperior() != null
                        && u.getUnidadeSuperior().getCodigo().equals(unidade.getCodigo()))
                .map(Unidade::getSigla)
                .sorted()
                .toList();

        // TODO isso é um invariante! Se é uma unidade intermediária, TEM que ter subordinadas!
        if (siglasSubordinadas.isEmpty()) {
            log.warn("Nenhuma unidade subordinada encontrada para notificar a unidade intermediária {}", unidade.getSigla());
            return;
        }

        // TODO o termo é 'Finalização' e não 'Conclusão'
        String assunto = String.format("SGC: Conclusão do processo %s em unidades subordinadas", processo.getDescricao());
        String html = notificacaoModelosService.criarEmailProcessoFinalizadoUnidadesSubordinadas(unidade.getSigla(), processo.getDescricao(), siglasSubordinadas);

        notificacaoEmailService.enviarEmailHtml(email, assunto, html);
        log.info("E-mail de finalização enviado para {})", unidade.getSigla());
    }

    private void enviarEmailProcessoIniciado(
            Processo processo, // TODO para que passar o processo se o subprocesso já tem uma referência para ele?
            Subprocesso subprocesso,
            Map<Long, ResponsavelDto> responsaveis,
            Map<String, UsuarioDto> usuarios) {

        if (subprocesso.getUnidade() == null) {
            log.warn("Subprocesso {} sem unidade associada", subprocesso.getCodigo());
            return;
        }

        Unidade unidade = subprocesso.getUnidade();
        Long codigoUnidade = unidade.getCodigo();

        try {
            ResponsavelDto responsavel = responsaveis.get(codigoUnidade);
            String nomeUnidade = unidade.getNome();

            if (responsavel == null || responsavel.getTitularTitulo() == null) {
                // TODO erro impossivel!
                log.warn("Responsável não encontrado para a unidade {}.", nomeUnidade);
                return;
            }

            UsuarioDto titular = usuarios.get(responsavel.getTitularTitulo());
            if (titular == null || titular.getEmail() == null || titular.getEmail().isBlank()) {
                // TODO erro impossivel!
                log.warn("E-mail não encontrado para o titular da unidade {}.", nomeUnidade);
                return;
            }

            String assunto;
            String corpoHtml;
            TipoUnidade tipoUnidade = unidade.getTipo();
            TipoProcesso tipoProcesso = processo.getTipo();

            switch (tipoUnidade) {
                case OPERACIONAL, INTEROPERACIONAL -> {
                    assunto = "Processo Iniciado - %s".formatted(processo.getDescricao());
                    corpoHtml = notificacaoModelosService.criarEmailProcessoIniciado(nomeUnidade, processo.getDescricao(), tipoProcesso.name(), subprocesso.getDataLimiteEtapa1());
                }
                case INTERMEDIARIA -> {
                    assunto = "Processo Iniciado em Unidades Subordinadas - %s".formatted(processo.getDescricao());
                    corpoHtml = notificacaoModelosService.criarEmailProcessoIniciado(nomeUnidade, processo.getDescricao(), tipoProcesso.name(), subprocesso.getDataLimiteEtapa1());
                }
                // TODO Não existem outros tipos!!
                case null, default -> {
                    log.warn("Tipo de unidade desconhecido: {} (unidade={})", tipoUnidade, codigoUnidade);
                    return;
                }
            }

            notificacaoEmailService.enviarEmailHtml(titular.getEmail(), assunto, corpoHtml);
            log.info("E-mail enviado para unidade {}", unidade.getSigla());

            // TODO titulo nao pode ser nulo nunca!
            if (responsavel.getSubstitutoTitulo() != null) {
                enviarEmailParaSubstituto(responsavel.getSubstitutoTitulo(), usuarios, assunto, corpoHtml, nomeUnidade);
            }
        } catch (RuntimeException e) { // TODO exceção muito genérica!
            log.error("Erro ao enviar e-mail para a unidade {}: {}", codigoUnidade, e.getClass().getSimpleName(), e);
        }
    }

    private void enviarEmailParaSubstituto(String tituloSubstituto, Map<String, UsuarioDto> usuarios, String assunto, String corpoHtml, String nomeUnidade) {
        try {
            UsuarioDto substituto = usuarios.get(tituloSubstituto);
            if (substituto != null && substituto.getEmail() != null && !substituto.getEmail().isBlank()) {
                notificacaoEmailService.enviarEmailHtml(substituto.getEmail(), assunto, corpoHtml);
                log.info("E-mail enviado para o substituto da unidade {}.", nomeUnidade);
            }
        } catch (RuntimeException e) {
            log.warn("Erro ao enviar e-mail para o substituto da unidade {}: {}", nomeUnidade, e.getClass().getSimpleName());
        }
    }
}
