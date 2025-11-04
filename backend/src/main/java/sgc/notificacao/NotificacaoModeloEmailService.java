package sgc.notificacao;

import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Serviço responsável por criar templates HTML para diferentes tipos de e-mail.
 * Cada metodo cria um template específico para um caso de uso do sistema.
 */
@Service
public class NotificacaoModeloEmailService {
    private static final DateTimeFormatter FORMATADOR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Constantes para títulos de e-mail
    private static final String TITULO_PROCESSO_INICIADO = "Processo iniciado - ";
    private static final String TITULO_CADASTRO_DISPONIBILIZADO = "Cadastro disponibilizado para análise";
    private static final String TITULO_CADASTRO_DEVOLVIDO = "Cadastro devolvido para ajustes";
    private static final String TITULO_MAPA_DISPONIBILIZADO = "Mapa de competências disponibilizado";
    private static final String TITULO_MAPA_VALIDADO = "Mapa de Competências Validado";
    private static final String TITULO_PROCESSO_FINALIZADO = "Processo Finalizado - Mapas Vigentes";
    private static final String TITULO_PROCESSO_CONCLUSAO_SGC = "Processo finalizado: ";

    private final SpringTemplateEngine templateEngine;

    NotificacaoModeloEmailService(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    /**
     * Gera o conteúdo HTML para o email de notificação de início de processo.
     * <p>
     * Corresponde aos casos de uso CDU-04 e CDU-05.
     *
     * @param nomeUnidade  O nome da unidade notificada.
     * @param nomeProcesso O nome do processo iniciado.
     * @param tipoProcesso O tipo do processo (e.g., MAPEAMENTO, REVISAO).
     * @param dataLimite   A data limite para a conclusão da primeira etapa.
     * @return O conteúdo HTML do email renderizado pelo Thymeleaf.
     */
    public String criarEmailDeProcessoIniciado(
            String nomeUnidade,
            String nomeProcesso,
            String tipoProcesso,
            LocalDateTime dataLimite) {

        Context context = new Context();
        context.setVariable("titulo", TITULO_PROCESSO_INICIADO + tipoProcesso);
        context.setVariable("nomeUnidade", nomeUnidade);
        context.setVariable("nomeProcesso", nomeProcesso);
        context.setVariable("tipoProcesso", tipoProcesso);
        context.setVariable("dataLimite", dataLimite.format(FORMATADOR));

        return templateEngine.process("processo-iniciado", context);
    }

    /**
     * Gera o conteúdo HTML para o email que notifica a disponibilização de um
     * cadastro para análise.
     * <p>
     * Corresponde aos casos de uso CDU-09 e CDU-10.
     *
     * @param nomeUnidade          O nome da unidade que disponibilizou o cadastro.
     * @param nomeProcesso         O nome do processo associado.
     * @param quantidadeAtividades O número de atividades registradas.
     */
    public void criarEmailCadastroDisponibilizado(
            String nomeUnidade,
            String nomeProcesso,
            int quantidadeAtividades) {

        Context context = new Context();
        context.setVariable("titulo", TITULO_CADASTRO_DISPONIBILIZADO);
        context.setVariable("nomeUnidade", nomeUnidade);
        context.setVariable("nomeProcesso", nomeProcesso);
        context.setVariable("quantidadeAtividades", quantidadeAtividades);

        templateEngine.process("cadastro-disponibilizado", context);
    }

    /**
     * Gera o conteúdo HTML para o email que notifica a devolução de um cadastro
     * para ajustes.
     * <p>
     * Corresponde ao caso de uso CDU-13.
     *
     * @param nomeUnidade  O nome da unidade que receberá a notificação.
     * @param nomeProcesso O nome do processo associado.
     * @param motivo       O motivo da devolução.
     * @param observacoes  Detalhes ou observações adicionais.
     */
    public void criarEmailCadastroDevolvido(
            String nomeUnidade,
            String nomeProcesso,
            String motivo,
            String observacoes) {

        Context context = new Context();
        context.setVariable("titulo", TITULO_CADASTRO_DEVOLVIDO);
        context.setVariable("nomeUnidade", nomeUnidade);
        context.setVariable("nomeProcesso", nomeProcesso);
        context.setVariable("motivo", motivo);
        context.setVariable("observacoes", observacoes);

        templateEngine.process("cadastro-devolvido", context);
    }

    /**
     * Gera o conteúdo HTML para o email que notifica a disponibilização de um mapa
     * para validação.
     * <p>
     * Corresponde ao caso de uso CDU-17.
     *
     * @param nomeUnidade         O nome da unidade que disponibilizou o mapa.
     * @param nomeProcesso        O nome do processo associado.
     * @param dataLimiteValidacao A data limite para a validação do mapa.
     */
    public void criarEmailMapaDisponibilizado(
            String nomeUnidade,
            String nomeProcesso,
            LocalDateTime dataLimiteValidacao) {

        Context context = new Context();
        context.setVariable("titulo", TITULO_MAPA_DISPONIBILIZADO);
        context.setVariable("nomeUnidade", nomeUnidade);
        context.setVariable("nomeProcesso", nomeProcesso);
        context.setVariable("dataLimiteValidacao", dataLimiteValidacao.format(FORMATADOR));

        templateEngine.process("mapa-disponibilizado", context);
    }

    /**
     * Gera o conteúdo HTML para o email que notifica a validação de um mapa.
     * <p>
     * Corresponde ao caso de uso CDU-18.
     *
     * @param nomeUnidade  O nome da unidade que validou o mapa.
     * @param nomeProcesso O nome do processo associado.
     */
    public void criarEmailMapaValidado(String nomeUnidade, String nomeProcesso) {
        Context context = new Context();
        context.setVariable("titulo", TITULO_MAPA_VALIDADO);
        context.setVariable("nomeUnidade", nomeUnidade);
        context.setVariable("nomeProcesso", nomeProcesso);

        templateEngine.process("mapa-validado", context);
    }

    /**
     * Gera o conteúdo HTML para o email que notifica a finalização de um processo.
     * <p>
     * Corresponde ao caso de uso CDU-21.
     *
     * @param nomeProcesso    O nome do processo finalizado.
     * @param dataFinalizacao A data em que o processo foi finalizado.
     * @param quantidadeMapas O número de mapas que se tornaram vigentes.
     */
    public void criarEmailProcessoFinalizado(String nomeProcesso, LocalDateTime dataFinalizacao, int quantidadeMapas) {
        Context context = new Context();
        context.setVariable("titulo", TITULO_PROCESSO_FINALIZADO);
        context.setVariable("nomeProcesso", nomeProcesso);
        context.setVariable("dataFinalizacao", dataFinalizacao.format(FORMATADOR));
        context.setVariable("quantidadeMapas", quantidadeMapas);

        templateEngine.process("processo-finalizado", context);
    }

    /**
     * Gera o conteúdo HTML para o email que notifica a conclusão de um processo
     * para uma unidade específica.
     * <p>
     * Corresponde ao caso de uso CDU-21.
     *
     * @param siglaUnidade A sigla da unidade notificada.
     * @param nomeProcesso O nome do processo concluído.
     * @return O conteúdo HTML do email.
     */
    public String criarEmailProcessoFinalizadoPorUnidade(String siglaUnidade, String nomeProcesso) {
        Context context = new Context();
        context.setVariable("titulo", "%s%s".formatted(TITULO_PROCESSO_CONCLUSAO_SGC, nomeProcesso));
        context.setVariable("siglaUnidade", siglaUnidade);
        context.setVariable("nomeProcesso", nomeProcesso);

        return templateEngine.process("processo-finalizado-por-unidade", context);
    }

    /**
     * Gera o conteúdo HTML para o email que notifica uma unidade intermediária
     * sobre a conclusão de um processo em suas unidades subordinadas.
     * <p>
     * Corresponde ao caso de uso CDU-21.
     *
     * @param siglaUnidade               A sigla da unidade intermediária notificada.
     * @param nomeProcesso               O nome do processo concluído.
     * @param siglasUnidadesSubordinadas A lista de siglas das unidades subordinadas
     *                                   que participaram do processo.
     * @return O conteúdo HTML do email.
     */
    public String criarEmailProcessoFinalizadoUnidadesSubordinadas(
            String siglaUnidade,
            String nomeProcesso,
            List<String> siglasUnidadesSubordinadas) {

        Context context = new Context();
        context.setVariable("titulo", "%s%s em unidades subordinadas".formatted(TITULO_PROCESSO_CONCLUSAO_SGC, nomeProcesso));
        context.setVariable("siglaUnidade", siglaUnidade);
        context.setVariable("nomeProcesso", nomeProcesso);
        context.setVariable("siglasUnidadesSubordinadas", siglasUnidadesSubordinadas);

        return templateEngine.process("processo-finalizado-unidades-subordinadas", context);
    }
}