package sgc.subprocesso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import sgc.subprocesso.model.Subprocesso;

/**
 * DTO para retornar sugestões apresentadas ao mapa (CDU-20 item 5).
 */
@Getter
@Builder
@AllArgsConstructor
public class SugestoesDto {

    /**
     * O texto das sugestões.
     */
    private final String sugestoes;

    /**
     * Indica se foram apresentadas sugestões.
     */
    private final boolean sugestoesApresentadas;

    /**
     * O nome da unidade que apresentou as sugestões.
     */
    private final String unidadeNome;

    public static SugestoesDto of(Subprocesso subprocesso) {
        String sugestoes =
                subprocesso.getMapa() != null ? subprocesso.getMapa().getSugestoes() : null;
        boolean sugestoesApresentadas = sugestoes != null && !sugestoes.isBlank();
        String nomeUnidade =
                subprocesso.getUnidade() != null ? subprocesso.getUnidade().getNome() : null;

        return SugestoesDto.builder()
                .sugestoes(sugestoes)
                .sugestoesApresentadas(sugestoesApresentadas)
                .unidadeNome(nomeUnidade)
                .build();
    }
}
