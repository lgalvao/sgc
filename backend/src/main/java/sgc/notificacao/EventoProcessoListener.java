package sgc.notificacao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.AlertaService;
import sgc.alerta.modelo.Alerta;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.processo.eventos.EventoProcessoIniciado;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.processo.modelo.TipoProcesso;
import sgc.sgrh.dto.ResponsavelDto;
import sgc.sgrh.dto.UnidadeDto;
import sgc.sgrh.dto.UsuarioDto;
import sgc.sgrh.service.SgrhService;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.TipoUnidade;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static sgc.unidade.modelo.TipoUnidade.*;

/**
 * Listener para eventos de processo.
 * <p>
 * Processa eventos de processo iniciado, criando alertas e enviando e-mails
 * para as unidades participantes de forma diferenciada, conforme o tipo de unidade.
 */
@Component
@Profile("!e2e")
@RequiredArgsConstructor
@Slf4j
public class EventoProcessoListener {
    private final AlertaService servicoAlertas;
    private final NotificacaoService notificacaoService;
    private final NotificacaoModeloEmailService notificacaoModeloEmailService;
    private final SgrhService sgrhService;
    private final ProcessoRepo processoRepo;
    private final SubprocessoRepo repoSubprocesso;
    private final Environment environment;

    /**
     * Escuta e processa o evento {@link EventoProcessoIniciado}, disparado quando
     * um novo processo de mapeamento ou revisão é iniciado.
     * <p>
     * Este método orquestra a criação de alertas e o envio de emails para todos
     * os participantes do processo. A lógica diferencia o conteúdo das notificações
     * com base no tipo de unidade (Operacional, Intermediária, etc.), garantindo
     * que cada participante receba instruções relevantes para sua função.
     *
     * @param evento O evento contendo os detalhes do processo que foi iniciado.
     */
    @EventListener
    @Transactional
    public void aoIniciarProcesso(EventoProcessoIniciado evento) {
        log.info("Processando evento de processo iniciado: codProcesso={}, tipo={}",
                evento.codProcesso(), evento.tipo());
        try {
            Processo processo = processoRepo.findById(evento.codProcesso())
                    .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo não encontrado: ", evento.codProcesso()));

            List<Subprocesso> subprocessos = repoSubprocesso.findByProcessoCodigoWithUnidade(evento.codProcesso());

            if (subprocessos.isEmpty()) {
                log.warn("Nenhum subprocesso encontrado para o processo {}", evento.codProcesso());
                return;
            }

            log.info("Encontrados {} subprocessos para o processo {}", subprocessos.size(), evento.codProcesso());

            // 1. Criar alertas diferenciados por tipo de unidade
            List<Alerta> alertas = servicoAlertas.criarAlertasProcessoIniciado(
                    processo,
                    evento.codUnidades(),
                    subprocessos
            );
            log.info("Criados {} alertas para o processo {}", alertas.size(), processo.getCodigo());

            // 2. Enviar e-mails para cada subprocesso
            for (Subprocesso subprocesso : subprocessos) {
                try {
                    enviarEmailDeProcessoIniciado(processo, subprocesso);
                } catch (Exception e) {
                    log.error("Erro ao enviar e-mail para o subprocesso {}: {}", subprocesso.getCodigo(), e.getClass().getSimpleName(), e);
                }
            }
            log.info("Processamento de evento concluído para o processo {}", processo.getCodigo());
        } catch (Exception e) {
            log.error("Erro ao processar evento de processo iniciado: {}", e.getClass().getSimpleName(), e);
        }
    }

