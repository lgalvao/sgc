package sgc.organizacao.dto;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.jspecify.annotations.*;
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
    private @Nullable Long codigoPai;
    @JsonView(OrganizacaoViews.Publica.class)
    private @Nullable String tipo;

    @Builder.Default
    @JsonView(OrganizacaoViews.Publica.class)
    private List<UnidadeDto> subunidades = new ArrayList<>();

    @JsonView(OrganizacaoViews.Publica.class)
    @JsonProperty("tituloTitular")
    private @Nullable String tituloTitular;

    @JsonView(OrganizacaoViews.Publica.class)
    @JsonProperty("isElegivel")
    private boolean isElegivel;

    @JsonView(OrganizacaoViews.Publica.class)
    private @Nullable UsuarioResumoDto responsavel;

    public static @Nullable UnidadeDto fromEntity(@Nullable Unidade entity) {
        if (entity == null) return null;
        return fromEntityObrigatoria(entity);
    }

    public static UnidadeDto fromEntityObrigatoria(Unidade entity) {
        Objects.requireNonNull(entity, "Unidade obrigatoria para montagem do DTO");

        UnidadeDto dto = fromResumoObrigatorio(
                entity.getCodigo(),
                entity.getNome(),
                entity.getSigla(),
                entity.getUnidadeSuperior() != null ? entity.getUnidadeSuperior().getCodigo() : null,
                entity.getTipo(),
                entity.getTituloTitular()
        );

        Responsabilidade responsabilidade = entity.getResponsabilidade();
        if (responsabilidade != null) {
            dto.setResponsavel(UsuarioResumoDto.fromEntityObrigatorio(responsabilidade.getUsuario()));
        }

        return dto;
    }

    public static UnidadeDto fromEntityResumoObrigatoria(@Nullable Unidade entity) {
        if (entity == null) {
            throw new IllegalStateException("Unidade obrigatoria para resumo");
        }

        return fromResumoObrigatorio(
                entity.getCodigo(),
                entity.getNome(),
                entity.getSigla(),
                null,
                entity.getTipo(),
                entity.getTituloTitular()
        );
    }

    public static UnidadeDto fromResumoObrigatorio(
            Long codigo,
            String nome,
            String sigla,
            Long codigoPai,
            TipoUnidade tipo,
            String tituloTitular
    ) {
        return UnidadeDto.builder()
                .codigo(Objects.requireNonNull(codigo, "Codigo da unidade obrigatorio"))
                .nome(Objects.requireNonNull(nome, "Nome da unidade obrigatorio"))
                .sigla(Objects.requireNonNull(sigla, "Sigla da unidade obrigatoria"))
                .codigoPai(codigoPai)
                .tipo(tipo != null ? tipo.name() : null)
                .tituloTitular(tituloTitular)
                .build();
    }
}
