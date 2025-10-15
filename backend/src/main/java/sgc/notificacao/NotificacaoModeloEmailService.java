package sgc.notificacao;

import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Serviço responsável por criar templates HTML para diferentes tipos de e-mail.
 * Cada método cria um template específico para um caso de uso do sistema.
 */
@Service
// TODO muitos strings fixos repetidos em toda esta classe
public class NotificacaoModeloEmailService {
    private static final DateTimeFormatter FORMATADOR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final SpringTemplateEngine templateEngine;

    NotificacaoModeloEmailService(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    /**
     * Template para notificar início de processo (CDU-04, CDU-05).
     */
    public String criarEmailDeProcessoIniciado(
            String nomeUnidade,
            String nomeProcesso,
            String tipoProcesso,
            LocalDate dataLimite) {

        Context context = new Context();
        context.setVariable("titulo", "Processo Iniciado - " + tipoProcesso);
        context.setVariable("nomeUnidade", nomeUnidade);
        context.setVariable("nomeProcesso", nomeProcesso);
        context.setVariable("tipoProcesso", tipoProcesso);
        context.setVariable("dataLimite", dataLimite.format(FORMATADOR));

        return templateEngine.process("processo-iniciado", context);
    }

    /**
     * Template para notificar disponibilização de cadastro (CDU-09, CDU-10).
     */
    public String criarEmailDeCadastroDisponibilizado(
            String nomeUnidade,
            String nomeProcesso,
            int quantidadeAtividades) {

        Context context = new Context();
        context.setVariable("titulo", "Cadastro Disponibilizado para Análise");
        context.setVariable("nomeUnidade", nomeUnidade);
        context.setVariable("nomeProcesso", nomeProcesso);
        context.setVariable("quantidadeAtividades", quantidadeAtividades);

        return templateEngine.process("cadastro-disponibilizado", context);
    }

    /**
     * Template para notificar devolução de cadastro (CDU-13).
     */
    public String criarEmailDeCadastroDevolvido(
            String nomeUnidade,
            String nomeProcesso,
            String motivo,
            String observacoes) {

        Context context = new Context();
        context.setVariable("titulo", "Cadastro Devolvido para Ajustes");
        context.setVariable("nomeUnidade", nomeUnidade);
        context.setVariable("nomeProcesso", nomeProcesso);
        context.setVariable("motivo", motivo);
        context.setVariable("observacoes", observacoes);

        return templateEngine.process("cadastro-devolvido", context);
    }

    /**
     * Template para notificar disponibilização de mapa (CDU-17).
     */
    public String criarEmailDeMapaDisponibilizado(
            String nomeUnidade,
            String nomeProcesso,
            LocalDate dataLimiteValidacao) {

        Context context = new Context();
        context.setVariable("titulo", "Mapa de Competências Disponibilizado");
        context.setVariable("nomeUnidade", nomeUnidade);
        context.setVariable("nomeProcesso", nomeProcesso);
        context.setVariable("dataLimiteValidacao", dataLimiteValidacao.format(FORMATADOR));

        return templateEngine.process("mapa-disponibilizado", context);
    }

    /**
     * Template para notificar validação de mapa (CDU-18).
     */
    public String criarEmailDeMapaValidado(
            String nomeUnidade,
            String nomeProcesso) {

        Context context = new Context();
        context.setVariable("titulo", "Mapa de Competências Validado");
        context.setVariable("nomeUnidade", nomeUnidade);
        context.setVariable("nomeProcesso", nomeProcesso);

        return templateEngine.process("mapa-validado", context);
    }

    /**
     * Template para notificar finalização de processo (CDU-21).
     */
    public String criarEmailDeProcessoFinalizado(
            String nomeProcesso,
            LocalDate dataFinalizacao,
            int quantidadeMapas) {

        Context context = new Context();
        context.setVariable("titulo", "Processo Finalizado - Mapas Vigentes");
        context.setVariable("nomeProcesso", nomeProcesso);
        context.setVariable("dataFinalizacao", dataFinalizacao.format(FORMATADOR));
        context.setVariable("quantidadeMapas", quantidadeMapas);

        return templateEngine.process("processo-finalizado", context);
    }

    /**
     * Template para notificar finalização de processo por unidade (CDU-21).
     */
    public String criarEmailDeProcessoFinalizadoPorUnidade(
            String siglaUnidade,
            String nomeProcesso) {

        Context context = new Context();
        context.setVariable("titulo", "SGC: Conclusão do processo " + nomeProcesso);
        context.setVariable("siglaUnidade", siglaUnidade);
        context.setVariable("nomeProcesso", nomeProcesso);

        return templateEngine.process("processo-finalizado-por-unidade", context);
    }

    /**
     * Template para notificar finalização de processo para unidades intermediárias (CDU-21).
     */
    public String criarEmailDeProcessoFinalizadoUnidadesSubordinadas(
            String siglaUnidade,
            String nomeProcesso,
            List<String> siglasUnidadesSubordinadas) {

        Context context = new Context();
        context.setVariable("titulo", "SGC: Conclusão do processo " + nomeProcesso + " em unidades subordinadas");
        context.setVariable("siglaUnidade", siglaUnidade);
        context.setVariable("nomeProcesso", nomeProcesso);
        context.setVariable("siglasUnidadesSubordinadas", siglasUnidadesSubordinadas);

        return templateEngine.process("processo-finalizado-unidades-subordinadas", context);
    }
}