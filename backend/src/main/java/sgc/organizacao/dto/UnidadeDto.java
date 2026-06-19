package sgc.organizacao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import sgc.comum.Mensagens;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    @NotBlank(message = Mensagens.SIGLA_OBRIGATORIA)
    @Size(max = 20, message = Mensagens.SIGLA_MAX)
    private String sigla;

    private Long codigoPai;

    private String tipo;

    @Builder.Default
    private List<UnidadeDto> subunidades = new ArrayList<>();

    @JsonProperty("tituloTitular")
    private String tituloTitular;

    @JsonProperty("isElegivel")
    private boolean isElegivel;

    @JsonProperty("tipoResponsabilidade")
    private String tipoResponsabilidade;

    @JsonProperty("dataInicioResponsabilidade")
    private LocalDateTime dataInicioResponsabilidade;

    @JsonProperty("dataFimResponsabilidade")
    private LocalDateTime dataFimResponsabilidade;

    private UsuarioResumoDto titular;

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
