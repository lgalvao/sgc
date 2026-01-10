package sgc.mapa.dto.visualizacao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConhecimentoDto {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private Long codigo;
    private String descricao;
}
