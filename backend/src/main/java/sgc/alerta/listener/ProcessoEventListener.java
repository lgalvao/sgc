package sgc.alerta.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.Alerta;
import sgc.alerta.AlertaService;
import sgc.notificacao.EmailNotificationService;
import sgc.notificacao.EmailTemplateService;
import sgc.processo.EventoProcessoIniciado;
import sgc.processo.Processo;
import sgc.processo.ProcessoRepository;
import sgc.sgrh.dto.ResponsavelDto;
import sgc.sgrh.dto.UnidadeDto;
import sgc.sgrh.dto.UsuarioDto;
import sgc.sgrh.service.SgrhService;
import sgc.subprocesso.Subprocesso;
import sgc.subprocesso.SubprocessoRepository;

import java.util.List;
import java.util.Optional;

/**
 * Listener para eventos de processo.
 * <p>
 * Processa eventos de processo iniciado, criando alertas e enviando e-mails
 * para as unidades participantes de forma diferenciada conforme tipo de unidade.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProcessoEventListener {
    
    private final AlertaService alertaService;
    private final EmailNotificationService emailService;
    private final EmailTemplateService emailTemplateService;
    private final SgrhService sgrhService;
    private final ProcessoRepository processoRepository;
    private final SubprocessoRepository subprocessoRepository;
    
    /**
     * Processa evento de processo iniciado.
     * Cria alertas diferenciados e envia e-mails para as unidades.
     * 
     * @param evento Evento contendo dados do processo iniciado
     */
    @EventListener
    @Async
    @Transactional
    public void handleProcessoIniciado(EventoProcessoIniciado evento) {
        log.info("Processando evento de processo iniciado: processoId={}, tipo={}", 
                evento.processoId(), evento.tipo());
        
        try {
            // Buscar processo completo
            Processo processo = processoRepository.findById(evento.processoId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Processo não encontrado: " + evento.processoId()));
            
            // Buscar subprocessos criados
            List<Subprocesso> subprocessos = subprocessoRepository
                    .findByProcessoCodigoWithUnidade(evento.processoId());
            
            if (subprocessos.isEmpty()) {
                log.warn("Nenhum subprocesso encontrado para processo {}", evento.processoId());
                return;
            }
            
            log.info("Encontrados {} subprocessos para processo {}", 
                    subprocessos.size(), evento.processoId());
            
            // 1. Criar alertas diferenciados por tipo de unidade
            List<Alerta> alertas = alertaService.criarAlertasProcessoIniciado(
                    processo, 
                    subprocessos
            );
            log.info("Criados {} alertas para processo {}", alertas.size(), processo.getCodigo());
            
            // 2. Enviar e-mails para cada subprocesso
            for (Subprocesso subprocesso : subprocessos) {
                try {
                    enviarEmailProcessoIniciado(processo, subprocesso);
                } catch (Exception e) {
                    log.error("Erro ao enviar e-mail para subprocesso {}: {}", 
                            subprocesso.getCodigo(), e.getMessage(), e);
                    // Continua processando outros subprocessos
                }
            }
            
            log.info("Processamento de evento concluído para processo {}", processo.getCodigo());
            
        } catch (Exception e) {
            log.error("Erro ao processar evento de processo iniciado: {}", e.getMessage(), e);
            // Não propaga exceção para não interromper outros listeners
        }
    }
    
    /**
     * Envia e-mail de processo iniciado para responsável da unidade.
     * 
     * @param processo Processo iniciado
     * @param subprocesso Subprocesso da unidade
     */
    private void enviarEmailProcessoIniciado(Processo processo, Subprocesso subprocesso) {
        if (subprocesso.getUnidade() == null) {
            log.warn("Subprocesso {} sem unidade associada", subprocesso.getCodigo());
            return;
        }
        
        Long unidadeCodigo = subprocesso.getUnidade().getCodigo();
        
        try {
            // Buscar dados da unidade via SGRH
            UnidadeDto unidade = sgrhService.buscarUnidadePorCodigo(unidadeCodigo)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Unidade não encontrada no SGRH: " + unidadeCodigo));
            
            // Buscar responsável da unidade
            Optional<ResponsavelDto> responsavel = sgrhService.buscarResponsavelUnidade(unidadeCodigo);
            
            if (responsavel.isEmpty() || responsavel.get().titularTitulo() == null) {
                log.warn("Responsável não encontrado para unidade {} ({})", 
                        unidade.nome(), unidadeCodigo);
                return;
            }
            
            // Buscar e-mail do titular
            UsuarioDto titular = sgrhService.buscarUsuarioPorTitulo(
                    responsavel.get().titularTitulo()
            ).orElse(null);
            
            if (titular == null || titular.email() == null || titular.email().isBlank()) {
                log.warn("E-mail não encontrado para titular {} da unidade {}", 
                        responsavel.get().titularTitulo(), unidade.nome());
                return;
            }
            
            // Determinar tipo de e-mail baseado no tipo de unidade
            String assunto;
            String htmlBody;
            String tipoUnidade = unidade.tipo();
            
            if ("OPERACIONAL".equalsIgnoreCase(tipoUnidade)) {
                // E-mail para unidade operacional
                assunto = "Processo Iniciado - " + processo.getDescricao();
                htmlBody = emailTemplateService.criarEmailProcessoIniciado(
                        unidade.nome(),
                        processo.getDescricao(),
                        processo.getTipo(),
                        subprocesso.getDataLimiteEtapa1()
                );
                
            } else if ("INTERMEDIARIA".equalsIgnoreCase(tipoUnidade)) {
                // E-mail para unidade intermediária
                assunto = "Processo Iniciado em Unidades Subordinadas - " + processo.getDescricao();
                htmlBody = criarEmailUnidadeIntermediaria(
                        unidade.nome(),
                        processo.getDescricao(),
                        processo.getTipo(),
                        subprocesso.getDataLimiteEtapa1()
                );
                
            } else if ("INTEROPERACIONAL".equalsIgnoreCase(tipoUnidade)) {
                // E-mail para unidade interoperacional (combina os dois tipos)
                assunto = "Processo Iniciado - " + processo.getDescricao();
                htmlBody = criarEmailUnidadeInteroperacional(
                        unidade.nome(),
                        processo.getDescricao(),
                        processo.getTipo(),
                        subprocesso.getDataLimiteEtapa1()
                );
                
            } else {
                log.warn("Tipo de unidade desconhecido: {} (unidade={})", 
                        tipoUnidade, unidadeCodigo);
                return;
            }
            
            // Enviar e-mail
            emailService.enviarEmailHtml(
                    titular.email(),
                    assunto,
                    htmlBody
            );
            
            log.info("E-mail enviado para unidade {} ({}) - Destinatário: {} ({})", 
                    unidade.sigla(), tipoUnidade, titular.nome(), titular.email());
            
            // Se houver substituto com e-mail, também envia
            if (responsavel.get().substitutoTitulo() != null) {
                enviarEmailParaSubstituto(responsavel.get().substitutoTitulo(), 
                        assunto, htmlBody, unidade.nome());
            }
            
        } catch (Exception e) {
            log.error("Erro ao enviar e-mail para unidade {}: {}", 
                    unidadeCodigo, e.getMessage(), e);
            // Não interrompe o fluxo se o e-mail falhar
        }
    }
    
    /**
     * Envia e-mail para o substituto da unidade.
     */
    private void enviarEmailParaSubstituto(String substitutoTitulo, String assunto, 
                                           String htmlBody, String nomeUnidade) {
        try {
            UsuarioDto substituto = sgrhService.buscarUsuarioPorTitulo(substitutoTitulo)
                    .orElse(null);
            
            if (substituto != null && substituto.email() != null && !substituto.email().isBlank()) {
                emailService.enviarEmailHtml(
                        substituto.email(),
                        assunto,
                        htmlBody
                );
                
                log.info("E-mail enviado para substituto da unidade {} - Destinatário: {} ({})", 
                        nomeUnidade, substituto.nome(), substituto.email());
            }
        } catch (Exception e) {
            log.warn("Erro ao enviar e-mail para substituto da unidade {}: {}", 
                    nomeUnidade, e.getMessage());
        }
    }
    
    /**
     * Cria HTML de e-mail específico para unidades intermediárias.
     */
    private String criarEmailUnidadeIntermediaria(String nomeUnidade, String nomeProcesso, 
                                                   String tipoProcesso, java.time.LocalDate dataLimite) {
        String dataFormatada = dataLimite != null ? 
                String.format("%02d/%02d/%d", 
                        dataLimite.getDayOfMonth(), 
                        dataLimite.getMonthValue(), 
                        dataLimite.getYear()) : 
                "a definir";
        
        String conteudo = String.format("""
                        <p>Um novo processo de <strong>%s</strong> foi iniciado em unidades subordinadas à sua unidade.</p>
                        
                        <div style="background-color: #f0f8ff; padding: 15px; margin: 15px 0; border-left: 4px solid #0066cc;">
                            <p style="margin: 5px 0;"><strong>Unidade:</strong> %s</p>
                            <p style="margin: 5px 0;"><strong>Processo:</strong> %s</p>
                            <p style="margin: 5px 0;"><strong>Tipo:</strong> %s</p>
                            <p style="margin: 5px 0;"><strong>Data limite para etapa 1:</strong> %s</p>
                        </div>
                        
                        <p><strong>Informações importantes:</strong></p>
                        <ul>
                            <li>As unidades subordinadas já podem iniciar o cadastro de atividades e conhecimentos</li>
                            <li>À medida que os cadastros forem disponibilizados, será possível visualizar e realizar a validação</li>
                            <li>Acompanhe o andamento no sistema SGC</li>
                        </ul>
                        
                        <p style="margin-top: 20px;">
                            <a href="https://sgc.tre-pe.jus.br"
                               style="background-color: #0066cc; color: white; padding: 10px 20px;
                                      text-decoration: none; border-radius: 5px; display: inline-block;">
                                Acompanhar Processo
                            </a>
                        </p>
                        """,
            tipoProcesso, nomeUnidade, nomeProcesso, tipoProcesso, dataFormatada);
        
        return emailTemplateService.criarTemplateBase(
                "Processo Iniciado em Unidades Subordinadas", conteudo);
    }
    
    /**
     * Cria HTML de e-mail específico para unidades interoperacionais.
     */
    private String criarEmailUnidadeInteroperacional(String nomeUnidade, String nomeProcesso, 
                                                      String tipoProcesso, java.time.LocalDate dataLimite) {
        String dataFormatada = dataLimite != null ? 
                String.format("%02d/%02d/%d", 
                        dataLimite.getDayOfMonth(), 
                        dataLimite.getMonthValue(), 
                        dataLimite.getYear()) : 
                "a definir";
        
        String conteudo = String.format("""
                        <p>Um novo processo de <strong>%s</strong> foi iniciado para sua unidade.</p>
                        
                        <div style="background-color: #f0f8ff; padding: 15px; margin: 15px 0; border-left: 4px solid #0066cc;">
                            <p style="margin: 5px 0;"><strong>Unidade:</strong> %s</p>
                            <p style="margin: 5px 0;"><strong>Processo:</strong> %s</p>
                            <p style="margin: 5px 0;"><strong>Tipo:</strong> %s</p>
                            <p style="margin: 5px 0;"><strong>Data limite para etapa 1:</strong> %s</p>
                        </div>
                        
                        <div style="background-color: #fff3cd; padding: 15px; margin: 15px 0; border-left: 4px solid #ffc107;">
                            <p style="margin: 0;"><strong>⚠ Atenção:</strong> Sua unidade é do tipo <strong>Interoperacional</strong></p>
                        </div>
                        
                        <p><strong>Você deverá realizar DUAS ações:</strong></p>
                        <ol>
                            <li><strong>Como unidade operacional:</strong> Realizar o cadastro de atividades e conhecimentos da sua própria unidade</li>
                            <li><strong>Como unidade intermediária:</strong> Validar os mapas das unidades subordinadas quando forem disponibilizados</li>
                        </ol>
                        
                        <p style="margin-top: 20px;">
                            <a href="https://sgc.tre-pe.jus.br"
                               style="background-color: #0066cc; color: white; padding: 10px 20px;
                                      text-decoration: none; border-radius: 5px; display: inline-block;">
                                Acessar Sistema
                            </a>
                        </p>
                        """,
            tipoProcesso, nomeUnidade, nomeProcesso, tipoProcesso, dataFormatada);
        
        return emailTemplateService.criarTemplateBase(
                "Processo Iniciado - Unidade Interoperacional", conteudo);
    }
}