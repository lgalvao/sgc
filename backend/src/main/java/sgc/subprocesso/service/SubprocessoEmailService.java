package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import sgc.alerta.EmailService;
import sgc.organizacao.OrganizacaoFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.dto.UnidadeResponsavelDto;
import sgc.organizacao.model.Unidade;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.TipoTransicao;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Comunicação relacionada a subprocessos via email.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubprocessoEmailService {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final OrganizacaoFacade organizacaoFacade;
    private final UsuarioFacade usuarioFacade;

    /**
     * Ponto de entrada para disparar todas as comunicações (e-mails) relacionadas a uma movimentação.
     */
    public void notificarMovimentacao(Subprocesso sp, TipoTransicao tipo,
                                            Unidade unidadeOrigem, Unidade unidadeDestino,
                                            String observacoes) {
        if (!tipo.enviaEmail())
            return;

        try {
            Map<String, Object> variaveis = criarVariaveisTemplateDireto(sp, unidadeOrigem, unidadeDestino, observacoes);
            
            // 1. Notificação Operacional (Unidade Destino - quem deve agir)
            enviarNotificacaoOperacional(sp, tipo, unidadeDestino, variaveis);

            // 2. Notificação de Acompanhamento (Unidades Superiores - quem deve monitorar)
            if (tipo.enviaEmailSuperior()) {
                enviarNotificacaoAcompanhamentoSuperior(unidadeOrigem, sp, tipo, variaveis, unidadeDestino);
            }

        } catch (Exception e) {
            log.error("Erro ao processar comunicações da movimentação {}: {}", tipo, e.getMessage(), e);
        }
    }

    private void enviarNotificacaoOperacional(Subprocesso sp, TipoTransicao tipo, Unidade unidadeDestino, Map<String, Object> variaveis) {
        String assunto = criarAssunto(tipo, sp, false);
        String corpo = processarTemplate(tipo.getTemplateEmail(), variaveis);
        
        String emailUnidade = String.format("%s@tre-pe.jus.br", unidadeDestino.getSigla().toLowerCase());
        emailService.enviarEmailHtml(emailUnidade, assunto, corpo);
        log.info("Notificação OPERACIONAL de '{}' enviada para unidade {}", tipo, unidadeDestino.getSigla());

        // Responsável pessoal (substituto) também recebe a operacional
        notificarResponsavelPessoal(unidadeDestino, assunto, corpo, tipo);
    }

    private void notificarResponsavelPessoal(Unidade unidade, String assunto, String corpo, TipoTransicao tipo) {
        UnidadeResponsavelDto responsavel = organizacaoFacade.buscarResponsavelUnidade(unidade.getCodigo());
        if (responsavel.substitutoTitulo() != null) {
            usuarioFacade.buscarUsuarioPorTitulo(responsavel.substitutoTitulo())
                .ifPresent(u -> {
                    if (u.getEmail() != null && !u.getEmail().isBlank()) {
                        emailService.enviarEmailHtml(u.getEmail(), assunto, corpo);
                        log.info("Notificação OPERACIONAL de '{}' enviada para e-mail pessoal de {}", tipo, u.getNome());
                    }
                });
        }
    }

    private void enviarNotificacaoAcompanhamentoSuperior(Unidade unidadeOrigem, Subprocesso sp, TipoTransicao tipo, Map<String, Object> variaveisBase, Unidade unidadeJaNotificada) {
        Unidade superior = unidadeOrigem.getUnidadeSuperior();
        String assunto = criarAssunto(tipo, sp, true);

        while (superior != null) {
            // Evita duplicidade se a unidade superior for a própria unidade de destino operacional
            if (unidadeJaNotificada != null && superior.getCodigo().equals(unidadeJaNotificada.getCodigo())) {
                superior = superior.getUnidadeSuperior();
                continue;
            }

            try {
                Map<String, Object> variaveis = new HashMap<>(variaveisBase);
                variaveis.put("siglaUnidadeSuperior", superior.getSigla());
                
                String corpo = processarTemplate(tipo.getTemplateEmailSuperior(), variaveis);
                String emailSuperior = String.format("%s@tre-pe.jus.br", superior.getSigla().toLowerCase());
                
                emailService.enviarEmailHtml(emailSuperior, assunto, corpo);
                log.info("Notificação de ACOMPANHAMENTO de '{}' enviada para unidade superior {}", tipo, superior.getSigla());
            } catch (Exception e) {
                log.warn("Falha ao notificar acompanhamento superior {}: {}", superior.getSigla(), e.getMessage());
            }
            superior = superior.getUnidadeSuperior();
        }
    }

    private Map<String, Object> criarVariaveisTemplateDireto(Subprocesso sp, 
                                                            Unidade unidadeOrigem, Unidade unidadeDestino, 
                                                            @Nullable String observacoes) {
        Map<String, Object> variaveis = new HashMap<>();

        variaveis.put("siglaUnidade", sp.getUnidade().getSigla());
        variaveis.put("nomeUnidade", sp.getUnidade().getNome());
        
        variaveis.put("siglaUnidadeOrigem", unidadeOrigem.getSigla());
        variaveis.put("nomeUnidadeOrigem", unidadeOrigem.getNome());
        variaveis.put("siglaUnidadeDestino", unidadeDestino.getSigla());
        variaveis.put("nomeUnidadeDestino", unidadeDestino.getNome());

        variaveis.put("nomeProcesso", sp.getProcesso().getDescricao());
        variaveis.put("tipoProcesso", sp.getProcesso().getTipo().name());

        if (sp.getDataLimiteEtapa1() != null) {
            variaveis.put("dataLimiteEtapa1", sp.getDataLimiteEtapa1().format(DATE_FORMATTER));
        }

        if (sp.getDataLimiteEtapa2() != null) {
            variaveis.put("dataLimiteEtapa2", sp.getDataLimiteEtapa2().format(DATE_FORMATTER));
            variaveis.put("dataLimiteValidacao", sp.getDataLimiteEtapa2().format(DATE_FORMATTER));
        }

        if (observacoes != null) {
            variaveis.put("observacoes", observacoes);
        }

        return variaveis;
    }

    private String criarAssunto(TipoTransicao tipo, Subprocesso sp, boolean paraSuperior) {
        String base = tipo.getDescricaoMovimentacao();
        if (paraSuperior) {
            return "SGC: %s - %s".formatted(base, sp.getUnidade().getSigla());
        }
        return "SGC: %s".formatted(base);
    }

    private String processarTemplate(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }
}
