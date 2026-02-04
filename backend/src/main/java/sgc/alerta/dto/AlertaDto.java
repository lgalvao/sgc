package sgc.alerta.dto;

import lombok.Builder;
import lombok.Value;
import org.jspecify.annotations.Nullable;

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
    String mensagem;
    String dataHoraFormatada;
    String origem;
    String processo;
    @Nullable LocalDateTime dataHoraLeitura;
}
