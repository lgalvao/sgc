package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import sgc.alerta.AlertaFacade;
import sgc.alerta.EmailService;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.dto.UnidadeResponsavelDto;
import sgc.organizacao.service.ResponsavelUnidadeService;
import sgc.organizacao.UsuarioFacade;
import sgc.subprocesso.dto.NotificacaoCommand;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.TipoTransicao;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SubprocessoNotificacaoService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AlertaFacade alertaService;
    private final EmailService emailService;
    private final ResponsavelUnidadeService responsavelService;
    private final UsuarioFacade usuarioFacade;
    private final SpringTemplateEngine templateEngine;

    public void notificarTransicao(NotificacaoCommand cmd) {
        if (cmd.tipoTransicao().geraAlerta()) {
            String sigla = cmd.subprocesso().getUnidade().getSigla();
            String descricao = cmd.tipoTransicao().formatarAlerta(sigla);
            alertaService.criarAlertaTransicao(cmd.subprocesso().getProcesso(), descricao, cmd.unidadeOrigem(), cmd.unidadeDestino());
        }
        if (cmd.tipoTransicao().enviaEmail()) {
            notificarMovimentacaoEmail(cmd);
        }
    }

    private void notificarMovimentacaoEmail(NotificacaoCommand cmd) {
        if (!cmd.tipoTransicao().enviaEmail()) return;

        Map<String, Object> variaveis = criarVariaveisTemplateDireto(cmd);
        enviarNotificacaoDireta(cmd, variaveis);
        
        if (cmd.tipoTransicao().notificacaoSuperior()) {
            enviarNotificacaoSuperior(cmd, variaveis);
        }
    }

    private String getEmailUnidade(Unidade unidade) {
        String sigla = unidade.getSigla();
        return sigla.toLowerCase() + "@tre-pe.jus.br";
    }

    private void enviarNotificacaoDireta(NotificacaoCommand cmd, Map<String, Object> variaveis) {
        String assunto = criarAssunto(cmd.tipoTransicao(), cmd.subprocesso(), false);
        String corpo = processarTemplate(cmd.tipoTransicao().getTemplateEmail(), variaveis);

        String emailUnidade = getEmailUnidade(cmd.unidadeDestino());
        emailService.enviarEmailHtml(emailUnidade, assunto, corpo);

        notificarResponsavelPessoal(cmd.unidadeDestino(), assunto, corpo);
    }

    private void notificarResponsavelPessoal(Unidade unidade, String assunto, String corpo) {
        UnidadeResponsavelDto responsavel = responsavelService.buscarResponsavelUnidade(unidade.getCodigo());
        if (responsavel.substitutoTitulo() != null) {
            usuarioFacade.buscarUsuarioPorTitulo(responsavel.substitutoTitulo()).ifPresent(u -> {
                if (!u.getEmail().isBlank()) {
                    emailService.enviarEmailHtml(u.getEmail(), assunto, corpo);
                }
            });
        }
    }

    private void enviarNotificacaoSuperior(NotificacaoCommand cmd, Map<String, Object> variaveisBase) {
        Unidade superior = cmd.unidadeOrigem().getUnidadeSuperior();
        String assunto = criarAssunto(cmd.tipoTransicao(), cmd.subprocesso(), true);

        while (superior != null) {
            if (superior.getCodigo().equals(cmd.unidadeDestino().getCodigo())) {
                superior = superior.getUnidadeSuperior();
                continue;
            }

            Map<String, Object> variaveis = new HashMap<>(variaveisBase);
            variaveis.put("siglaUnidadeSuperior", superior.getSigla());

            String corpo = processarTemplate(cmd.tipoTransicao().getTemplateEmailSuperior(), variaveis);
            String emailSuperior = getEmailUnidade(superior);

            emailService.enviarEmailHtml(emailSuperior, assunto, corpo);
            superior = superior.getUnidadeSuperior();
        }
    }

    private Map<String, Object> criarVariaveisTemplateDireto(NotificacaoCommand cmd) {
        Map<String, Object> variaveis = new HashMap<>();

        variaveis.put("siglaUnidade", cmd.subprocesso().getUnidade().getSigla());
        variaveis.put("nomeUnidade", cmd.subprocesso().getUnidade().getNome());
        variaveis.put("siglaUnidadeOrigem", cmd.unidadeOrigem().getSigla());
        variaveis.put("nomeUnidadeOrigem", cmd.unidadeOrigem().getNome());
        variaveis.put("siglaUnidadeDestino", cmd.unidadeDestino().getSigla());
        variaveis.put("nomeUnidadeDestino", cmd.unidadeDestino().getNome());
        variaveis.put("nomeProcesso", cmd.subprocesso().getProcesso().getDescricao());
        variaveis.put("tipoProcesso", cmd.subprocesso().getProcesso().getTipo().name());

        if (cmd.subprocesso().getDataLimiteEtapa1() != null) {
            variaveis.put("dataLimiteEtapa1", cmd.subprocesso().getDataLimiteEtapa1().format(DATE_FORMATTER));
        }

        if (cmd.subprocesso().getDataLimiteEtapa2() != null) {
            variaveis.put("dataLimiteEtapa2", cmd.subprocesso().getDataLimiteEtapa2().format(DATE_FORMATTER));
            variaveis.put("dataLimiteValidacao", cmd.subprocesso().getDataLimiteEtapa2().format(DATE_FORMATTER));
        }

        String observacoes = Objects.requireNonNullElse(cmd.observacoes(), "-");
        if (!"-".equals(observacoes)) {
            variaveis.put("observacoes", observacoes);
        }

        return variaveis;
    }

    private String criarAssunto(TipoTransicao tipo, Subprocesso sp, boolean paraSuperior) {
        String base = tipo.getDescMovimentacao();

        return paraSuperior
                ? "SGC: %s - %s".formatted(base, sp.getUnidade().getSigla())
                : "SGC: %s".formatted(base);
    }

    private String processarTemplate(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }
}
