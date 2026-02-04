package sgc.organizacao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO para dados de unidade do SGRH. Suporta estrutura hierárquica com subunidades.
 *
 * <p>Mantido como classe por necessitar de mutabilidade para construção de árvore hierárquica
 * no {@link sgc.organizacao.service.UnidadeHierarquiaService}.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnidadeDto {
    private Long codigo;
    private String nome;
    private String sigla;
    private Long codigoPai;
    private String tipo;

    @Builder.Default
    private List<UnidadeDto> subunidades = new ArrayList<>();

    @JsonProperty("tituloTitular")
    private String tituloTitular;

    @JsonProperty("isElegivel")
    private boolean isElegivel;
}
