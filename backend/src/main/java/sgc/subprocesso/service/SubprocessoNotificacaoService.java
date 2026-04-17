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
    private final EmailService emailService;
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

        emailService.enviarEmailHtml(emailUnidade, assunto, corpo);
        notificarResponsavelPessoal(cmd.unidadeDestino(), assunto, corpo);
    }

    private void notificarResponsavelPessoal(Unidade unidade, String assunto, String corpo) {
        UnidadeResponsavelDto responsavel;
        try {
            responsavel = responsavelService.buscarResponsavelUnidade(unidade.getCodigo());
        } catch (sgc.comum.erros.ErroEntidadeNaoEncontrada ex) {
            log.warn("Responsavel nao encontrado para unidade {}; email pessoal nao enviado.", unidade.getCodigo());
            return;
        }

        if (responsavel.substitutoTitulo() != null) {
            usuarioService.buscarOpt(responsavel.substitutoTitulo()).ifPresent(u -> {
                if (!u.getEmail().isBlank()) emailService.enviarEmailHtml(u.getEmail(), assunto, corpo);
            });
        }
    }

    private void enviarNotificacaoSuperior(NotificacaoCommand cmd, Map<String, Object> variaveisBase) {
        String assunto = criarAssunto(cmd.tipoTransicao(), cmd.subprocesso(), true);
        List<Long> codigosSuperiores = unidadeHierarquiaService.buscarCodigosSuperiores(cmd.unidadeOrigem().getCodigo());
        if (codigosSuperiores.isEmpty()) return;

        Map<Long, UnidadeResumoLeitura> mapaUnidades = unidadeService.buscarResumosPorCodigos(codigosSuperiores).stream()
                .collect(java.util.stream.Collectors.toMap(
                        UnidadeResumoLeitura::codigo,
                        unidade -> unidade,
                        (primeira, ignorada) -> primeira,
                        HashMap::new
                ));

        for (Long codigoSuperior : codigosSuperiores) {
            if (Objects.equals(codigoSuperior, cmd.unidadeDestino().getCodigo())) {
                continue;
            }

            UnidadeResumoLeitura superior = mapaUnidades.get(codigoSuperior);
            if (superior == null) continue;

            Map<String, Object> variaveis = new HashMap<>(variaveisBase);
            variaveis.put("siglaUnidadeSuperior", superior.sigla());

            String templateEmailSuperior = obterTemplateObrigatorio(cmd.tipoTransicao().getTemplateEmailSuperior(), "e-mail superior");
            String corpo = processarTemplate(templateEmailSuperior, variaveis);
            String emailSuperior = "%s@tre-pe.jus.br".formatted(superior.sigla().toLowerCase());

            emailService.enviarEmailHtml(emailSuperior, assunto, corpo);
        }
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
}
