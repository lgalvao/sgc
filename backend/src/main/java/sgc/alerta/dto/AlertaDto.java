package sgc.alerta.dto;

import lombok.*;
import org.jspecify.annotations.*;

import java.time.*;

@Builder
public record AlertaDto(
        Long codigo,
        @Nullable Long codProcesso,
        @Nullable String processo,
        String origem,
        @Nullable String unidadeDestino,
        String descricao,
        String mensagem,
        LocalDateTime dataHora,
        @Nullable LocalDateTime dataHoraLeitura) {
}
