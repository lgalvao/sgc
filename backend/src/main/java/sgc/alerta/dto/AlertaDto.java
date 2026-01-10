package sgc.alerta.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class AlertaDto {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    Long codigo;
    Long codProcesso;
    String unidadeOrigem;
    String unidadeDestino;
    String descricao;
    LocalDateTime dataHora;
    LocalDateTime dataHoraLeitura;
    String mensagem;
    String dataHoraFormatada;
    String origem;
    String processo;
}
