package sgc.notificacao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.AlertaService;
import sgc.alerta.modelo.Alerta;
import sgc.processo.eventos.ProcessoIniciadoEvento;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.sgrh.SgrhService;
import sgc.sgrh.dto.ResponsavelDto;
import sgc.sgrh.dto.UnidadeDto;
import sgc.sgrh.dto.UsuarioDto;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.TipoUnidade;
import sgc.processo.modelo.TipoProcesso;

import java.util.List;
import java.util.Optional;

/**
 * Listener para eventos de processo.
 * <p>
 * Processa eventos de processo iniciado, criando alertas e enviando e-mails
 * para as unidades participantes de forma diferenciada, conforme o tipo de unidade.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventoProcessoListener {
    private final AlertaService servicoAlertas;
    private final NotificacaoServico notificacaoServico;
    private final NotificacaoModeloEmailService notificacaoModeloEmailService;
    private final SgrhService sgrhService;
    private final ProcessoRepo processoRepo;
    private final SubprocessoRepo subprocessoRepo;

    /**
     * Processa o evento de processo iniciado.
     * Cria alertas diferenciados e envia e-mails para as unidades.
     *
     * @param evento Evento contendo os dados do processo iniciado
     */
    @EventListener
    @Transactional
    public void aoIniciarProcesso(ProcessoIniciadoEvento evento) {
        log.info("Processando evento de processo iniciado: idProcesso={}, tipo={}",
                evento.idProcesso(), evento.tipo());

        try {
            Processo processo = processoRepo.findById(evento.idProcesso())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Processo não encontrado: " + evento.idProcesso()));

            List<Subprocesso> subprocessos = subprocessoRepo
                    .findByProcessoCodigoWithUnidade(evento.idProcesso());

            if (subprocessos.isEmpty()) {
                log.warn("Nenhum subprocesso encontrado para o processo {}", evento.idProcesso());
                return;
            }

            log.info("Encontrados {} subprocessos para o processo {}",
                    subprocessos.size(), evento.idProcesso());

            // 1. Criar alertas diferenciados por tipo de unidade
            List<Alerta> alertas = servicoAlertas.criarAlertasProcessoIniciado(
                    processo,
                    evento.idsUnidades(),
                    subprocessos
            );
            log.info("Criados {} alertas para o processo {}", alertas.size(), processo.getCodigo());

            // 2. Enviar e-mails para cada subprocesso
            for (Subprocesso subprocesso : subprocessos) {
                try {
                    enviarEmailDeProcessoIniciado(processo, subprocesso);
                } catch (Exception e) {
                    log.error("Erro ao enviar e-mail para o subprocesso {}: {}", subprocesso.getCodigo(), e.getMessage(), e);
                }
            }

            log.info("Processamento de evento concluído para o processo {}", processo.getCodigo());
        } catch (Exception e) {
            log.error("Erro ao processar evento de processo iniciado: {}", e.getMessage(), e);
        }
    }

    private void enviarEmailDeProcessoIniciado(Processo processo, Subprocesso subprocesso) {
        if (subprocesso.getUnidade() == null) {
            log.warn("Subprocesso {} sem unidade associada", subprocesso.getCodigo());
            return;
        }

        Long codigoUnidade = subprocesso.getUnidade().getCodigo();

        try {
            UnidadeDto unidade = sgrhService.buscarUnidadePorCodigo(codigoUnidade)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Unidade não encontrada no SGRH: " + codigoUnidade));

            Optional<ResponsavelDto> responsavelOpt = sgrhService.buscarResponsavelUnidade(codigoUnidade);

            if (responsavelOpt.isEmpty() || responsavelOpt.get().titularTitulo() == null) {
                log.warn("Responsável não encontrado para a unidade {} ({})",
                        unidade.nome(), codigoUnidade);
                return;
            }

            UsuarioDto titular = sgrhService.buscarUsuarioPorTitulo(responsavelOpt.get().titularTitulo()).orElse(null);

            if (titular == null || titular.email() == null || titular.email().isBlank()) {
                log.warn("E-mail não encontrado para o titular {} da unidade {}",
                        responsavelOpt.get().titularTitulo(), unidade.nome());
                return;
            }

            String assunto;
            String corpoHtml;
            TipoUnidade tipoUnidade = TipoUnidade.valueOf(unidade.tipo());
            TipoProcesso tipoProcesso = processo.getTipo();

            if (TipoUnidade.OPERACIONAL.equals(tipoUnidade)) {
                assunto = "Processo Iniciado - " + processo.getDescricao();
                corpoHtml = notificacaoModeloEmailService.criarEmailDeProcessoIniciado(
                        unidade.nome(),
                        processo.getDescricao(),
                        tipoProcesso.name(),
                        subprocesso.getDataLimiteEtapa1()
                );
            } else if (TipoUnidade.INTERMEDIARIA.equals(tipoUnidade)) {
                assunto = "Processo Iniciado em Unidades Subordinadas - " + processo.getDescricao();
                corpoHtml = criarEmailParaUnidadeIntermediaria(
                        unidade.nome(),
                        processo.getDescricao(),
                        tipoProcesso.name(),
                        subprocesso.getDataLimiteEtapa1()
                );
            } else if (TipoUnidade.INTEROPERACIONAL.equals(tipoUnidade)) {
                assunto = "Processo Iniciado - " + processo.getDescricao();
                corpoHtml = criarEmailParaUnidadeInteroperacional(
                        unidade.nome(),
                        processo.getDescricao(),
                        tipoProcesso.name(),
                        subprocesso.getDataLimiteEtapa1()
                );
            } else {
                log.warn("Tipo de unidade desconhecido: {} (unidade={})",
                        tipoUnidade, codigoUnidade);
                return;
            }

            notificacaoServico.enviarEmailHtml(titular.email(), assunto, corpoHtml);
            log.info("E-mail enviado para a unidade {} ({}) - Destinatário: {} ({})",
                    unidade.sigla(), tipoUnidade, titular.nome(), titular.email());

            if (responsavelOpt.get().substitutoTitulo() != null) {
                enviarEmailParaSubstituto(responsavelOpt.get().substitutoTitulo(), assunto, corpoHtml, unidade.nome());
            }

        } catch (Exception e) {
            log.error("Erro ao enviar e-mail para a unidade {}: {}",
                    codigoUnidade, e.getMessage(), e);
        }
    }

    private void enviarEmailParaSubstituto(String tituloSubstituto, String assunto,
                                           String corpoHtml, String nomeUnidade) {
        try {
            UsuarioDto substituto = sgrhService.buscarUsuarioPorTitulo(tituloSubstituto).orElse(null);
            if (substituto != null && substituto.email() != null && !substituto.email().isBlank()) {
                notificacaoServico.enviarEmailHtml(substituto.email(), assunto, corpoHtml);
                log.info("E-mail enviado para o substituto da unidade {} - Destinatário: {} ({})",
                        nomeUnidade, substituto.nome(), substituto.email());
            }
        } catch (Exception e) {
            log.warn("Erro ao enviar e-mail para o substituto da unidade {}: {}",
                    nomeUnidade, e.getMessage());
        }
    }

    private String criarEmailParaUnidadeIntermediaria(String nomeUnidade, String nomeProcesso,
                                                  String tipoProcesso, java.time.LocalDate dataLimite) {
        String dataFormatada = dataLimite != null ?
                String.format("%02d/%02d/%d",
                        dataLimite.getDayOfMonth(),
                        dataLimite.getMonthValue(),
                        dataLimite.getYear()) :
                "a definir";

        String conteudo = String.format("""
                        <p>Um novo processo de <strong>%s</strong> foi iniciado em unidades subordinadas à sua unidade.</p>%n
                        %n
                        <div style="background-color: #f0f8ff; padding: 15px; margin: 15px 0; border-left: 4px solid #0066cc;">%n
                            <p style="margin: 5px 0;"><strong>Unidade:</strong> %s</p>%n
                            <p style="margin: 5px 0;"><strong>Processo:</strong> %s</p>%n
                            <p style="margin: 5px 0;"><strong>Tipo:</strong> %s</p>%n
                            <p style="margin: 5px 0;"><strong>Data limite para etapa 1:</strong> %s</p>%n
                        </div>%n
                        %n
                        <p><strong>Informações importantes:</strong></p>%n
                        <ul>%n
                            <li>As unidades subordinadas já podem iniciar o cadastro de atividades e conhecimentos.</li>%n
                            <li>À medida que os cadastros forem disponibilizados, será possível visualizar e realizar a validação.</li>%n
                            <li>Acompanhe o andamento no sistema SGC.</li>%n
                        </ul>%n
                        %n
                        <p style="margin-top: 20px;">%n
                            <a href="https://sgc.tre-pe.jus.br"%n
                               style="background-color: #0066cc; color: white; padding: 10px 20px;%n
                                      text-decoration: none; border-radius: 5px; display: inline-block;">%n
                                Acompanhar Processo%n
                            </a>%n
                        </p>%n
                        """,
                tipoProcesso, nomeUnidade, nomeProcesso, tipoProcesso, dataFormatada);

        return notificacaoModeloEmailService.criarTemplateBase(
                "Processo Iniciado em Unidades Subordinadas", conteudo);
    }

    private String criarEmailParaUnidadeInteroperacional(String nomeUnidade, String nomeProcesso,
                                                     String tipoProcesso, java.time.LocalDate dataLimite) {
        String dataFormatada = dataLimite != null ?
                String.format("%02d/%02d/%d",
                        dataLimite.getDayOfMonth(),
                        dataLimite.getMonthValue(),
                        dataLimite.getYear()) :
                "a definir";

        String conteudo = String.format("""
                        <p>Um novo processo de <strong>%s</strong> foi iniciado para sua unidade.</p>%n
                        %n
                        <div style="background-color: #f0f8ff; padding: 15px; margin: 15px 0; border-left: 4px solid #0066cc;">%n
                            <p style="margin: 5px 0;"><strong>Unidade:</strong> %s</p>%n
                            <p style="margin: 5px 0;"><strong>Processo:</strong> %s</p>%n
                            <p style="margin: 5px 0;"><strong>Tipo:</strong> %s</p>%n
                            <p style="margin: 5px 0;"><strong>Data limite para etapa 1:</strong> %s</p>%n
                        </div>%n
                        %n
                        <div style="background-color: #fff3cd; padding: 15px; margin: 15px 0; border-left: 4px solid #ffc107;">%n
                            <p style="margin: 0;"><strong>⚠ Atenção:</strong> Sua unidade é do tipo <strong>Interoperacional</strong>.</p>%n
                        </div>%n
                        %n
                        <p><strong>Você deverá realizar DUAS ações:</strong></p>%n
                        <ol>%n
                            <li><strong>Como unidade operacional:</strong> Realizar o cadastro de atividades e conhecimentos da sua própria unidade.</li>%n
                            <li><strong>Como unidade intermediária:</strong> Validar os mapas das unidades subordinadas quando forem disponibilizados.</li>%n
                        </ol>%n
                        %n
                        <p style="margin-top: 20px;">%n
                            <a href="https://sgc.tre-pe.jus.br"%n
                               style="background-color: #0066cc; color: white; padding: 10px 20px;%n
                                      text-decoration: none; border-radius: 5px; display: inline-block;">%n
                                Acessar Sistema%n
                            </a>%n
                        </p>%n
                        """,
                tipoProcesso, nomeUnidade, nomeProcesso, tipoProcesso, dataFormatada);

        return notificacaoModeloEmailService.criarTemplateBase(
                "Processo Iniciado - Unidade Interoperacional", conteudo);
    }
}