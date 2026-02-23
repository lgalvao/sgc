package sgc.subprocesso.service.notificacao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import sgc.notificacao.EmailService;
import sgc.organizacao.OrganizacaoFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.dto.UnidadeResponsavelDto;
import sgc.organizacao.model.Unidade;
import sgc.subprocesso.eventos.TipoTransicao;
import sgc.subprocesso.model.Subprocesso;
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
     * Envia e-mail de transição diretamente (sem evento assíncrono).
     */
    public void enviarEmailTransicaoDireta(Subprocesso sp, TipoTransicao tipo,
                                            Unidade unidadeOrigem, Unidade unidadeDestino,
                                            String observacoes) {
        if (!tipo.enviaEmail())
            return;

        try {
            Map<String, Object> variaveis = criarVariaveisTemplateDireto(sp, unidadeOrigem, unidadeDestino, observacoes);
            String assunto = criarAssunto(tipo, sp);
            String corpo = processarTemplate(tipo.getTemplateEmail(), variaveis);

            // 1. Enviar para o e-mail da unidade (ex: sesel@tre-pe.jus.br)
            String emailUnidade = String.format("%s@tre-pe.jus.br", unidadeDestino.getSigla().toLowerCase());
            emailService.enviarEmailHtml(emailUnidade, assunto, corpo);
            log.info("E-mail enviado para {}", unidadeDestino.getSigla());

            // 2. Enviar para o responsável atual (substituto ou titular se solicitado)
            notificarResponsaveisPessoais(unidadeDestino, assunto, corpo);

            if (deveNotificarHierarquia(tipo)) {
                notificarHierarquia(unidadeOrigem, assunto, corpo);
            }

        } catch (Exception e) {
            log.error("Erro ao enviar e-mail de transição {}: {}", tipo, e.getMessage(), e);
        }
    }

    private void notificarResponsaveisPessoais(Unidade unidade, String assunto, String corpo) {
        UnidadeResponsavelDto responsavel = organizacaoFacade.buscarResponsavelUnidade(unidade.getCodigo());
        // responsavel nunca deve ser nulo. É invariante do sistema, garantido pelas views.

        // Se houver substituto, ele é o responsável atual e deve receber no seu e-mail pessoal
        if (responsavel.substitutoTitulo() != null) {
            usuarioFacade.buscarUsuarioPorTitulo(responsavel.substitutoTitulo())
                .ifPresent(u -> {
                    if (u.getEmail() != null && !u.getEmail().isBlank()) {
                        emailService.enviarEmailHtml(u.getEmail(), assunto, corpo);
                        log.info("E-mail pessoal enviado para substituto da unidade {}: {}", unidade.getSigla(), u.getEmail());
                    }
                });
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
        }

        if (observacoes != null) {
            variaveis.put("observacoes", observacoes);
            variaveis.put("motivo", observacoes);
        }

        return variaveis;
    }

    private String criarAssunto(TipoTransicao tipo, Subprocesso sp) {
        String siglaUnidade = sp.getUnidade().getSigla();

        return switch (tipo) {
            case CADASTRO_DISPONIBILIZADO, REVISAO_CADASTRO_DISPONIBILIZADA ->
                String.format("SGC: Cadastro de atividades da unidade %s disponibilizado", siglaUnidade);
            case CADASTRO_DEVOLVIDO, REVISAO_CADASTRO_DEVOLVIDA ->
                String.format("SGC: Cadastro de atividades da unidade %s devolvido para ajustes", siglaUnidade);
            case CADASTRO_ACEITO ->
                String.format("SGC: Cadastro de atividades da unidade %s aceito", siglaUnidade);
            case REVISAO_CADASTRO_ACEITA ->
                String.format("SGC: Revisão do cadastro de atividades e conhecimentos da %s submetido para análise", siglaUnidade);
            case MAPA_DISPONIBILIZADO ->
                String.format("SGC: Mapa de competências da unidade %s disponibilizado", siglaUnidade);
            case MAPA_SUGESTOES_APRESENTADAS -> String.format("SGC: Sugestões para o mapa da unidade %s", siglaUnidade);
            case MAPA_VALIDADO -> String.format("SGC: Mapa de competências da unidade %s validado", siglaUnidade);
            case MAPA_VALIDACAO_DEVOLVIDA ->
                String.format("SGC: Validação do mapa da unidade %s devolvida", siglaUnidade);
            case MAPA_VALIDACAO_ACEITA -> String.format("SGC: Validação do mapa da unidade %s aceita", siglaUnidade);
            default -> String.format("SGC: Notificação - %s", tipo.getDescricaoMovimentacao());
        };
    }

    private String processarTemplate(String templateName, Map<String, Object> variaveis) {
        Context context = new Context();
        context.setVariables(variaveis);
        return templateEngine.process(templateName, context);
    }

    private boolean deveNotificarHierarquia(TipoTransicao tipo) {
        return tipo == TipoTransicao.MAPA_DISPONIBILIZADO
                || tipo == TipoTransicao.CADASTRO_DISPONIBILIZADO
                || tipo == TipoTransicao.REVISAO_CADASTRO_DISPONIBILIZADA;
    }

    private void notificarHierarquia(Unidade unidadeOrigem, String assunto, String corpo) {
        Unidade superior = unidadeOrigem.getUnidadeSuperior();
        while (superior != null) {
            try {
                String emailSuperior = String.format("%s@tre-pe.jus.br", superior.getSigla().toLowerCase());
                emailService.enviarEmailHtml(emailSuperior, assunto, corpo);
                log.info("E-mail enviado para unidade superior {} ({})", superior.getSigla(), emailSuperior);
            } catch (Exception e) {
                log.warn("Falha ao enviar e-mail para {}: {}", superior.getSigla(), e.getMessage());
            }
            superior = superior.getUnidadeSuperior();
        }
    }
}
