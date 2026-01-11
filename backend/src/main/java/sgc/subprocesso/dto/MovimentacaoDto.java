package sgc.subprocesso.dto;

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
    @java.io.Serial
    private static final long serialVersionUID = 1L;
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
