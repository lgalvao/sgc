package sgc.processo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

/**
 * DTO usado para atualizar um processo existente.
 */
public record AtualizarProcessoReq(
    Long codigo,

    @NotBlank(message = "Preencha a descrição")
    String descricao,

    @NotBlank(message = "Tipo do processo é obrigatório")
    String tipo,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate dataLimiteEtapa1,

    @NotEmpty(message = "Pelo menos uma unidade participante deve ser incluída.")
    List<Long> unidades
) {
    public AtualizarProcessoReq {
        unidades = new ArrayList<>(unidades);
    }

    @Override
    public List<Long> unidades() {
        return new ArrayList<>(unidades);
    }
}