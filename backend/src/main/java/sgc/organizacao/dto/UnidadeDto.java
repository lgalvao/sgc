package sgc.organizacao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * DTO para dados de unidade do SGRH. Suporta estrutura hierárquica com subunidades.
 * 
 * <p>Requer @NoArgsConstructor e @Setter para uso em mappers e construção de árvore.
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
    private List<UnidadeDto> subunidades = new java.util.ArrayList<>(); // Para árvore hierárquica

    @JsonProperty("tituloTitular")
    private String tituloTitular;

    @JsonProperty("isElegivel")
    private boolean isElegivel;
}
