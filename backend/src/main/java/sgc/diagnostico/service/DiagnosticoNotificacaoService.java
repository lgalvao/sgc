package sgc.diagnostico.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import sgc.alerta.AlertaFacade;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class DiagnosticoNotificacaoService {
    private final AlertaFacade alertaFacade;
    private final NotificacaoService notificacaoService;
    private final ResponsavelUnidadeService responsavelService;
    private final UsuarioService usuarioService;
    private final UnidadeService unidadeService;
    private final ConfigAplicacao configAplicacao;

    public void notificarInicioProcessoServidor(Subprocesso sp, Usuario servidor) {
        if (servidor.getEmail() == null || servidor.getEmail().isBlank()) {
            log.warn("Servidor {} sem email para notificação de início de diagnóstico.", servidor.getTituloEleitoral());
            return;
        }

        String nomeProcesso = sp.getProcesso().getDescricao();
        String dataLimite = sp.getDataLimiteEtapa1().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String assunto = "SGC: Autoavaliação de diagnóstico de competências disponível";

        String corpo = """
                <p>Prezado(a) %s,</p>
                <p>O processo de diagnóstico de competências técnicas <strong>%s</strong> foi iniciado para a sua unidade (<strong>%s</strong>).</p>
                <p>A sua autoavaliação já está disponível para preenchimento no Sistema de Gestão de Competências.</p>
                <p>O prazo para conclusão desta etapa é <strong>%s</strong>.</p>
                <p>Acompanhe o processo no Sistema de Gestão de Competências (%s).</p>
                """.formatted(servidor.getNome(), nomeProcesso, sp.getUnidade().getSigla(), dataLimite, urlSistema());

        enfileirarNotificacao(sp, sp.getUnidade(),
                new DestinatarioNotificacao(servidor.getEmail(), servidor.getTituloEleitoral(), servidor.getNome()),
                TipoNotificacao.PROCESSO_INICIADO,
                assunto,
                corpo,
                "diagnostico:%d:inicio:servidor:%s".formatted(sp.getCodigo(), servidor.getTituloEleitoral()));

        alertaFacade.criarAlertaPessoal(
                servidor.getTituloEleitoral(),
                "Sua autoavaliação de diagnóstico no processo %s está disponível. Prazo: %s".formatted(nomeProcesso, dataLimite)
        );
    }

    public void notificarAutoavaliacaoConcluida(Subprocesso sp, String servidorTitulo) {
        Usuario servidor = usuarioService.buscarOpt(servidorTitulo).orElse(null);
        String nomeServidor = servidor != null ? servidor.getNome() : servidorTitulo;
        Unidade unidade = sp.getUnidade();

        DestinatarioNotificacao destinatario = obterDestinatarioResponsavel(unidade);
        String assunto = "SGC: Autoavaliação de %s submetida para análise".formatted(nomeServidor);
        String corpo = """
                <p>Prezado(a) responsável pela %s,</p>
                <p>O servidor %s concluiu a autoavaliação no processo %s.</p>
                <p>A análise já pode ser realizada no Sistema de Gestão de Competências (%s).</p>
                """.formatted(unidade.getSigla(), nomeServidor, sp.getProcesso().getDescricao(), urlSistema());

        enfileirarNotificacao(sp, unidade, destinatario, TipoNotificacao.DIAGNOSTICO_AUTOAVALIACAO_CONCLUIDA, assunto, corpo,
                "diagnostico:%d:autoavaliacao:%s".formatted(sp.getCodigo(), servidorTitulo));

        alertaFacade.criarAlertaTransicao(
                sp.getProcesso(),
                "Autoavaliação de %s submetida para análise".formatted(nomeServidor),
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

        String assunto = "SGC: Avaliação de consenso de %s disponível para validação".formatted(servidor.getNome());
        String corpo = """
                <p>Prezado(a) %s,</p>
                <p>A chefia da unidade %s registrou a avaliação de consenso do processo %s.</p>
                <p>O consenso já pode ser consultado e validado no Sistema de Gestão de Competências (%s).</p>
                """.formatted(servidor.getNome(), unidade.getSigla(), sp.getProcesso().getDescricao(), urlSistema());

        enfileirarNotificacao(sp, unidade,
                new DestinatarioNotificacao(servidor.getEmail(), servidor.getTituloEleitoral(), servidor.getNome()),
                TipoNotificacao.DIAGNOSTICO_CONSENSO_DISPONIVEL,
                assunto,
                corpo,
                "diagnostico:%d:consenso-disponivel:%s".formatted(sp.getCodigo(), servidorTitulo));

        alertaFacade.criarAlertaTransicao(
                sp.getProcesso(),
                "Avaliação de consenso de %s disponível para validação".formatted(servidor.getNome()),
                unidade,
                unidade
        );
    }

    public void notificarConsensoAprovado(Subprocesso sp, String servidorTitulo) {
        Usuario servidor = usuarioService.buscarOpt(servidorTitulo).orElse(null);
        String nomeServidor = servidor != null ? servidor.getNome() : servidorTitulo;
        Unidade unidade = sp.getUnidade();

        DestinatarioNotificacao destinatario = obterDestinatarioResponsavel(unidade);
        String assunto = "SGC: Avaliação de consenso de %s aprovada".formatted(nomeServidor);
        String corpo = """
                <p>Prezado(a) responsável pela %s,</p>
                <p>O servidor %s aprovou a avaliação de consenso do processo %s.</p>
                <p>Acompanhe o processo no Sistema de Gestão de Competências (%s).</p>
                """.formatted(unidade.getSigla(), nomeServidor, sp.getProcesso().getDescricao(), urlSistema());

        enfileirarNotificacao(sp, unidade, destinatario, TipoNotificacao.DIAGNOSTICOCONSENSO_APROVADO, assunto, corpo,
                "diagnostico:%d:consenso-aprovado:%s".formatted(sp.getCodigo(), servidorTitulo));

        alertaFacade.criarAlertaTransicao(
                sp.getProcesso(),
                "Avaliação de consenso de %s aprovada".formatted(nomeServidor),
                unidade,
                unidade
        );
    }

    public void notificarDiagnosticoConcluido(Subprocesso sp, Unidade unidadeSuperior) {
        Unidade unidadeSubprocesso = sp.getUnidade();
        DestinatarioNotificacao destinatario = obterDestinatarioResponsavel(unidadeSuperior);
        String assunto = "SGC: Diagnóstico da unidade %s concluído".formatted(unidadeSubprocesso.getSigla());
        String corpo = """
                <p>Prezado(a) responsável pela %s,</p>
                <p>O diagnóstico da unidade %s no processo %s foi concluído.</p>
                <p>A análise já pode ser realizada no Sistema de Gestão de Competências (%s).</p>
                """.formatted(unidadeSuperior.getSigla(), unidadeSubprocesso.getSigla(), sp.getProcesso().getDescricao(), urlSistema());

        enfileirarNotificacao(sp, unidadeSuperior, destinatario, TipoNotificacao.DIAGNOSTICO_CONCLUIDO, assunto, corpo,
                "diagnostico:%d:concluido:superior:%d".formatted(sp.getCodigo(), unidadeSuperior.getCodigo()));

        alertaFacade.criarAlertaTransicao(
                sp.getProcesso(),
                "Diagnóstico da unidade %s concluído".formatted(unidadeSubprocesso.getSigla()),
                unidadeSubprocesso,
                unidadeSuperior
        );
    }

    public void notificarDiagnosticoDevolvido(Subprocesso sp, Unidade unidadeAnalise, Unidade unidadeDevolucao) {
        Unidade unidadeSubprocesso = sp.getUnidade();
        DestinatarioNotificacao destinatario = obterDestinatarioResponsavel(unidadeDevolucao);
        String assunto = "SGC: Diagnóstico da unidade %s devolvido para ajustes".formatted(unidadeSubprocesso.getSigla());
        String corpo = """
                <p>Prezado(a) responsável pela %s,</p>
                <p>O diagnóstico da unidade %s no processo %s foi devolvido para ajustes.</p>
                <p>Acompanhe o processo no Sistema de Gestão de Competências (%s).</p>
                """.formatted(unidadeDevolucao.getSigla(), unidadeSubprocesso.getSigla(), sp.getProcesso().getDescricao(), urlSistema());

        enfileirarNotificacao(sp, unidadeDevolucao, destinatario, TipoNotificacao.DIAGNOSTICO_DEVOLVIDO, assunto, corpo,
                "diagnostico:%d:devolvido:destino:%d".formatted(sp.getCodigo(), unidadeDevolucao.getCodigo()));

        alertaFacade.criarAlertaTransicao(
                sp.getProcesso(),
                "Diagnóstico da unidade %s devolvido para ajustes".formatted(unidadeSubprocesso.getSigla()),
                unidadeAnalise,
                unidadeDevolucao
        );
    }

    public void notificarDiagnosticoAceito(Subprocesso sp, Unidade unidadeAnalise, Unidade unidadeSuperior) {
        Unidade unidadeSubprocesso = sp.getUnidade();
        DestinatarioNotificacao destinatario = obterDestinatarioResponsavel(unidadeSuperior);
        String assunto = "SGC: Diagnóstico da unidade %s submetido para análise".formatted(unidadeSubprocesso.getSigla());
        String corpo = """
                <p>Prezado(a) responsável pela %s,</p>
                <p>O diagnóstico da unidade %s no processo %s foi aceito e submetido para análise por essa unidade.</p>
                <p>A análise já pode ser realizada no Sistema de Gestão de Competências (%s).</p>
                """.formatted(unidadeSuperior.getSigla(), unidadeSubprocesso.getSigla(), sp.getProcesso().getDescricao(), urlSistema());

        enfileirarNotificacao(sp, unidadeSuperior, destinatario, TipoNotificacao.DIAGNOSTICO_ACEITO, assunto, corpo,
                "diagnostico:%d:aceito:superior:%d".formatted(sp.getCodigo(), unidadeSuperior.getCodigo()));

        alertaFacade.criarAlertaTransicao(
                sp.getProcesso(),
                "Diagnóstico da unidade %s submetido para análise".formatted(unidadeSubprocesso.getSigla()),
                unidadeAnalise,
                unidadeSuperior
        );
    }

    public void notificarDiagnosticoHomologado(Subprocesso sp) {
        Unidade unidadeSubprocesso = sp.getUnidade();
        Unidade admin = unidadeService.buscarAdmin();

        DestinatarioNotificacao destinatario = obterDestinatarioResponsavel(unidadeSubprocesso);
        String assunto = "SGC: Diagnóstico da unidade %s homologado".formatted(unidadeSubprocesso.getSigla());
        String corpo = """
                <p>Prezado(a) responsável pela %s,</p>
                <p>O diagnóstico da sua unidade no processo %s foi homologado.</p>
                """.formatted(unidadeSubprocesso.getSigla(), sp.getProcesso().getDescricao());

        enfileirarNotificacao(sp, unidadeSubprocesso, destinatario, TipoNotificacao.DIAGNOSTICO_HOMOLOGADO, assunto, corpo,
                "diagnostico:%d:homologado".formatted(sp.getCodigo()));

        alertaFacade.criarAlertaTransicao(
                sp.getProcesso(),
                "Diagnóstico da unidade %s homologado".formatted(unidadeSubprocesso.getSigla()),
                admin,
                unidadeSubprocesso
        );
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

    private record DestinatarioNotificacao(
            String email,
            @Nullable String usuarioTitulo,
            @Nullable String nome
    ) {
    }
}