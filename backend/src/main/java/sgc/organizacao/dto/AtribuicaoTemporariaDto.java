package sgc.organizacao.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtribuicaoTemporariaDto {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private Long codigo;
    private UnidadeDto unidade;
    private UsuarioDto usuario;
    private LocalDateTime dataInicio;
    private LocalDateTime dataTermino;
    private String justificativa;
}
