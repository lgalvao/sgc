package sgc.sgrh.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.AccessLevel;

import java.util.List;

/**
 * DTO para dados de unidade do SGRH. Suporta estrutura hierárquica com subunidades.
 */
@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UnidadeDto {
    private Long codigo;
    private String nome;
    private String sigla;
    private Long codigoPai;
    private String tipo;
    private List<UnidadeDto> subunidades; // Para árvore hierárquica

    @JsonProperty("tituloTitular")
    private String tituloTitular;

    @JsonProperty("isElegivel")
    private boolean isElegivel;
}
