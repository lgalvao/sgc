package sgc.processo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import sgc.processo.model.TipoProcesso;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO usado para atualizar um processo existente.
 */
@Getter
@Builder
@AllArgsConstructor
public class AtualizarProcessoRequest {
    private final Long codigo;

    @NotBlank(message = "Preencha a descrição")
    private final String descricao;

    @NotNull(message = "Tipo do processo é obrigatório")
    private final TipoProcesso tipo;

    @NotNull(message = "Preencha a data limite")
    @Future(message = "A data limite deve ser futura")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime dataLimiteEtapa1;

    @NotEmpty(message = "Pelo menos uma unidade participante deve ser incluída.")
    private final List<Long> unidades;
}
