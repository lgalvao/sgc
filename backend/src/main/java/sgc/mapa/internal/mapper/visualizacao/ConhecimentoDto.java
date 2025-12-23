package sgc.mapa.internal.mapper.visualizacao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConhecimentoDto {
    private Long codigo;
    private String descricao;
}
