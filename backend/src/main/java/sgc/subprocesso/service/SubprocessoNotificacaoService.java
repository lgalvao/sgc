package sgc.subprocesso.service;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.*;
import org.springframework.stereotype.*;
import org.thymeleaf.context.*;
import org.thymeleaf.spring6.*;
import sgc.alerta.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;

import java.time.*;
import java.time.format.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubprocessoNotificacaoService {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AlertaFacade alertaService;
    private final NotificacaoEmailService notificacaoEmailService;
    private final ResponsavelUnidadeService responsavelService;
    private final UsuarioService usuarioService;
    private final SpringTemplateEngine templateEngine;
    private final UnidadeHierarquiaService unidadeHierarquiaService;
    private final UnidadeService unidadeService;

    public void notificarTransicao(NotificacaoCommand cmd) {
        TipoTransicao tipoTransicao = cmd.tipoTransicao();
        if (tipoTransicao.geraAlerta()) {
            Subprocesso subprocesso = cmd.subprocesso();
            String sigla = subprocesso.getUnidade().getSigla();
            String descricao = tipoTransicao.formatarAlerta(sigla);
            alertaService.criarAlertaTransicao(subprocesso.getProcesso(), descricao, cmd.unidadeOrigem(), cmd.unidadeDestino());
        }
        if (tipoTransicao.enviaEmail()) {
            notificarMovimentacaoEmail(cmd);
        }
    }

    private void notificarMovimentacaoEmail(NotificacaoCommand cmd) {
        TipoTransicao tipoTransicao = cmd.tipoTransicao();
        if (!tipoTransicao.enviaEmail()) return;

        Map<String, Object> variaveis = criarVariaveisTemplateDireto(cmd);
        enviarNotificacaoDireta(cmd, variaveis);

        if (tipoTransicao.notificacaoSuperior()) {
            enviarNotificacaoSuperior(cmd, variaveis);
        }
    }

    public String getEmailUnidade(Unidade unidade) {
        return "%s@tre-pe.jus.br".formatted(unidade.getSigla().toLowerCase());
    }

    private void enviarNotificacaoDireta(NotificacaoCommand cmd, Map<String, Object> variaveis) {
        TipoTransicao tipo = cmd.tipoTransicao();
        String templateEmail = obterTemplateObrigatorio(tipo.getTemplateEmail(), "e-mail direto");

        String assunto = criarAssunto(tipo, cmd.subprocesso(), false);
        String corpo = processarTemplate(templateEmail, variaveis);
        String emailUnidade = getEmailUnidade(cmd.unidadeDestino());

        EmailGerado emailDireto = new EmailGerado(emailUnidade, assunto, corpo, "direto");
        enfileirarEmail(cmd, emailDireto);
        notificarResponsavelPessoal(cmd, cmd.unidadeDestino(), emailDireto);
    }

    private void notificarResponsavelPessoal(NotificacaoCommand cmd, Unidade unidade, EmailGerado emailDireto) {
        UnidadeResponsavelDto responsavel;
        try {
            responsavel = responsavelService.buscarResponsavelUnidade(unidade.getCodigo());
        } catch (sgc.comum.erros.ErroEntidadeNaoEncontrada ex) {
            log.warn("Responsavel nao encontrado para unidade {}; email pessoal nao enviado.", unidade.getCodigo());
            return;
        }

        if (responsavel.substitutoTitulo() != null) {
            usuarioService.buscarOpt(responsavel.substitutoTitulo()).ifPresent(u -> {
                if (!u.getEmail().isBlank()) {
                    enfileirarEmail(cmd, new EmailGerado(u.getEmail(), emailDireto.assunto(), emailDireto.corpo(), "responsavel"));
                }
            });
        }
    }

    private void enviarNotificacaoSuperior(NotificacaoCommand cmd, Map<String, Object> variaveisBase) {
        String assunto = criarAssunto(cmd.tipoTransicao(), cmd.subprocesso(), true);
        Long codigoPai = unidadeHierarquiaService.buscarCodigoPai(cmd.unidadeOrigem().getCodigo());
        if (codigoPai == null || Objects.equals(codigoPai, cmd.unidadeDestino().getCodigo())) return;

        UnidadeResumoLeitura superior = unidadeService.buscarResumosPorCodigos(List.of(codigoPai)).stream()
                .findFirst()
                .orElse(null);
        if (superior == null) return;

        Map<String, Object> variaveis = new HashMap<>(variaveisBase);
        variaveis.put("siglaUnidadeSuperior", superior.sigla());

        String templateEmailSuperior = obterTemplateObrigatorio(cmd.tipoTransicao().getTemplateEmailSuperior(), "e-mail superior");
        String corpo = processarTemplate(templateEmailSuperior, variaveis);
        String emailSuperior = "%s@tre-pe.jus.br".formatted(superior.sigla().toLowerCase());

        enfileirarEmail(cmd, new EmailGerado(emailSuperior, assunto, corpo, "superior"));
    }

    private void enfileirarEmail(NotificacaoCommand cmd, EmailGerado email) {
        notificacaoEmailService.enfileirar(new EnfileirarNotificacaoEmailCommand(
                cmd.subprocesso(),
                cmd.tipoTransicao().name(),
                email.destinatario(),
                email.assunto(),
                email.corpo(),
                chaveIdempotencia(cmd, email)
        ));
    }

    private String chaveIdempotencia(NotificacaoCommand cmd, EmailGerado email) {
        String destinatario = email.destinatario().trim().toLowerCase(Locale.ROOT);
        return "subprocesso:%s:transicao:%s:destinatario:%s:tipo:%s".formatted(
                cmd.subprocesso().getCodigo(),
                cmd.tipoTransicao().name(),
                destinatario,
                email.tipo()
        );
    }

    private Map<String, Object> criarVariaveisTemplateDireto(NotificacaoCommand cmd) {
        Map<String, Object> variaveis = new HashMap<>();

        Subprocesso subprocesso = cmd.subprocesso();
        Unidade unidade = subprocesso.getUnidade();
        variaveis.put("siglaUnidade", unidade.getSigla());
        variaveis.put("nomeUnidade", unidade.getNome());

        Unidade unidadeOrigem = cmd.unidadeOrigem();
        variaveis.put("siglaUnidadeOrigem", unidadeOrigem.getSigla());
        variaveis.put("nomeUnidadeOrigem", unidadeOrigem.getNome());

        Unidade unidadeDestino = cmd.unidadeDestino();
        variaveis.put("siglaUnidadeDestino", unidadeDestino.getSigla());
        variaveis.put("nomeUnidadeDestino", unidadeDestino.getNome());

        Processo processo = subprocesso.getProcesso();
        variaveis.put("nomeProcesso", processo.getDescricao());
        variaveis.put("tipoProcesso", processo.getTipo().name());

        if (subprocesso.getDataLimiteEtapa1() != null) {
            variaveis.put("dataLimiteEtapa1", subprocesso.getDataLimiteEtapa1().format(DATE_FORMATTER));
        }

        LocalDateTime dataLimiteEtapa2 = subprocesso.getDataLimiteEtapa2();
        if (dataLimiteEtapa2 != null) {
            variaveis.put("dataLimiteEtapa2", dataLimiteEtapa2.format(DATE_FORMATTER));
            variaveis.put("dataLimiteValidacao", dataLimiteEtapa2.format(DATE_FORMATTER));
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

    private String obterTemplateObrigatorio(@Nullable String template, String contexto) {
        if (template == null || template.isBlank()) {
            throw new IllegalStateException("Template ausente para %s".formatted(contexto));
        }
        return template;
    }

    private record EmailGerado(String destinatario, String assunto, String corpo, String tipo) {
    }
}
