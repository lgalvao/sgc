package sgc.alerta;

import lombok.*;
import org.jspecify.annotations.*;
import org.springframework.stereotype.*;
import org.thymeleaf.context.*;
import org.thymeleaf.spring6.*;
import sgc.processo.model.*;

import java.time.*;
import java.time.format.*;
import java.util.*;

/**
 * Cria templates HTML para diferentes tipos de e-mail.
 */
@Service
@RequiredArgsConstructor
public class EmailModelosService {
    private static final DateTimeFormatter FORMATADOR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String VAR_TITULO = "titulo";
    private static final String VAR_NOME_PROCESSO = "nomeProcesso";
    private static final String VAR_SIGLA_UNIDADE = "siglaUnidade";
    private static final String VAR_DATA_LIMITE = "dataLimite";

    private final SpringTemplateEngine templateEngine;

    /**
     * Gera o conteúdo HTML para o email de notificação de início de processo consolidado.
     */
    public String criarEmailInicioProcessoConsolidado(
            String siglaUnidade,
            String nomeProcesso,
            LocalDateTime dataLimite,
            String tipoProcesso,
            boolean isParticipante,
            List<String> siglasSubordinadas) {

        Context context = new Context();
        String assunto = criarAssuntoInicioProcesso(tipoProcesso, isParticipante);

        context.setVariable(VAR_TITULO, assunto);
        context.setVariable(VAR_SIGLA_UNIDADE, siglaUnidade);
        context.setVariable(VAR_NOME_PROCESSO, nomeProcesso);
        context.setVariable(VAR_DATA_LIMITE, dataLimite.format(FORMATADOR));
        context.setVariable("tipoProcesso", tipoProcesso);
        context.setVariable("isParticipante", isParticipante);
        context.setVariable("siglasSubordinadas", siglasSubordinadas);
        context.setVariable("hasSubordinadas", !siglasSubordinadas.isEmpty());

        return templateEngine.process("email-inicio-processo-consolidado", context);
    }

    public String criarAssuntoInicioProcesso(String tipoProcesso, boolean participante) {
        return AssuntosNotificacao.inicioProcesso(tipoProcesso, participante);
    }

    public String criarAssuntoProcessoFinalizado(TipoProcesso tipoProcesso) {
        return AssuntosNotificacao.processoFinalizado(tipoProcesso);
    }

    public String criarAssuntoProcessoFinalizadoUnidadesSubordinadas(TipoProcesso tipoProcesso) {
        return AssuntosNotificacao.processoFinalizadoUnidadesSubordinadas(tipoProcesso);
    }

    public String criarAssuntoLembretePrazo(String nomeProcesso) {
        return AssuntosNotificacao.lembretePrazo(nomeProcesso);
    }

    /**
     * Gera o conteúdo HTML para o email que notifica a conclusão de um processo para uma unidade
     */
    public String criarEmailProcessoFinalizadoPorUnidade(String siglaUnidade, String nomeProcesso, TipoProcesso tipoProcesso) {
        Context context = new Context();
        context.setVariable(VAR_TITULO, criarAssuntoProcessoFinalizado(tipoProcesso));
        context.setVariable(VAR_SIGLA_UNIDADE, siglaUnidade);
        context.setVariable(VAR_NOME_PROCESSO, nomeProcesso);
        context.setVariable("tipoProcesso", tipoProcesso.name());

        return templateEngine.process("processo-finalizado-por-unidade", context);
    }

    /**
     * Gera o conteúdo HTML para o email notificando unidade intermediária sobre conclusão de
     * processo em suas unidades subordinadas.
     */
    public String criarEmailProcessoFinalizadoUnidadesSubordinadas(
            String siglaUnidade,
            String nomeProcesso,
            List<String> siglasUnidadesSubordinadas,
            TipoProcesso tipoProcesso
    ) {

        Context ctx = new Context();
        ctx.setVariable(VAR_TITULO, criarAssuntoProcessoFinalizadoUnidadesSubordinadas(tipoProcesso));
        ctx.setVariable(VAR_SIGLA_UNIDADE, siglaUnidade);
        ctx.setVariable(VAR_NOME_PROCESSO, nomeProcesso);
        ctx.setVariable("siglasUnidadesSubordinadas", siglasUnidadesSubordinadas);
        ctx.setVariable("tipoProcesso", tipoProcesso.name());

        return templateEngine.process("processo-finalizado-unidades-subordinadas", ctx);
    }

    /**
     * Gera o conteúdo HTML para o email de lembrete de prazo.
     */
    public String criarEmailLembretePrazo(String siglaUnidade, String nomeProcesso, LocalDateTime dataLimite) {
        Context ctx = new Context();
        ctx.setVariable(VAR_TITULO, criarAssuntoLembretePrazo(nomeProcesso));
        ctx.setVariable(VAR_SIGLA_UNIDADE, siglaUnidade);
        ctx.setVariable(VAR_NOME_PROCESSO, nomeProcesso);
        ctx.setVariable(VAR_DATA_LIMITE, dataLimite.format(FORMATADOR));

        return templateEngine.process("lembrete-prazo", ctx);
    }

    public String criarEmailAtribuicaoTemporaria(EmailAtribuicaoTemporariaCommand cmd) {
        Context ctx = new Context();
        ctx.setVariable(VAR_TITULO, cmd.assunto());
        ctx.setVariable("nomeServidor", cmd.nomeServidor());
        ctx.setVariable(VAR_SIGLA_UNIDADE, cmd.siglaUnidade());
        ctx.setVariable("dataInicio", cmd.dataInicio().format(FORMATADOR));
        ctx.setVariable("dataTermino", cmd.dataTermino().format(FORMATADOR));
        ctx.setVariable("justificativa", cmd.justificativa());
        ctx.setVariable("urlSistema", cmd.urlSistema());

        return templateEngine.process("atribuicao-temporaria", ctx);
    }

