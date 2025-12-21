package sgc.unidade.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sgc.sgrh.dto.UnidadeDto;
import sgc.sgrh.dto.UsuarioDto;

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
