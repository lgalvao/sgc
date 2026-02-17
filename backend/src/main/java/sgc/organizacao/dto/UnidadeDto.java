package sgc.organizacao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.*;
import sgc.organizacao.model.OrganizacaoViews;
import sgc.organizacao.model.Unidade;

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
    @JsonView(OrganizacaoViews.Publica.class)
    private Long codigo;
    @JsonView(OrganizacaoViews.Publica.class)
    private String nome;
    @JsonView(OrganizacaoViews.Publica.class)
    private String sigla;
    @JsonView(OrganizacaoViews.Publica.class)
    private Long codigoPai;
    @JsonView(OrganizacaoViews.Publica.class)
    private String tipo;

    @Builder.Default
    @JsonView(OrganizacaoViews.Publica.class)
    private List<UnidadeDto> subunidades = new ArrayList<>();

    @JsonView(OrganizacaoViews.Publica.class)
    @JsonProperty("tituloTitular")
    private String tituloTitular;

    @JsonView(OrganizacaoViews.Publica.class)
    @JsonProperty("isElegivel")
    private boolean isElegivel;

    public static UnidadeDto fromEntity(Unidade entity) {
        if (entity == null) return null;
        return UnidadeDto.builder()
                .codigo(entity.getCodigo())
                .nome(entity.getNome())
                .sigla(entity.getSigla())
                .codigoPai(entity.getUnidadeSuperior() != null ? entity.getUnidadeSuperior().getCodigo() : null)
                .tipo(entity.getTipo().name())
                .tituloTitular(entity.getTituloTitular())
                .build();
    }
}
