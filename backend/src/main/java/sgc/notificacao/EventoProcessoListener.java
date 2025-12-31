package sgc.notificacao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.AlertaService;
import sgc.alerta.model.Alerta;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.processo.eventos.EventoProcessoFinalizado;
import sgc.processo.eventos.EventoProcessoIniciado;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoService;
import sgc.usuario.UsuarioService;
import sgc.usuario.dto.ResponsavelDto;
import sgc.usuario.dto.UsuarioDto;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoService;
import sgc.unidade.model.TipoUnidade;
import sgc.unidade.model.Unidade;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static sgc.unidade.model.TipoUnidade.*;

/**
 * Listener para eventos de processo.
 *
 * <p>Processa eventos de processo iniciado e finalizado, criando alertas e enviando e-mails para as unidades
 * participantes de forma diferenciada, conforme o tipo de unidade.
 *
 * <p>Nota: Este listener permanece no pacote 'notificacao' pois sua responsabilidade principal é
 * orquestrar notificações (alertas e e-mails), não apenas escutar eventos. O pacote 'eventos'
 * contém as definições das classes de evento (EventoProcessoIniciado, etc), enquanto este listener
 * contém a lógica de reação aos eventos.
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
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public void aoIniciarProcesso(EventoProcessoIniciado evento) {
        log.debug("Processando evento de processo iniciado: codProcesso={}, tipo={}", evento.getCodProcesso(), evento.getTipo());
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
        log.debug("Processando evento de processo finalizado: {}", evento.getCodProcesso());
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

        log.debug(
                "Encontrados {} subprocessos para o processo {}",
                subprocessos.size(),
                evento.getCodProcesso());

        // 1. Criar alertas diferenciados por tipo de unidade
        List<Unidade> unidadesParticipantes = subprocessos.stream()
                .map(Subprocesso::getUnidade)
                .toList();
        List<Alerta> alertas = servicoAlertas.criarAlertasProcessoIniciado(processo, unidadesParticipantes);
        log.debug("Criados {} alertas para o processo {}", alertas.size(), processo.getCodigo());

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
                enviarEmailDeProcessoIniciado(processo, subprocesso, responsaveis, usuarios);
            } catch (RuntimeException e) {
                log.error("Erro ao enviar e-mail para o subprocesso {}: {}", subprocesso.getCodigo(), e.getClass().getSimpleName(), e);
            }
        }
        log.debug("Processamento de evento concluído para o processo {}", processo.getCodigo());
    }

    private void processarFinalizacaoProcesso(EventoProcessoFinalizado evento) {
        Processo processo = processoService.buscarEntidadePorId(evento.getCodProcesso());

        List<Unidade> unidadesParticipantes = new ArrayList<>(processo.getParticipantes());

        if (unidadesParticipantes.isEmpty()) {
            log.warn("Nenhuma unidade participante encontrada para notificar na finalização do processo {}", processo.getCodigo());
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
                ResponsavelDto responsavel =
                        Optional.ofNullable(responsaveis.get(unidade.getCodigo()))
                                .orElseThrow(() -> new IllegalStateException("Responsável não encontrado para a unidade %s".formatted(unidade.getSigla())));

                UsuarioDto titular =
                        Optional.ofNullable(usuarios.get(responsavel.getTitularTitulo()))
                                .orElseThrow(() -> new IllegalStateException("Usuário titular não encontrado: %s".formatted(responsavel.getTitularTitulo())));

                String emailTitular =
                        Optional.ofNullable(titular.getEmail())
                                .filter(e -> !e.isBlank())
                                .orElseThrow(() -> new IllegalStateException("E-mail não cadastrado para o titular %s".formatted(titular.getNome())));

                if (unidade.getTipo() == TipoUnidade.OPERACIONAL || unidade.getTipo() == TipoUnidade.INTEROPERACIONAL) {
                    enviarEmailUnidadeFinal(processo, unidade, emailTitular);
                } else if (unidade.getTipo() == TipoUnidade.INTERMEDIARIA) {
                    enviarEmailUnidadeIntermediaria(
                            processo, unidade, emailTitular, unidadesParticipantes);
                }
            } catch (RuntimeException ex) {
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

    private void enviarEmailUnidadeIntermediaria(
            Processo processo,
            Unidade unidadeIntermediaria,
            String email,
            List<Unidade> todasUnidades) {

        List<String> siglasSubordinadas = todasUnidades.stream()
                        .filter(u -> u.getUnidadeSuperior() != null 
                                && u.getUnidadeSuperior().getCodigo().equals(unidadeIntermediaria.getCodigo()))
                        .map(Unidade::getSigla)
                        .sorted()
                        .toList();

        if (siglasSubordinadas.isEmpty()) {
            log.warn("Nenhuma unidade subordinada encontrada para notificar a unidade intermediária {}", unidadeIntermediaria.getSigla());
            return;
        }

        String assunto = String.format("SGC: Conclusão do processo %s em unidades subordinadas", processo.getDescricao());
        String html = notificacaoModelosService.criarEmailProcessoFinalizadoUnidadesSubordinadas(unidadeIntermediaria.getSigla(), processo.getDescricao(), siglasSubordinadas);

        notificacaoEmailService.enviarEmailHtml(email, assunto, html);
        log.info("E-mail de finalização enviado para {})", unidadeIntermediaria.getSigla());
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private void enviarEmailDeProcessoIniciado(
            Processo processo, 
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
            if (responsavel == null || responsavel.getTitularTitulo() == null) {
                log.warn("Responsável não encontrado para a unidade {}.", unidade.getNome());
                return;
            }

            UsuarioDto titular = usuarios.get(responsavel.getTitularTitulo());
            if (titular == null || titular.getEmail() == null || titular.getEmail().isBlank()) {
                log.warn("E-mail não encontrado para o titular da unidade {}.", unidade.getNome());
                return;
            }

            String assunto;
            String corpoHtml;
            TipoUnidade tipoUnidade = unidade.getTipo();
            TipoProcesso tipoProcesso = processo.getTipo();

            if (OPERACIONAL == tipoUnidade) {
                assunto = "Processo Iniciado - %s".formatted(processo.getDescricao());
                corpoHtml = notificacaoModelosService.criarEmailDeProcessoIniciado(unidade.getNome(), processo.getDescricao(), tipoProcesso.name(), subprocesso.getDataLimiteEtapa1());
            } else if (INTERMEDIARIA == tipoUnidade) {
                assunto = "Processo Iniciado em Unidades Subordinadas - %s".formatted(processo.getDescricao());
                corpoHtml = notificacaoModelosService.criarEmailDeProcessoIniciado(unidade.getNome(), processo.getDescricao(), tipoProcesso.name(), subprocesso.getDataLimiteEtapa1());
            } else if (INTEROPERACIONAL == tipoUnidade) {
                assunto = "Processo Iniciado - %s".formatted(processo.getDescricao());
                corpoHtml = notificacaoModelosService.criarEmailDeProcessoIniciado(unidade.getNome(), processo.getDescricao(), tipoProcesso.name(), subprocesso.getDataLimiteEtapa1());
            } else {
                log.warn("Tipo de unidade desconhecido: {} (unidade={})", tipoUnidade, codigoUnidade);
                return;
            }

            notificacaoEmailService.enviarEmailHtml(titular.getEmail(), assunto, corpoHtml);
            log.info("E-mail enviado para unidade {}", unidade.getSigla());

            if (responsavel.getSubstitutoTitulo() != null) {
                enviarEmailParaSubstituto(responsavel.getSubstitutoTitulo(), usuarios, assunto, corpoHtml, unidade.getNome());
            }

        } catch (RuntimeException e) {
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
