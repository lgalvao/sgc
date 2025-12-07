package sgc.subprocesso.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimentacaoDto {
    private Long codigo;
    private LocalDateTime dataHora;
    private Long unidadeOrigemCodigo;
    private String unidadeOrigemSigla;
    private String unidadeOrigemNome;
    private Long unidadeDestinoCodigo;
    private String unidadeDestinoSigla;
    private String unidadeDestinoNome;
    private String descricao;
}
