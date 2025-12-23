package sgc.unidade.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sgc.sgrh.api.UnidadeDto;
import sgc.sgrh.api.UsuarioDto;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtribuicaoTemporariaDto {
    private Long codigo;
    private UnidadeDto unidade;
    private UsuarioDto usuario;
    private LocalDateTime dataInicio;
    private LocalDateTime dataTermino;
    private String justificativa;
}
