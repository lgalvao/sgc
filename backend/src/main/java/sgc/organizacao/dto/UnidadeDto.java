package sgc.organizacao.dto;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.jspecify.annotations.*;
import sgc.comum.*;
import sgc.organizacao.model.OrganizacaoViews;

import java.time.*;
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
    @JsonProperty("tipoResponsabilidade")
    private String tipoResponsabilidade;

    @JsonView(OrganizacaoViews.Publica.class)
    @JsonProperty("dataInicioResponsabilidade")
    private LocalDateTime dataInicioResponsabilidade;

    @JsonView(OrganizacaoViews.Publica.class)
    @JsonProperty("dataFimResponsabilidade")
    private LocalDateTime dataFimResponsabilidade;

    @JsonView(OrganizacaoViews.Publica.class)
    private UsuarioResumoDto titular;

    @JsonView(OrganizacaoViews.Publica.class)
    private UsuarioResumoDto responsavel;

    public static UnidadeDto fromResumoObrigatorio(
            Long codigo,
            String nome,
            String sigla,
            Long codigoPai,
            String tipo,
            String tituloTitular
    ) {
        return UnidadeDto.builder()
                .codigo(Objects.requireNonNull(codigo, "Codigo da unidade obrigatorio"))
                .nome(Objects.requireNonNull(nome, "Nome da unidade obrigatorio"))
                .sigla(Objects.requireNonNull(sigla, "Sigla da unidade obrigatoria"))
                .codigoPai(codigoPai)
                .tipo(tipo)
                .tituloTitular(tituloTitular)
                .build();
    }
}
