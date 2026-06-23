package sgc.alerta.dto;

import lombok.*;

import java.time.*;

@Builder
public record AlertaDto(
        Long codigo,
        Long codProcesso,
        String processo,
        String origem,
        String unidadeDestino,
        String descricao,
        String mensagem,
        LocalDateTime dataHora,
        LocalDateTime dataHoraLeitura) {
}
