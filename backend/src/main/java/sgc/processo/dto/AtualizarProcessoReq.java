package sgc.processo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sgc.processo.model.TipoProcesso;

/** DTO usado para atualizar um processo existente. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtualizarProcessoReq {
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
