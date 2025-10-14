package sgc.subprocesso.dto;

/**
 * DTO para retornar sugestões apresentadas ao mapa (CDU-20 item 5).
 */
import sgc.subprocesso.modelo.Subprocesso;

public record SugestoesDto(
    String sugestoes,
    boolean sugestoesApresentadas,
    String unidadeNome
) {

    public static SugestoesDto of(Subprocesso subprocesso) {
        String sugestoes = subprocesso.getMapa() != null ? subprocesso.getMapa().getSugestoes() : null;
        boolean sugestoesApresentadas = sugestoes != null && !sugestoes.trim().isEmpty();
        String nomeUnidade = subprocesso.getUnidade() != null ? subprocesso.getUnidade().getNome() : null;
        return new SugestoesDto(sugestoes, sugestoesApresentadas, nomeUnidade);
    }
}