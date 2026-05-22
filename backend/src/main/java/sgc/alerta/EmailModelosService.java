package sgc.alerta;

import lombok.*;
import org.springframework.stereotype.*;
import org.thymeleaf.context.*;
import org.thymeleaf.spring6.*;

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

    public String criarAssuntoProcessoFinalizado(String nomeProcesso) {
        return AssuntosNotificacao.processoFinalizado(nomeProcesso);
    }

    public String criarAssuntoProcessoFinalizadoUnidadesSubordinadas(String nomeProcesso) {
        return AssuntosNotificacao.processoFinalizadoUnidadesSubordinadas(nomeProcesso);
    }

    public String criarAssuntoLembretePrazo(String nomeProcesso) {
        return AssuntosNotificacao.lembretePrazo(nomeProcesso);
    }

    /**
     * Gera o conteúdo HTML para o email que notifica a conclusão de um processo para uma unidade
     */
    public String criarEmailProcessoFinalizadoPorUnidade(String siglaUnidade, String nomeProcesso) {
        Context context = new Context();
        context.setVariable(VAR_TITULO, criarAssuntoProcessoFinalizado(nomeProcesso));
        context.setVariable(VAR_SIGLA_UNIDADE, siglaUnidade);
        context.setVariable(VAR_NOME_PROCESSO, nomeProcesso);

        return templateEngine.process("processo-finalizado-por-unidade", context);
    }

    /**
     * Gera o conteúdo HTML para o email notificando unidade intermediária sobre conclusão de
     * processo em suas unidades subordinadas.
     */
    public String criarEmailProcessoFinalizadoUnidadesSubordinadas(
            String siglaUnidade, String nomeProcesso, List<String> siglasUnidadesSubordinadas) {

        Context ctx = new Context();
        ctx.setVariable(VAR_TITULO, criarAssuntoProcessoFinalizadoUnidadesSubordinadas(nomeProcesso));
        ctx.setVariable(VAR_SIGLA_UNIDADE, siglaUnidade);
        ctx.setVariable(VAR_NOME_PROCESSO, nomeProcesso);
        ctx.setVariable("siglasUnidadesSubordinadas", siglasUnidadesSubordinadas);

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
