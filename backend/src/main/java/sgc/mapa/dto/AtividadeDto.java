package sgc.mapa.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sgc.comum.json.SanitizarHtml;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtividadeDto {
    private Long codigo;
    private Long mapaCodigo;

    @NotBlank(message = "Descrição não pode ser vazia")
    @SanitizarHtml
    private String descricao;
}
