package sgc.diagnostico.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import sgc.alerta.EnfileirarNotificacaoCommand;
import sgc.alerta.NotificacaoService;
import sgc.alerta.model.TipoNotificacao;
import sgc.comum.config.ConfigAplicacao;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.organizacao.dto.UnidadeResponsavelDto;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.service.ResponsavelUnidadeService;
import sgc.organizacao.service.UnidadeService;
import sgc.organizacao.service.UsuarioService;
import sgc.subprocesso.model.Subprocesso;

import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiagnosticoNotificacaoService {
    private final DiagnosticoAlertaService alertaService;
    private final NotificacaoService notificacaoService;
    private final ResponsavelUnidadeService responsavelService;
    private final UsuarioService usuarioService;
    private final UnidadeService unidadeService;
    private final ConfigAplicacao configAplicacao;
    private final sgc.alerta.EmailModelosService emailModelosService;

    public void notificarInicioProcessoServidor(Subprocesso sp, Usuario servidor) {
        if (servidor.getEmail().isBlank()) {
            log.warn("Servidor {} sem email para notificação de início de diagnóstico.", servidor.getTituloEleitoral());
            return;
        }

        String nomeProcesso = sp.getProcesso().getDescricao();
        String assunto = "SGC: Autoavaliação de diagnóstico de competências disponível";

        String corpo = emailModelosService.criarEmailDiagnosticoInicioServidor(
                servidor.getNome(),
                nomeProcesso,
                sp.getUnidade().getSigla(),
                sp.getDataLimiteEtapa1(),
                urlSistema()
        );

        enfileirarNotificacao(sp, sp.getUnidade(),
                new DestinatarioNotificacao(servidor.getEmail(), servidor.getTituloEleitoral(), servidor.getNome()),
                TipoNotificacao.PROCESSO_INICIADO,
                assunto,
                corpo,
                "diagnostico:%d:inicio:servidor:%s".formatted(sp.getCodigo(), servidor.getTituloEleitoral()));
    }

    public void notificarAutoavaliacaoConcluida(Subprocesso sp, String servidorTitulo) {
        Usuario servidor = usuarioService.buscarOpt(servidorTitulo).orElse(null);
        String nomeServidor = servidor != null ? servidor.getNome() : servidorTitulo;
        Unidade unidade = sp.getUnidade();

        DestinatarioNotificacao destinatario = obterDestinatarioResponsavel(unidade);
        String assunto = "SGC: Autoavaliação concluída: %s".formatted(nomeServidor);
        String corpo = emailModelosService.criarEmailDiagnosticoAutoavaliacaoConcluida(
                unidade.getSigla(),
                nomeServidor,
                sp.getProcesso().getDescricao(),
                urlSistema()
        );

        enfileirarNotificacao(sp, unidade, destinatario, TipoNotificacao.DIAGNOSTICO_AUTOAVALIACAO_CONCLUIDA, assunto, corpo,
                "diagnostico:%d:autoavaliacao:%s".formatted(sp.getCodigo(), servidorTitulo));

        alertaService.criarAlertaTransicao(
                sp.getProcesso(),
                "Autoavaliação concluída: %s".formatted(nomeServidor),
                unidade,
                unidade
        );
    }

    public void notificarConsensoDisponivel(Subprocesso sp, String servidorTitulo) {
        Optional<Usuario> servidorOpt = usuarioService.buscarOpt(servidorTitulo);
        if (servidorOpt.isEmpty() || servidorOpt.get().getEmail().isBlank()) {
            log.warn("Servidor {} sem email para notificação de consenso disponível.", servidorTitulo);
            return;
        }
        Usuario servidor = servidorOpt.get();
        Unidade unidade = sp.getUnidade();

        String assunto = "SGC: Avaliação de consenso criada";
        String corpo = emailModelosService.criarEmailDiagnosticoConsensoDisponivel(
                servidor.getNome(),
                unidade.getSigla(),
                sp.getProcesso().getDescricao(),
                urlSistema()
        );

        enfileirarNotificacao(sp, unidade,
                new DestinatarioNotificacao(servidor.getEmail(), servidor.getTituloEleitoral(), servidor.getNome()),
                TipoNotificacao.DIAGNOSTICO_CONSENSO_DISPONIVEL,
                assunto,
                corpo,
                "diagnostico:%d:consenso-disponivel:%s".formatted(sp.getCodigo(), servidorTitulo));

        alertaService.criarAlertaPessoal(
                sp.getProcesso(),
                unidade,
                unidade,
                servidor.getTituloEleitoral(),
                "Avaliação de consenso criada"
        );
    }

    public void notificarConsensoAprovado(Subprocesso sp, String servidorTitulo) {
        Usuario servidor = usuarioService.buscarOpt(servidorTitulo).orElse(null);
        String nomeServidor = servidor != null ? servidor.getNome() : servidorTitulo;
        Unidade unidade = sp.getUnidade();

        DestinatarioNotificacao destinatario = obterDestinatarioResponsavel(unidade);
        String assunto = "SGC: Avaliação de consenso aprovada: %s".formatted(nomeServidor);
        String corpo = emailModelosService.criarEmailDiagnosticoConsensoAprovado(
                unidade.getSigla(),
                nomeServidor,
                sp.getProcesso().getDescricao(),
                urlSistema()
        );

        enfileirarNotificacao(sp, unidade, destinatario, TipoNotificacao.DIAGNOSTICO_CONSENSO_APROVADO, assunto, corpo,
                "diagnostico:%d:consenso-aprovado:%s".formatted(sp.getCodigo(), servidorTitulo));

        alertaService.criarAlertaTransicao(
                sp.getProcesso(),
                "Avaliação de consenso aprovada: %s".formatted(nomeServidor),
                unidade,
                unidade
        );
    }

    public void notificarDiagnosticoConcluido(Subprocesso sp, Unidade unidadeSuperior) {
        Unidade unidadeSubprocesso = sp.getUnidade();
        DestinatarioNotificacao destinatario = obterDestinatarioResponsavel(unidadeSuperior);
        String assunto = "SGC: Diagnóstico da unidade %s submetido para análise".formatted(unidadeSubprocesso.getSigla());
        String corpo = emailModelosService.criarEmailDiagnosticoConcluido(
                unidadeSuperior.getSigla(),
                unidadeSubprocesso.getSigla(),
                sp.getProcesso().getDescricao(),
                urlSistema()
        );

        enfileirarNotificacao(sp, unidadeSuperior, destinatario, TipoNotificacao.DIAGNOSTICO_CONCLUIDO, assunto, corpo,
                "diagnostico:%d:concluido:superior:%d".formatted(sp.getCodigo(), unidadeSuperior.getCodigo()));

        alertaService.criarAlertaTransicao(
                sp.getProcesso(),
                sgc.comum.Mensagens.ALERTA_DIAGNOSTICO_CONCLUIDO.formatted(unidadeSubprocesso.getSigla()),
                unidadeSubprocesso,
                unidadeSuperior
        );
    }

    public void notificarDiagnosticoDevolvido(
            Subprocesso sp,
            Unidade unidadeAnalise,
            Unidade unidadeDevolucao,
            @Nullable String observacao
    ) {
        Unidade unidadeSubprocesso = sp.getUnidade();
        DestinatarioNotificacao destinatario = obterDestinatarioResponsavel(unidadeDevolucao);
        String assunto = "SGC: Diagnóstico da unidade %s devolvido para ajustes".formatted(unidadeSubprocesso.getSigla());
        String corpo = emailModelosService.criarEmailDiagnosticoDevolvido(
                unidadeDevolucao.getSigla(),
                unidadeSubprocesso.getSigla(),
                sp.getProcesso().getDescricao(),
                observacao
        );

        enfileirarNotificacao(sp, unidadeDevolucao, destinatario, TipoNotificacao.DIAGNOSTICO_DEVOLVIDO, assunto, corpo,
                "diagnostico:%d:devolvido:destino:%d".formatted(sp.getCodigo(), unidadeDevolucao.getCodigo()));

        alertaService.criarAlertaTransicao(
                sp.getProcesso(),
                "Diagnóstico da unidade %s devolvido para ajustes".formatted(unidadeSubprocesso.getSigla()),
                unidadeAnalise,
                unidadeDevolucao
        );
    }

    public void notificarDiagnosticoAceito(Subprocesso sp, Unidade unidadeAnalise, Unidade unidadeSuperior) {
        Unidade unidadeSubprocesso = sp.getUnidade();
        DestinatarioNotificacao destinatario = obterDestinatarioResponsavel(unidadeSuperior);
        String assunto = "SGC: Diagnóstico da unidade %s aceito".formatted(unidadeSubprocesso.getSigla());
        String corpo = emailModelosService.criarEmailDiagnosticoAceito(
                unidadeSuperior.getSigla(),
                unidadeSubprocesso.getSigla(),
                sp.getProcesso().getDescricao()
        );

        enfileirarNotificacao(sp, unidadeSuperior, destinatario, TipoNotificacao.DIAGNOSTICO_ACEITO, assunto, corpo,
                "diagnostico:%d:aceito:superior:%d".formatted(sp.getCodigo(), unidadeSuperior.getCodigo()));

        alertaService.criarAlertaTransicao(
                sp.getProcesso(),
                sgc.comum.Mensagens.ALERTA_DIAGNOSTICO_ACEITO.formatted(unidadeSubprocesso.getSigla()),
                unidadeAnalise,
                unidadeSuperior
        );
    }

    public void notificarDiagnosticosAceitosEmBloco(java.util.List<Subprocesso> subprocessos) {
        if (subprocessos.isEmpty()) {
            return;
        }

        agruparPorSuperiorImediato(subprocessos).forEach((unidadeSuperior, subprocessosSuperior) -> {
            DestinatarioNotificacao destinatario = obterDestinatarioResponsavel(unidadeSuperior);
            Subprocesso base = subprocessosSuperior.getFirst();
            String assunto = "SGC: Diagnósticos submetidos para análise";
            String corpo = emailModelosService.criarEmailDiagnosticoAceitoEmBloco(
                    unidadeSuperior.getSigla(),
                    base.getProcesso().getDescricao(),
                    subprocessosSuperior.stream()
                            .map(Subprocesso::getUnidade)
                            .map(Unidade::getSigla)
                            .distinct()
                            .sorted()
                            .toList()
            );

            notificacaoService.enfileirar(EnfileirarNotificacaoCommand.builder()
                    .subprocesso(base)
                    .tipoNotificacao(TipoNotificacao.DIAGNOSTICO_ACEITO)
                    .usuarioDestinoTitulo(destinatario.usuarioTitulo())
                    .unidadeDestinoSigla(unidadeSuperior.getSigla())
                    .destinatario(destinatario.email())
                    .assunto(assunto)
                    .corpoHtml(corpo)
                    .chaveIdempotencia("diagnostico:%d:aceito:bloco:superior:%d:unidades:%s".formatted(
                            base.getProcesso().getCodigo(),
                            unidadeSuperior.getCodigo(),
                            subprocessosSuperior.stream()
                                    .map(Subprocesso::getUnidade)
                                    .map(Unidade::getSigla)
                                    .distinct()
                                    .sorted()
                                    .collect(Collectors.joining("-"))
                    ))
                    .build());
        });
    }

    public void notificarDiagnosticoHomologado(Subprocesso sp) {
        Unidade unidadeSubprocesso = sp.getUnidade();
        Unidade admin = unidadeRaiz();

        DestinatarioNotificacao destinatario = obterDestinatarioResponsavel(unidadeSubprocesso);
        String assunto = "SGC: Diagnóstico da unidade %s homologado".formatted(unidadeSubprocesso.getSigla());
        String corpo = emailModelosService.criarEmailDiagnosticoHomologado(
                unidadeSubprocesso.getSigla(),
                sp.getProcesso().getDescricao()
        );

        enfileirarNotificacao(sp, unidadeSubprocesso, destinatario, TipoNotificacao.DIAGNOSTICO_HOMOLOGADO, assunto, corpo,
                "diagnostico:%d:homologado".formatted(sp.getCodigo()));

        alertaService.criarAlertaTransicao(
                sp.getProcesso(),
                "Diagnóstico da unidade %s homologado".formatted(unidadeSubprocesso.getSigla()),
                admin,
                unidadeSubprocesso
        );
    }

    private Unidade unidadeRaiz() {
        return unidadeService.buscarAdmin();
    }

    private void enfileirarNotificacao(
            Subprocesso sp,
            Unidade unidadeDestino,
            DestinatarioNotificacao destinatario,
            TipoNotificacao tipo,
            String assunto,
            String corpoHtml,
            String chaveIdempotencia
    ) {
        notificacaoService.enfileirar(EnfileirarNotificacaoCommand.builder()
                .subprocesso(sp)
                .tipoNotificacao(tipo)
                .usuarioDestinoTitulo(destinatario.usuarioTitulo())
                .unidadeDestinoSigla(unidadeDestino.getSigla())
                .destinatario(destinatario.email())
                .assunto(assunto)
                .corpoHtml(corpoHtml)
                .chaveIdempotencia(chaveIdempotencia)
                .build());
    }

    private DestinatarioNotificacao obterDestinatarioResponsavel(Unidade unidade) {
        try {
            UnidadeResponsavelDto responsavel = responsavelService.buscarResponsavelUnidade(unidade.getCodigo());
            String titulo = responsavel.substitutoTitulo() != null
                    ? responsavel.substitutoTitulo()
                    : responsavel.titularTitulo();
            Usuario usuario = usuarioService.buscarOpt(titulo).orElse(null);
            if (usuario == null || usuario.getEmail().isBlank()) {
                return new DestinatarioNotificacao(emailUnidade(unidade), null, null);
            }
            return new DestinatarioNotificacao(usuario.getEmail(), usuario.getTituloEleitoral(), usuario.getNome());
        } catch (ErroEntidadeNaoEncontrada ex) {
            log.warn("Responsável não encontrado para unidade {}. Notificação será enviada para o e-mail da unidade.", unidade.getCodigo());
            return new DestinatarioNotificacao(emailUnidade(unidade), null, null);
        }
    }

    private String emailUnidade(Unidade unidade) {
        return "%s@tre-pe.jus.br".formatted(unidade.getSigla().toLowerCase(Locale.ROOT));
    }

    private String urlSistema() {
        String url = configAplicacao.isAmbienteTestes()
                ? configAplicacao.getUrlAcessoHom()
                : configAplicacao.getUrlAcessoProd();
        return url == null || url.isBlank() ? "http://localhost:5173" : url;
    }

    private java.util.Map<Unidade, java.util.List<Subprocesso>> agruparPorSuperiorImediato(java.util.List<Subprocesso> subprocessos) {
        java.util.Map<Long, Unidade> superiores = new java.util.LinkedHashMap<>();
        java.util.Map<Long, java.util.List<Subprocesso>> agrupado = new java.util.LinkedHashMap<>();

        for (Subprocesso subprocesso : subprocessos) {
            Unidade superior = subprocesso.getUnidade().getUnidadeSuperior();
            if (superior == null) {
                continue;
            }
            superiores.putIfAbsent(superior.getCodigo(), superior);
            agrupado.computeIfAbsent(superior.getCodigo(), ignorado -> new java.util.ArrayList<>()).add(subprocesso);
        }

        java.util.Map<Unidade, java.util.List<Subprocesso>> resultado = new java.util.LinkedHashMap<>();
        agrupado.forEach((codigoSuperior, subprocessosSuperior) -> resultado.put(superiores.get(codigoSuperior), subprocessosSuperior));
        return resultado;
    }

    private record DestinatarioNotificacao(
            String email,
            @Nullable String usuarioTitulo,
            @Nullable String nome
    ) {
    }
}
