package sgc.subprocesso.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sgc.subprocesso.internal.model.Subprocesso;

/**
 * DTO para retornar sugestões apresentadas ao mapa (CDU-20 item 5).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SugestoesDto {
    /**
     * O texto das sugestões.
     */
    private String sugestoes;

    /**
     * Indica se foram apresentadas sugestões.
     */
    private boolean sugestoesApresentadas;

    /**
     * O nome da unidade que apresentou as sugestões.
     */
    private String unidadeNome;

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
