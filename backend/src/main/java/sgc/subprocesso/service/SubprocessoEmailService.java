package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import sgc.notificacao.NotificacaoEmailService;
import sgc.subprocesso.eventos.EventoTransicaoSubprocesso;
import sgc.subprocesso.eventos.TipoTransicao;
import sgc.subprocesso.model.Subprocesso;
import sgc.organizacao.model.Unidade;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Serviço responsável pelo envio de e-mails de notificação para transições de subprocesso.
 *
 * <p>Utiliza os metadados do {@link TipoTransicao} para determinar qual template utilizar
 * e processa os templates Thymeleaf correspondentes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubprocessoEmailService {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    private final NotificacaoEmailService notificacaoEmailService;
    private final TemplateEngine templateEngine;

    /**
     * Envia e-mail de notificação para uma transição de subprocesso.
     *
     * @param evento O evento de transição contendo os dados necessários
     */
    public void enviarEmailTransicao(EventoTransicaoSubprocesso evento) {
        TipoTransicao tipo = evento.getTipo();
        
        if (!tipo.enviaEmail()) {
            return;
        }

        Subprocesso sp = evento.getSubprocesso();
        Unidade unidadeOrigem = evento.getUnidadeOrigem();
        Unidade unidadeDestino = evento.getUnidadeDestino();

        try {
            Map<String, Object> variaveis = criarVariaveisTemplate(sp, evento);
            String assunto = criarAssunto(tipo, sp);
            String corpo = processarTemplate(tipo.getTemplateEmail(), variaveis);

            // Envia para a unidade destino
            if (unidadeDestino != null) {
                notificacaoEmailService.enviarEmail(unidadeDestino.getSigla(), assunto, corpo);
                log.info("E-mail enviado para {} - Transição: {}", unidadeDestino.getSigla(), tipo);
            }

            // Para alguns tipos, notifica também a hierarquia
            if (deveNotificarHierarquia(tipo) && unidadeOrigem != null) {
                notificarHierarquia(unidadeOrigem, assunto, corpo);
            }

        } catch (Exception e) {
            log.error("Erro ao enviar e-mail de transição {}: {}", tipo, e.getMessage(), e);
        }
    }

    private Map<String, Object> criarVariaveisTemplate(Subprocesso sp, EventoTransicaoSubprocesso evento) {
        Map<String, Object> variaveis = new HashMap<>();
        
        Unidade unidade = sp.getUnidade();
        if (unidade != null) {
            variaveis.put("siglaUnidade", unidade.getSigla());
            variaveis.put("nomeUnidade", unidade.getNome());
        }
        
        variaveis.put("nomeProcesso", sp.getProcesso().getDescricao());
        variaveis.put("tipoProcesso", sp.getProcesso().getTipo().name());
        
        if (sp.getDataLimiteEtapa1() != null) {
            variaveis.put("dataLimiteEtapa1", sp.getDataLimiteEtapa1().format(DATE_FORMATTER));
        }
        if (sp.getDataLimiteEtapa2() != null) {
            variaveis.put("dataLimiteEtapa2", sp.getDataLimiteEtapa2().format(DATE_FORMATTER));
        }
        
        // Adiciona observações/motivo se presente
        if (evento.getObservacoes() != null) {
            variaveis.put("observacoes", evento.getObservacoes());
            variaveis.put("motivo", evento.getObservacoes());
        }
        
        return variaveis;
    }

    private String criarAssunto(TipoTransicao tipo, Subprocesso sp) {
        String siglaUnidade = sp.getUnidade() != null ? sp.getUnidade().getSigla() : "N/A";
        
        return switch (tipo) {
            case CADASTRO_DISPONIBILIZADO, REVISAO_CADASTRO_DISPONIBILIZADA ->
                String.format("SGC: Cadastro de atividades da unidade %s disponibilizado", siglaUnidade);
            case CADASTRO_DEVOLVIDO, REVISAO_CADASTRO_DEVOLVIDA ->
                String.format("SGC: Cadastro de atividades da unidade %s devolvido para ajustes", siglaUnidade);
            case CADASTRO_ACEITO, REVISAO_CADASTRO_ACEITA ->
                String.format("SGC: Cadastro de atividades da unidade %s aceito", siglaUnidade);
            case MAPA_DISPONIBILIZADO ->
                String.format("SGC: Mapa de competências da unidade %s disponibilizado", siglaUnidade);
            case MAPA_SUGESTOES_APRESENTADAS ->
                String.format("SGC: Sugestões para o mapa da unidade %s", siglaUnidade);
            case MAPA_VALIDADO ->
                String.format("SGC: Mapa de competências da unidade %s validado", siglaUnidade);
            case MAPA_VALIDACAO_DEVOLVIDA ->
                String.format("SGC: Validação do mapa da unidade %s devolvida", siglaUnidade);
            case MAPA_VALIDACAO_ACEITA ->
                String.format("SGC: Validação do mapa da unidade %s aceita", siglaUnidade);
            default ->
                String.format("SGC: Notificação - %s", tipo.getDescricaoMovimentacao());
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
                notificacaoEmailService.enviarEmail(superior.getSigla(), assunto, corpo);
            } catch (Exception e) {
                log.warn("Falha ao enviar e-mail para {}: {}", superior.getSigla(), e.getMessage());
            }
            superior = superior.getUnidadeSuperior();
        }
    }
}
