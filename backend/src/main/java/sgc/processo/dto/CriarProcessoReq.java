package sgc.processo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public record CriarProcessoReq(
    @NotBlank(message = "Preencha a descrição")
    String descricao,

    @jakarta.validation.constraints.NotNull(message = "Tipo do processo é obrigatório")
    sgc.processo.model.TipoProcesso tipo,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime dataLimiteEtapa1,

    @NotEmpty(message = "Pelo menos uma unidade participante deve ser incluída.")
    List<Long> unidades
) {
    public CriarProcessoReq {
        unidades = new ArrayList<>(unidades);
    }

    @Override
    public List<Long> unidades() {
        return new ArrayList<>(unidades);
    }
}
