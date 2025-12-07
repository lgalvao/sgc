package sgc.processo.dto;

import java.time.LocalDateTime;
import lombok.*;
import sgc.processo.model.SituacaoProcesso;

@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProcessoDto {
    private Long codigo;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataFinalizacao;
    private LocalDateTime dataLimite;
    private String descricao;
    private SituacaoProcesso situacao;
    private String tipo;

    // Campos formatados para apresentação
    private String dataCriacaoFormatada;
    private String dataFinalizacaoFormatada;
    private String dataLimiteFormatada;
    private String situacaoLabel;
    private String tipoLabel;
}

