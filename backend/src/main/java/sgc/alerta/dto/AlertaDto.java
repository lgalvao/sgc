package sgc.alerta.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;

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
    String mensagem;
    String dataHoraFormatada;
    String origem;
    String processo;
}
