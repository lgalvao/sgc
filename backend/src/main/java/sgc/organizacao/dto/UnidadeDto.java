package sgc.organizacao.dto;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.*;
import sgc.organizacao.model.*;

import java.util.*;

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
    @NotBlank(message = "Sigla é obrigatória")
    @Size(max = 20, message = "A sigla deve ter no máximo 20 caracteres")
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

    @JsonView(OrganizacaoViews.Publica.class)
    private Usuario responsavel;

    public static UnidadeDto fromEntity(Unidade entity) {
        if (entity == null) return null;
        UnidadeDto dto = UnidadeDto.builder()
                .codigo(entity.getCodigo())
                .nome(entity.getNome())
                .sigla(entity.getSigla())
                .codigoPai(entity.getUnidadeSuperior() != null ? entity.getUnidadeSuperior().getCodigo() : null)
                .tipo(entity.getTipo().name())
                .tituloTitular(entity.getTituloTitular())
                .build();

        if (entity.getResponsabilidade() != null) {
            dto.setResponsavel(entity.getResponsabilidade().getUsuario());
        }

        return dto;
    }
}
