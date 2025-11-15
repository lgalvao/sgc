package sgc.alerta.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class AlertaDto {
    Long codigo;
    Long codProcesso;
    String unidadeOrigem;
    String unidadeDestino;
    String descricao;
    LocalDateTime dataHora;
    LocalDateTime dataHoraLeitura;
    String linkDestino;
}
