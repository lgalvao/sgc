package sgc.subprocesso.service;

import lombok.*;
import lombok.extern.slf4j.*;
import org.jspecify.annotations.*;
import org.springframework.stereotype.*;
import org.thymeleaf.context.*;
import org.thymeleaf.spring6.*;
import sgc.alerta.*;
import sgc.alerta.model.*;
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
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AlertaFacade alertaFacade;
    private final NotificacaoService notificacaoService;
    private final ResponsavelUnidadeService responsavelService;
    private final UsuarioService usuarioService;
    private final SpringTemplateEngine templateEngine;
    private final UnidadeHierarquiaService unidadeHierarquiaService;
    private final UnidadeService unidadeService;

    public void registrarComunicacoesTransicao(NotificacaoCommand cmd) {
        TipoTransicao tipoTransicao = cmd.tipoTransicao();
        if (tipoTransicao.geraAlerta()) {
            criarAlertaTransicao(cmd);
        }
        if (tipoTransicao.enviaEmail()) {
            executarNotificacaoSemInterromperAlerta(() -> criarNotificacoesTransicao(cmd), "transicao", cmd.subprocesso().getCodigo());
        }
    }

    public void registrarAlertaTransicao(NotificacaoCommand cmd) {
        criarAlertaTransicao(cmd);
    }

    private void criarAlertaTransicao(NotificacaoCommand cmd) {
        Subprocesso subprocesso = cmd.subprocesso();
        String sigla = subprocesso.getUnidade().getSigla();
        String descricao = cmd.tipoTransicao().formatarAlerta(sigla);
        alertaFacade.criarAlertaTransicao(subprocesso.getProcesso(), descricao, cmd.unidadeOrigem(), cmd.unidadeDestino());
    }

    private void criarNotificacoesTransicao(NotificacaoCommand cmd) {
        Map<String, Object> variaveis = criarVariaveisTemplateDireto(cmd);
        criarNotificacaoDireta(cmd, variaveis);

        if (Boolean.FALSE.equals(cmd.notificarSuperior())) {
            return;
        }
        criarNotificacaoSuperior(cmd, variaveis);
    }

    public String getEmailUnidade(Unidade unidade) {
        return "%s@tre-pe.jus.br".formatted(unidade.getSigla().toLowerCase());
    }

    public void notificarAlteracaoDataLimite(Subprocesso sp, String novaDataFormatada, int etapa) {
        alertaFacade.criarAlertaAlteracaoDataLimite(
                sp.getProcesso(), sp.getUnidade(), novaDataFormatada, etapa);

        Map<String, Object> variaveis = new HashMap<>();
        String assunto = AssuntosNotificacao.dataLimiteAlterada();
        variaveis.put("titulo", assunto);
        variaveis.put("siglaUnidade", sp.getUnidade().getSigla());
        variaveis.put("nomeProcesso", sp.getProcesso().getDescricao());
        variaveis.put("novaData", novaDataFormatada);
        variaveis.put("etapa", etapa);

        String corpo = processarTemplate("data-limite-alterada", variaveis);
        String emailDestino = getEmailUnidade(sp.getUnidade());
        String chave = "subprocesso:%d:data-limite-alterada:etapa:%d:data:%s"
                .formatted(sp.getCodigo(), etapa, novaDataFormatada);

        executarNotificacaoSemInterromperAlerta(() -> {
            notificacaoService.enfileirar(EnfileirarNotificacaoCommand.builder()
                    .subprocesso(sp)
                    .tipoNotificacao(TipoNotificacao.DATA_LIMITE_ALTERADA)
                    .unidadeDestinoSigla(sp.getUnidade().getSigla())
                    .destinatario(emailDestino)
                    .assunto(assunto)
                    .corpoHtml(corpo)
                    .chaveIdempotencia(chave)
                    .build());
        }, "alteracao-data-limite", sp.getCodigo());
    }

    public void notificarHomologacaoMapa(Subprocesso sp) {
        criarNotificacaoHomologacao(sp, TipoTransicao.MAPA_HOMOLOGADO, TipoNotificacao.MAPA_HOMOLOGADO, "mapa-homologado");
    }

    public void notificarAceiteCadastroEmBloco(List<Subprocesso> subprocessos) {
        if (subprocessos.isEmpty()) {
            return;
        }
        subprocessos.forEach(this::criarNotificacaoDiretaAceiteCadastroBloco);
        agruparPorSuperiorImediata(subprocessos).forEach(this::criarNotificacaoConsolidadaAceiteCadastroBloco);
    }

    public void notificarDisponibilizacaoMapaEmBloco(List<Subprocesso> subprocessos) {
        if (subprocessos.isEmpty()) {
            return;
        }
        agruparPorSuperiorImediata(subprocessos).forEach(this::criarNotificacaoConsolidadaDisponibilizacaoMapaBloco);
    }

    public void notificarAceiteValidacaoEmBloco(List<Subprocesso> subprocessos) {
        if (subprocessos.isEmpty()) {
            return;
        }
        subprocessos.forEach(this::criarNotificacaoDiretaAceiteValidacaoBloco);
        agruparPorSuperiorImediata(subprocessos).forEach(this::criarNotificacaoConsolidadaAceiteValidacaoBloco);
    }

    private void criarNotificacaoDireta(NotificacaoCommand cmd, Map<String, Object> variaveis) {
        TipoTransicao tipo = cmd.tipoTransicao();
        String templateEmail = obterTemplateObrigatorio(tipo.getTemplateEmail(), "e-mail direto");

        String assunto = criarAssunto(tipo, cmd.subprocesso(), false);
        String corpo = processarTemplate(templateEmail, variaveis);
        String emailUnidade = getEmailUnidade(cmd.unidadeDestino());

        EmailGerado emailDireto = new EmailGerado(emailUnidade, assunto, corpo, OrigemNotificacao.DIRETO, cmd.unidadeDestino().getSigla(), null);
        criarNotificacao(cmd, emailDireto);
        notificarResponsavelPessoal(cmd, cmd.unidadeDestino(), emailDireto);
    }

    private void notificarResponsavelPessoal(NotificacaoCommand cmd, Unidade unidade, EmailGerado emailDireto) {
        responsavelService.buscarResponsavelUnidadeOpt(unidade.getCodigo())
                .ifPresent(responsavel -> {
                    String titulo = responsavel.substitutoTitulo();
                    if (titulo != null) {
                        usuarioService.buscarOpt(titulo).ifPresent(u -> {
                            if (!u.getEmail().isBlank()) {
                                criarNotificacao(cmd, new EmailGerado(u.getEmail(), emailDireto.assunto(), emailDireto.corpo(), OrigemNotificacao.RESPONSAVEL, unidade.getSigla(), titulo));
                            }
                        });
                    }
                });
    }

    private void criarNotificacaoSuperior(NotificacaoCommand cmd, Map<String, Object> variaveisBase) {
        String templateEmailSuperior = obterTemplateObrigatorio(
                cmd.tipoTransicao().getTemplateEmailSuperior(),
                "e-mail superior"
        );

        String assunto = criarAssunto(cmd.tipoTransicao(), cmd.subprocesso(), true);
        Long codigoUnidade = cmd.subprocesso().getUnidade().getCodigo();
        Long codigoSuperior = unidadeHierarquiaService.buscarCodigoPai(codigoUnidade);
        if (codigoSuperior == null) {
            return;
        }
        if (Objects.equals(codigoSuperior, cmd.unidadeDestino().getCodigo())) {
            return;
        }

        UnidadeResumoLeitura superior = unidadeService.buscarResumosPorCodigos(List.of(codigoSuperior)).stream()
                .findFirst()
                .orElse(null);
        if (superior == null) return;

        Map<String, Object> variaveis = new HashMap<>(variaveisBase);
        variaveis.put("siglaUnidadeSuperior", superior.sigla());
        String corpo = processarTemplate(templateEmailSuperior, variaveis);
        String emailSuperior = "%s@tre-pe.jus.br".formatted(superior.sigla().toLowerCase());

        criarNotificacao(cmd, new EmailGerado(emailSuperior, assunto, corpo, OrigemNotificacao.SUPERIOR, superior.sigla(), null));
    }

    private void criarNotificacao(NotificacaoCommand cmd, EmailGerado email) {
        criarNotificacao(cmd, email, TipoNotificacao.valueOf(cmd.tipoTransicao().name()));
    }

    private void criarNotificacao(NotificacaoCommand cmd, EmailGerado email, TipoNotificacao tipoNotificacao) {
        notificacaoService.enfileirar(EnfileirarNotificacaoCommand.builder()
                .subprocesso(cmd.subprocesso())
                .tipoNotificacao(tipoNotificacao)
                .unidadeDestinoSigla(email.unidadeSigla())
                .usuarioDestinoTitulo(email.usuarioTitulo())
                .destinatario(email.destinatario())
                .assunto(email.assunto())
                .corpoHtml(email.corpo())
                .chaveIdempotencia(chaveIdempotencia(cmd, email))
                .build());
    }

    private void criarNotificacaoComChave(NotificacaoComChaveCommand notificacaoComChave) {
        NotificacaoCommand cmd = notificacaoComChave.cmd();
        EmailGerado email = notificacaoComChave.email();
        notificacaoService.enfileirar(EnfileirarNotificacaoCommand.builder()
                .subprocesso(cmd.subprocesso())
                .tipoNotificacao(notificacaoComChave.tipoNotificacao())
                .unidadeDestinoSigla(email.unidadeSigla())
                .usuarioDestinoTitulo(email.usuarioTitulo())
                .destinatario(email.destinatario())
                .assunto(email.assunto())
                .corpoHtml(email.corpo())
                .chaveIdempotencia(chaveIdempotenciaComSufixo(cmd, email, notificacaoComChave.sufixoChave()))
                .build());
    }

    private void criarNotificacaoHomologacao(
            Subprocesso sp,
            TipoTransicao tipoTransicao,
            TipoNotificacao tipoNotificacao,
            String template
    ) {
        Unidade admin = unidadeService.buscarAdmin();
        Unidade unidadeDestino = sp.getUnidade();
        NotificacaoCommand cmd = NotificacaoCommand.builder()
                .subprocesso(sp)
                .tipoTransicao(tipoTransicao)
                .unidadeOrigem(admin)
                .unidadeDestino(unidadeDestino)
                .build();
        Map<String, Object> variaveis = criarVariaveisTemplateDireto(cmd);
        String assunto = criarAssunto(tipoTransicao, sp, false);
        String corpo = processarTemplate(template, variaveis);
        String emailUnidade = getEmailUnidade(unidadeDestino);

        EmailGerado emailDireto = new EmailGerado(
                emailUnidade,
                assunto,
                corpo,
                OrigemNotificacao.DIRETO,
                unidadeDestino.getSigla(),
                null
        );
        criarNotificacao(cmd, emailDireto, tipoNotificacao);
        notificarResponsavelPessoal(cmd, unidadeDestino, emailDireto);
    }

    private void criarNotificacaoDiretaAceiteCadastroBloco(Subprocesso sp) {
        Unidade unidade = sp.getUnidade();
        Unidade admin = unidadeService.buscarAdmin();
        TipoTransicao tipoTransicao = sp.getProcesso().getTipo() == TipoProcesso.REVISAO
                ? TipoTransicao.REVISAO_CADASTRO_ACEITA
                : TipoTransicao.CADASTRO_ACEITO;
        TipoNotificacao tipoNotificacao = sp.getProcesso().getTipo() == TipoProcesso.REVISAO
                ? TipoNotificacao.REVISAO_CADASTRO_ACEITA
                : TipoNotificacao.CADASTRO_ACEITO;
        String template = sp.getProcesso().getTipo() == TipoProcesso.REVISAO
                ? "revisao-cadastro-aceita-bloco-unidade"
                : "cadastro-aceito-bloco-unidade";
        NotificacaoCommand cmd = NotificacaoCommand.builder()
                .subprocesso(sp)
                .tipoTransicao(tipoTransicao)
                .unidadeOrigem(admin)
                .unidadeDestino(unidade)
                .build();
        Map<String, Object> variaveis = criarVariaveisTemplateDireto(cmd);
        String assunto = criarAssunto(tipoTransicao, sp, false);
        String corpo = processarTemplate(template, variaveis);
        EmailGerado email = new EmailGerado(getEmailUnidade(unidade), assunto, corpo, OrigemNotificacao.DIRETO, unidade.getSigla(), null);
        criarNotificacaoComChave(new NotificacaoComChaveCommand(cmd, email, tipoNotificacao, "bloco-direto"));
        notificarResponsavelPessoal(cmd, unidade, email);
    }

    private void criarNotificacaoConsolidadaAceiteCadastroBloco(Unidade superior, List<Subprocesso> subprocessos) {
        Subprocesso base = subprocessos.getFirst();
        Unidade admin = unidadeService.buscarAdmin();
        boolean revisao = base.getProcesso().getTipo() == TipoProcesso.REVISAO;
        TipoTransicao tipoTransicao = revisao ? TipoTransicao.REVISAO_CADASTRO_ACEITA : TipoTransicao.CADASTRO_ACEITO;
        TipoNotificacao tipoNotificacao = revisao ? TipoNotificacao.REVISAO_CADASTRO_ACEITA : TipoNotificacao.CADASTRO_ACEITO;
        NotificacaoCommand cmd = NotificacaoCommand.builder()
                .subprocesso(base)
                .tipoTransicao(tipoTransicao)
                .unidadeOrigem(admin)
                .unidadeDestino(superior)
                .build();
        Map<String, Object> variaveis = criarVariaveisConsolidacao(superior, subprocessos);
        String assunto = AssuntosNotificacao.cadastroAceitoBloco(revisao);
        String corpo = processarTemplate(
                revisao ? "revisao-cadastro-aceita-bloco-superior" : "cadastro-aceito-bloco-superior",
                variaveis
        );
        EmailGerado email = new EmailGerado(getEmailUnidade(superior), assunto, corpo, OrigemNotificacao.SUPERIOR, superior.getSigla(), null);
        criarNotificacaoComChave(new NotificacaoComChaveCommand(cmd, email, tipoNotificacao, "bloco-superior"));
    }

    private void criarNotificacaoConsolidadaDisponibilizacaoMapaBloco(Unidade superior, List<Subprocesso> subprocessos) {
        Subprocesso base = subprocessos.getFirst();
        Unidade admin = unidadeService.buscarAdmin();
        NotificacaoCommand cmd = NotificacaoCommand.builder()
                .subprocesso(base)
                .tipoTransicao(TipoTransicao.MAPA_DISPONIBILIZADO)
                .unidadeOrigem(admin)
                .unidadeDestino(superior)
                .observacoes("Disponibilização em bloco")
                .build();
        Map<String, Object> variaveis = criarVariaveisConsolidacao(superior, subprocessos);
        String assunto = AssuntosNotificacao.disponibilizacaoMapaBloco();
        String corpo = processarTemplate("mapa-disponibilizado-bloco-superior", variaveis);
        EmailGerado email = new EmailGerado(getEmailUnidade(superior), assunto, corpo, OrigemNotificacao.SUPERIOR, superior.getSigla(), null);
        criarNotificacaoComChave(new NotificacaoComChaveCommand(cmd, email, TipoNotificacao.MAPA_DISPONIBILIZADO, "bloco-superior"));
    }

    private void criarNotificacaoDiretaAceiteValidacaoBloco(Subprocesso sp) {
        Unidade unidade = sp.getUnidade();
        Unidade admin = unidadeService.buscarAdmin();
        NotificacaoCommand cmd = NotificacaoCommand.builder()
                .subprocesso(sp)
                .tipoTransicao(TipoTransicao.MAPA_VALIDACAO_ACEITA)
                .unidadeOrigem(admin)
                .unidadeDestino(unidade)
                .build();
        Map<String, Object> variaveis = criarVariaveisTemplateDireto(cmd);
        String assunto = AssuntosNotificacao.aceiteValidacaoBlocoDireto(unidade.getSigla());
        String corpo = processarTemplate("validacao-mapa-aceita-bloco-unidade", variaveis);
        EmailGerado email = new EmailGerado(getEmailUnidade(unidade), assunto, corpo, OrigemNotificacao.DIRETO, unidade.getSigla(), null);
        criarNotificacaoComChave(new NotificacaoComChaveCommand(cmd, email, TipoNotificacao.MAPA_VALIDACAO_ACEITA, "bloco-direto"));
        notificarResponsavelPessoal(cmd, unidade, email);
    }

    private void criarNotificacaoConsolidadaAceiteValidacaoBloco(Unidade superior, List<Subprocesso> subprocessos) {
        Subprocesso base = subprocessos.getFirst();
        Unidade admin = unidadeService.buscarAdmin();
        NotificacaoCommand cmd = NotificacaoCommand.builder()
                .subprocesso(base)
                .tipoTransicao(TipoTransicao.MAPA_VALIDACAO_ACEITA)
                .unidadeOrigem(admin)
                .unidadeDestino(superior)
                .build();
        Map<String, Object> variaveis = criarVariaveisConsolidacao(superior, subprocessos);
        String assunto = AssuntosNotificacao.aceiteValidacaoBlocoSuperior();
        String corpo = processarTemplate("validacao-mapa-aceita-bloco-superior", variaveis);
        EmailGerado email = new EmailGerado(getEmailUnidade(superior), assunto, corpo, OrigemNotificacao.SUPERIOR, superior.getSigla(), null);
        criarNotificacaoComChave(new NotificacaoComChaveCommand(cmd, email, TipoNotificacao.MAPA_VALIDACAO_ACEITA, "bloco-superior"));
    }

    private String chaveIdempotencia(NotificacaoCommand cmd, EmailGerado email) {
        String destinatario = email.destinatario().trim().toLowerCase(Locale.ROOT);
        return "subprocesso:%s:transicao:%s:destinatario:%s:origem:%s".formatted(
                cmd.subprocesso().getCodigo(),
                cmd.tipoTransicao().name(),
                destinatario,
                email.origem().name()
        );
    }

    private String chaveIdempotenciaComSufixo(NotificacaoCommand cmd, EmailGerado email, String sufixo) {
        return "%s:%s".formatted(chaveIdempotencia(cmd, email), sufixo);
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
        variaveis.put("siglaUnidadeSuperior", unidadeDestino.getSigla());
        variaveis.put("nomeUnidadeSuperior", unidadeDestino.getNome());

        Processo processo = subprocesso.getProcesso();
        variaveis.put("nomeProcesso", processo.getDescricao());
        variaveis.put("tipoProcesso", processo.getTipo().name());

        if (subprocesso.getDataLimiteEtapa1() != null) {
            variaveis.put("dataLimiteEtapa1", subprocesso.getDataLimiteEtapa1().format(FORMATO_DATA));
        }

        LocalDateTime dataLimiteEtapa2 = subprocesso.getDataLimiteEtapa2();
        if (dataLimiteEtapa2 != null) {
            variaveis.put("dataLimiteEtapa2", dataLimiteEtapa2.format(FORMATO_DATA));
            variaveis.put("dataLimiteValidacao", dataLimiteEtapa2.format(FORMATO_DATA));
        }

        String observacoes = Objects.requireNonNullElse(cmd.observacoes(), "-");
        if (!"-".equals(observacoes)) {
            variaveis.put("observacoes", observacoes);
        }

        return variaveis;
    }

    private Map<String, Object> criarVariaveisConsolidacao(Unidade superior, List<Subprocesso> subprocessos) {
        Map<String, Object> variaveis = new HashMap<>();
        Subprocesso base = subprocessos.getFirst();
        variaveis.put("siglaUnidadeSuperior", superior.getSigla());
        variaveis.put("nomeProcesso", base.getProcesso().getDescricao());
        variaveis.put("siglasUnidades", subprocessos.stream()
                .map(sp -> sp.getUnidade().getSigla())
                .distinct()
                .sorted()
                .toList());
        if (base.getDataLimiteEtapa2() != null) {
            variaveis.put("dataLimiteValidacao", base.getDataLimiteEtapa2().format(FORMATO_DATA));
        }
        return variaveis;
    }

    private Map<Unidade, List<Subprocesso>> agruparPorSuperiorImediata(List<Subprocesso> subprocessos) {
        Map<Long, Unidade> superiores = new LinkedHashMap<>();
        Map<Long, List<Subprocesso>> agrupado = new LinkedHashMap<>();
        for (Subprocesso sp : subprocessos) {
            Unidade superior = sp.getUnidade().getUnidadeSuperior();
            if (superior == null) {
                continue;
            }
            superiores.putIfAbsent(superior.getCodigo(), superior);
            agrupado.computeIfAbsent(superior.getCodigo(), ignored -> new ArrayList<>()).add(sp);
        }

        Map<Unidade, List<Subprocesso>> resultado = new LinkedHashMap<>();
        agrupado.forEach((codigo, lista) -> resultado.put(superiores.get(codigo), lista));
        return resultado;
    }

    String criarAssunto(TipoTransicao tipo, Subprocesso sp, boolean paraSuperior) {
        return AssuntosNotificacao.subprocesso(tipo, sp.getUnidade().getSigla(), paraSuperior);
    }

    private String processarTemplate(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }

    String obterTemplateObrigatorio(@Nullable String template, String contexto) {
        if (template == null || template.isBlank()) {
            throw new IllegalStateException("Template ausente para %s".formatted(contexto));
        }
        return template;
    }

    private void executarNotificacaoSemInterromperAlerta(Runnable acaoNotificacao, String contexto, Long codigoSubprocesso) {
        try {
            acaoNotificacao.run();
        } catch (RuntimeException ex) {
            log.warn(
                    "Falha ao enviar notificação de e-mail ({}) para subprocesso {}. Fluxo de alerta será mantido.",
                    contexto,
                    codigoSubprocesso,
                    ex
            );
        }
    }

    private enum OrigemNotificacao {
        DIRETO, RESPONSAVEL, SUPERIOR
    }

    private record EmailGerado(String destinatario, String assunto, String corpo, OrigemNotificacao origem,
                               @Nullable String unidadeSigla, @Nullable String usuarioTitulo) {
    }

    private record NotificacaoComChaveCommand(
            NotificacaoCommand cmd,
            EmailGerado email,
            TipoNotificacao tipoNotificacao,
            String sufixoChave
    ) {
    }
}
