package sgc.subprocesso.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
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