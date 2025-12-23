package sgc.processo.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sgc.processo.internal.model.TipoProcesso;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CriarProcessoReq {

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

    public List<Long> getUnidades() {
        return unidades == null ? null : new ArrayList<>(unidades);
    }

    public void setUnidades(List<Long> unidades) {
        this.unidades = unidades == null ? null : new ArrayList<>(unidades);
    }
}
