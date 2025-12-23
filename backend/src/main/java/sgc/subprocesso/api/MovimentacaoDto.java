package sgc.subprocesso.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
