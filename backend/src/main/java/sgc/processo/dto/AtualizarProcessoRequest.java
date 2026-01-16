package sgc.processo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.processo.model.TipoProcesso;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO usado para atualizar um processo existente.
 * 
 * <p>Requer @NoArgsConstructor e @Setter para deserialização Jackson.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtualizarProcessoRequest {
    private Long codigo;

    @NotBlank(message = "Preencha a descrição")
    private String descricao;

    @NotNull(message = "Tipo do processo é obrigatório")
    private TipoProcesso tipo;

    @NotNull(message = "Preencha a data limite")
    @Future(message = "A data limite deve ser futura")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataLimiteEtapa1;

    @NotEmpty(message = "Pelo menos uma unidade participante deve ser incluída.")
    private List<Long> unidades;
}
