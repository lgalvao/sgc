package sgc.mapa.dto.visualizacao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtividadeDto {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private Long codigo;
    private String descricao;
    private List<ConhecimentoDto> conhecimentos;
}