    private void enviarEmailDeProcessoIniciado(Processo processo, Subprocesso subprocesso) {
        if (subprocesso.getUnidade() == null) {
            log.warn("Subprocesso {} sem unidade associada", subprocesso.getCodigo());
            return;
        }

        Long codigoUnidade = subprocesso.getUnidade().getCodigo();
        try {
            Optional<UnidadeDto> unidadeOpt = sgrhService.buscarUnidadePorCodigo(codigoUnidade);
            if (unidadeOpt.isEmpty()) {
                boolean isE2E = Arrays.asList(environment.getActiveProfiles()).contains("e2e");
                if (isE2E) {
                    log.warn("Unidade não encontrada no SGRH: {} (ignorado no perfil e2e)", codigoUnidade);
                    return;
                }
                throw new IllegalStateException("Unidade não encontrada no SGRH: %d".formatted(codigoUnidade));
            }
            UnidadeDto unidade = unidadeOpt.get();

            Optional<ResponsavelDto> responsavelOpt = sgrhService.buscarResponsavelUnidade(codigoUnidade);
            if (responsavelOpt.isEmpty() || responsavelOpt.get().titularTitulo() == null) {
                log.warn("Responsável não encontrado para a unidade {}.",
                        unidade.nome());
                return;
            }

            UsuarioDto titular = sgrhService.buscarUsuarioPorTitulo(responsavelOpt.get().titularTitulo()).orElse(null);
            if (titular == null || titular.email() == null || titular.email().isBlank()) {
                log.warn("E-mail não encontrado para o titular da unidade {}.",
                        unidade.nome());
                return;
            }

            String assunto;
            String corpoHtml;
            TipoUnidade tipoUnidade = TipoUnidade.valueOf(unidade.tipo());
            TipoProcesso tipoProcesso = processo.getTipo();

            if (OPERACIONAL.equals(tipoUnidade)) {
                assunto = "Processo Iniciado - %s".formatted(processo.getDescricao());
                corpoHtml = notificacaoModeloEmailService.criarEmailDeProcessoIniciado(
                        unidade.nome(),
                        processo.getDescricao(),
                        tipoProcesso.name(),
                        subprocesso.getDataLimiteEtapa1()
                );
            } else if (INTERMEDIARIA.equals(tipoUnidade)) {
                assunto = "Processo Iniciado em Unidades Subordinadas - %s".formatted(processo.getDescricao());
                corpoHtml = notificacaoModeloEmailService.criarEmailDeProcessoIniciado(
                        unidade.nome(),
                        processo.getDescricao(),
                        tipoProcesso.name(),
                        subprocesso.getDataLimiteEtapa1()
                );
            } else if (INTEROPERACIONAL.equals(tipoUnidade)) {
                assunto = "Processo Iniciado - %s".formatted(processo.getDescricao());
                corpoHtml = notificacaoModeloEmailService.criarEmailDeProcessoIniciado(
                        unidade.nome(),
                        processo.getDescricao(),
                        tipoProcesso.name(),
                        subprocesso.getDataLimiteEtapa1()
                );
            } else {
                log.warn("Tipo de unidade desconhecido: {} (unidade={})", tipoUnidade, codigoUnidade);
                return;
            }

            notificacaoService.enviarEmailHtml(titular.email(), assunto, corpoHtml);
            log.info("E-mail enviado para a unidade {} ({})", unidade.sigla(), tipoUnidade);

            if (responsavelOpt.get().substitutoTitulo() != null) {
                enviarEmailParaSubstituto(responsavelOpt.get().substitutoTitulo(), assunto, corpoHtml, unidade.nome());
            }

        } catch (Exception e) {
            log.error("Erro ao enviar e-mail para a unidade {}: {}", codigoUnidade, e.getClass().getSimpleName(), e);
        }
    }

    private void enviarEmailParaSubstituto(String tituloSubstituto, String assunto,
                                           String corpoHtml, String nomeUnidade) {
        try {
            UsuarioDto substituto = sgrhService.buscarUsuarioPorTitulo(tituloSubstituto).orElse(null);
            if (substituto != null && substituto.email() != null && !substituto.email().isBlank()) {
                notificacaoService.enviarEmailHtml(substituto.email(), assunto, corpoHtml);
                log.info("E-mail enviado para o substituto da unidade {}.", nomeUnidade);
            }
        } catch (Exception e) {
            log.warn("Erro ao enviar e-mail para o substituto da unidade {}: {}", nomeUnidade, e.getClass().getSimpleName());
        }
    }
}