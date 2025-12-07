package sgc.mapa.dto.visualizacao;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtividadeDto {
    private Long codigo;
    private String descricao;
    private List<ConhecimentoDto> conhecimentos;
}
