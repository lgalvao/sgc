package sgc.processo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO usado para atualizar um processo existente.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReqAtualizarProcesso {
    private Long codigo;

    @NotBlank(message = "Preencha a descrição")
    private String descricao;

    @NotBlank(message = "Tipo do processo é obrigatório")
    private String tipo;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dataLimiteEtapa1;

    @NotEmpty(message = "Pelo menos uma unidade participante deve ser incluída.")
    private List<Long> unidades;
}