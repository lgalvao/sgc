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

    private static final String TITULO_PROCESSO_CONCLUSAO_SGC = "Conclusão do processo ";
    private static final String TITULO_LEMBRETE_PRAZO = "SGC: Lembrete de prazo - ";

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
            boolean isParticipante,
            List<String> siglasSubordinadas) {

        Context context = new Context();
        String assunto = isParticipante
                ? "SGC: Início de processo de mapeamento de competências"
                : "SGC: Início de processo de mapeamento de competências em unidades subordinadas";

        context.setVariable(VAR_TITULO, assunto);
        context.setVariable(VAR_SIGLA_UNIDADE, siglaUnidade);
        context.setVariable(VAR_NOME_PROCESSO, nomeProcesso);
        context.setVariable(VAR_DATA_LIMITE, dataLimite.format(FORMATADOR));
        context.setVariable("isParticipante", isParticipante);
        context.setVariable("siglasSubordinadas", siglasSubordinadas);
        context.setVariable("hasSubordinadas", !siglasSubordinadas.isEmpty());

        return templateEngine.process("email-inicio-processo-consolidado", context);
    }

    /**
     * Gera o conteúdo HTML para o email que notifica a conclusão de um processo para uma unidade
     */
    public String criarEmailProcessoFinalizadoPorUnidade(String siglaUnidade, String nomeProcesso) {
        Context context = new Context();
        context.setVariable(
                VAR_TITULO, "%s%s".formatted(TITULO_PROCESSO_CONCLUSAO_SGC, nomeProcesso));
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
        ctx.setVariable(VAR_TITULO, "%s%s em unidades subordinadas".formatted(TITULO_PROCESSO_CONCLUSAO_SGC, nomeProcesso));
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
        ctx.setVariable(VAR_TITULO, TITULO_LEMBRETE_PRAZO + nomeProcesso);
        ctx.setVariable(VAR_SIGLA_UNIDADE, siglaUnidade);
        ctx.setVariable(VAR_NOME_PROCESSO, nomeProcesso);
        ctx.setVariable(VAR_DATA_LIMITE, dataLimite.format(FORMATADOR));

        return templateEngine.process("lembrete-prazo", ctx);
    }
}
