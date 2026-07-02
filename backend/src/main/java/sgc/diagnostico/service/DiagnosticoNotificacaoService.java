package sgc.diagnostico.service;

import lombok.*;
import lombok.extern.slf4j.*;
import org.jspecify.annotations.*;
import org.springframework.stereotype.*;
import sgc.alerta.*;
import sgc.alerta.model.*;
import sgc.comum.config.*;
import sgc.comum.erros.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;

import java.util.*;

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

    /**
     * Cria um alerta individual para o servidor no início do processo de diagnóstico (CDU-41 §14).
     * O e-mail de início é enviado para a unidade (responsável), não para o servidor individualmente.
     */
    public void criarAlertaInicioParaServidor(Subprocesso sp, Usuario servidor) {
        Unidade admin = unidadeService.buscarAdmin();
        alertaService.criarAlertaPessoal(
                sp.getProcesso(),
                admin,
                sp.getUnidade(),
                servidor.getTituloEleitoral(),
                "Início do processo"
        );
    }

    public void notificarAutoavaliacaoConcluida(Subprocesso sp, String servidorTitulo) {
        Usuario servidor = usuarioService.buscarOpt(servidorTitulo).orElse(null);
        String nomeServidor = servidor != null ? servidor.getNome() : servidorTitulo;
        Unidade unidade = sp.getUnidade();

        DestinatarioNotificacao destinatario = obterDestinatarioResponsavel(unidade);
        String assunto = AssuntosNotificacao.diagnosticoAutoavaliacaoConcluida(nomeServidor);
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

        String assunto = AssuntosNotificacao.DIAGNOSTICO_CONSENSO_DISPONIVEL;
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
        String assunto = AssuntosNotificacao.diagnosticoConsensoAprovado(nomeServidor);
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
        String assunto = AssuntosNotificacao.diagnosticoConcluido(unidadeSubprocesso.getSigla());
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
        String assunto = AssuntosNotificacao.diagnosticoDevolvido(unidadeSubprocesso.getSigla());
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
        String assunto = AssuntosNotificacao.diagnosticoAceito(unidadeSubprocesso.getSigla());
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

    public void notificarDiagnosticosAceitosEmBloco(java.util.List<Subprocesso> subprocessos, Unidade unidadeSuperior) {
        if (subprocessos.isEmpty()) {
            return;
        }

        DestinatarioNotificacao destinatario = obterDestinatarioResponsavel(unidadeSuperior);
        Subprocesso base = subprocessos.getFirst();
        List<String> siglasUnidades = subprocessos.stream()
                .map(Subprocesso::getUnidade)
                .map(Unidade::getSigla)
                .distinct()
                .sorted()
                .toList();
        String assunto = AssuntosNotificacao.DIAGNOSTICOS_ACEITOS_EM_BLOCO;
        String corpo = emailModelosService.criarEmailDiagnosticoAceitoEmBloco(
                unidadeSuperior.getSigla(),
                base.getProcesso().getDescricao(),
                siglasUnidades
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
                        String.join("-", siglasUnidades)
                ))
                .build());
    }

    public void criarAlertaDiagnosticosAceitosEmBloco(Processo processo, Unidade unidadeAnalise, Unidade unidadeSuperior) {
        alertaService.criarAlertaTransicao(
                processo,
                sgc.comum.Mensagens.ALERTA_DIAGNOSTICO_ACEITO_BLOCO,
                unidadeAnalise,
                unidadeSuperior
        );
    }

    public void notificarDiagnosticoHomologado(Subprocesso sp) {
        Unidade unidadeSubprocesso = sp.getUnidade();
        Unidade admin = unidadeRaiz();

        DestinatarioNotificacao destinatario = obterDestinatarioResponsavel(unidadeSubprocesso);
        String assunto = AssuntosNotificacao.diagnosticoHomologado(unidadeSubprocesso.getSigla());
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
            String substitutoTitulo = responsavel.substitutoTitulo();
            if (substitutoTitulo != null && !substitutoTitulo.isBlank()) {
                Usuario usuario = usuarioService.buscarOpt(substitutoTitulo).orElse(null);
                if (usuario != null && !usuario.getEmail().isBlank()) {
                    return new DestinatarioNotificacao(usuario.getEmail(), usuario.getTituloEleitoral(), usuario.getNome());
                }
            }
            return new DestinatarioNotificacao(emailUnidade(unidade), null, null);
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

    private record DestinatarioNotificacao(
            String email,
            @Nullable String usuarioTitulo,
            @Nullable String nome
    ) {
    }
}