    public String criarEmailDiagnosticoAutoavaliacaoConcluida(String siglaUnidade, String nomeServidor, String nomeProcesso, String urlSistema) {
        Context ctx = new Context();
        ctx.setVariable(VAR_TITULO, AssuntosNotificacao.diagnosticoAutoavaliacaoConcluida(nomeServidor));
        ctx.setVariable(VAR_SIGLA_UNIDADE, siglaUnidade);
        ctx.setVariable("nomeServidor", nomeServidor);
        ctx.setVariable(VAR_NOME_PROCESSO, nomeProcesso);
        ctx.setVariable("urlSistema", urlSistema);
        return templateEngine.process("diagnostico-autoavaliacao-concluida", ctx);
    }

    public String criarEmailDiagnosticoConsensoDisponivel(String nomeServidor, String siglaUnidade, String nomeProcesso, String urlSistema) {
        Context ctx = new Context();
        ctx.setVariable(VAR_TITULO, AssuntosNotificacao.DIAGNOSTICO_CONSENSO_DISPONIVEL);
        ctx.setVariable("nomeServidor", nomeServidor);
        ctx.setVariable(VAR_SIGLA_UNIDADE, siglaUnidade);
        ctx.setVariable(VAR_NOME_PROCESSO, nomeProcesso);
        ctx.setVariable("urlSistema", urlSistema);
        return templateEngine.process("diagnostico-consenso-disponivel", ctx);
    }

    public String criarEmailDiagnosticoConsensoAprovado(String siglaUnidade, String nomeServidor, String nomeProcesso, String urlSistema) {
        Context ctx = new Context();
        ctx.setVariable(VAR_TITULO, AssuntosNotificacao.diagnosticoConsensoAprovado(nomeServidor));
        ctx.setVariable(VAR_SIGLA_UNIDADE, siglaUnidade);
        ctx.setVariable("nomeServidor", nomeServidor);
        ctx.setVariable(VAR_NOME_PROCESSO, nomeProcesso);
        ctx.setVariable("urlSistema", urlSistema);
        return templateEngine.process("diagnostico-consenso-aprovado", ctx);
    }

    public String criarEmailDiagnosticoConcluido(String siglaUnidadeDestino, String siglaUnidadeOrigem, String nomeProcesso, String urlSistema) {
        Context ctx = new Context();
        ctx.setVariable(VAR_TITULO, AssuntosNotificacao.diagnosticoConcluido(siglaUnidadeOrigem));
        ctx.setVariable(VAR_SIGLA_UNIDADE, siglaUnidadeDestino);
        ctx.setVariable("siglaUnidadeSubordinada", siglaUnidadeOrigem);
        ctx.setVariable(VAR_NOME_PROCESSO, nomeProcesso);
        ctx.setVariable("urlSistema", urlSistema);
        return templateEngine.process("diagnostico-concluido", ctx);
    }

    public String criarEmailDiagnosticoDevolvido(
            String siglaUnidadeDestino,
            String siglaUnidadeOrigem,
            String nomeProcesso) {

        Context ctx = new Context();
        ctx.setVariable(VAR_TITULO, AssuntosNotificacao.diagnosticoDevolvido(siglaUnidadeOrigem));
        ctx.setVariable(VAR_SIGLA_UNIDADE, siglaUnidadeDestino);
        ctx.setVariable("siglaUnidadeSubordinada", siglaUnidadeOrigem);
        ctx.setVariable(VAR_NOME_PROCESSO, nomeProcesso);
        return templateEngine.process("diagnostico-devolvido", ctx);
    }

    public String criarEmailDiagnosticoAceito(String siglaUnidadeDestino, String siglaUnidadeOrigem, String nomeProcesso) {
        Context ctx = new Context();
        ctx.setVariable(VAR_TITULO, AssuntosNotificacao.diagnosticoAceito(siglaUnidadeOrigem));
        ctx.setVariable(VAR_SIGLA_UNIDADE, siglaUnidadeDestino);
        ctx.setVariable("siglaUnidadeSubordinada", siglaUnidadeOrigem);
        ctx.setVariable(VAR_NOME_PROCESSO, nomeProcesso);
        return templateEngine.process("diagnostico-aceito", ctx);
    }

    public String criarEmailDiagnosticoAceitoEmBloco(String siglaUnidadeDestino, String nomeProcesso, List<String> siglasUnidades) {
        Context ctx = new Context();
        ctx.setVariable(VAR_TITULO, AssuntosNotificacao.DIAGNOSTICOS_ACEITOS_EM_BLOCO);
        ctx.setVariable("siglaUnidadeSuperior", siglaUnidadeDestino);
        ctx.setVariable(VAR_NOME_PROCESSO, nomeProcesso);
        ctx.setVariable("siglasUnidades", siglasUnidades);
        return templateEngine.process("diagnostico-aceito-bloco-superior", ctx);
    }

    public String criarEmailDiagnosticoHomologado(String siglaUnidade, String nomeProcesso) {
        Context ctx = new Context();
        ctx.setVariable(VAR_TITULO, AssuntosNotificacao.diagnosticoHomologado(siglaUnidade));
        ctx.setVariable(VAR_SIGLA_UNIDADE, siglaUnidade);
        ctx.setVariable(VAR_NOME_PROCESSO, nomeProcesso);
        return templateEngine.process("diagnostico-homologado", ctx);
    }

    public record EmailAtribuicaoTemporariaCommand(
            String assunto,
            String nomeServidor,
            String siglaUnidade,
            LocalDateTime dataInicio,
            LocalDateTime dataTermino,
            String justificativa,
            String urlSistema
    ) {
    }
}
