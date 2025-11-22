package sgc.sgrh.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para dados de unidade do SGRH.
 * Suporta estrutura hierárquica com subunidades.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnidadeDto {
    private Long codigo;
    private String nome;
    private String sigla;
    private Long codigoPai;
    private String tipo;
    private List<UnidadeDto> subunidades;  // Para árvore hierárquica

    @JsonProperty("isElegivel")
    private boolean isElegivel;


    /**
     * Construtor sem subunidades.
     */
    public UnidadeDto(Long codigo, String nome, String sigla, Long codigoPai, String tipo, boolean isElegivel) {
        this.codigo = codigo;
        this.nome = nome;
        this.sigla = sigla;
        this.codigoPai = codigoPai;
        this.tipo = tipo;
        this.subunidades = null;
        this.isElegivel = isElegivel;
    }

    public void setElegivel(boolean elegivel) {
        isElegivel = elegivel;
        if (subunidades != null) {
            for (UnidadeDto subunidade : subunidades) {
                subunidade.setElegivel(elegivel);
            }
        }
    }
}
