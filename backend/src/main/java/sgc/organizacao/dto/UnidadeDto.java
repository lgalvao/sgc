package sgc.organizacao.dto;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.*;
import sgc.comum.*;
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
    @NotBlank(message = Mensagens.SIGLA_OBRIGATORIA)
    @Size(max = 20, message = Mensagens.SIGLA_MAX)
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
    private UsuarioResumoDto responsavel;

    public static UnidadeDto fromEntity(Unidade entity) {
        if (entity == null) return null;
        UnidadeDto dto = UnidadeDto.builder()
                .codigo(entity.getCodigo())
                .nome(entity.getNome())
                .sigla(entity.getSigla())
                .codigoPai(entity.getUnidadeSuperior() != null ? entity.getUnidadeSuperior().getCodigo() : null)
                .tipo(entity.getTipo() != null ? entity.getTipo().name() : null)
                .tituloTitular(entity.getTituloTitular())
                .build();

        if (entity.getResponsabilidade() != null) {
            dto.setResponsavel(UsuarioResumoDto.fromEntity(entity.getResponsabilidade().getUsuario()));
        }

        return dto;
    }
}
