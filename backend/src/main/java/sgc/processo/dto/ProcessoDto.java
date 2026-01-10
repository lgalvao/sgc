package sgc.processo.dto;

import lombok.*;
import sgc.processo.model.SituacaoProcesso;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProcessoDto {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
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
    private String unidadesParticipantes;
}
