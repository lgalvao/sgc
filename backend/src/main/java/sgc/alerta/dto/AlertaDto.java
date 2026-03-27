package sgc.alerta.dto;

import lombok.*;
import sgc.alerta.model.*;

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

    public static AlertaDto fromEntity(Alerta alerta) {
        return AlertaDto.builder()
                .codigo(alerta.getCodigo())
                .codProcesso(alerta.getCodProcessoSintetico())
                .processo(alerta.getProcessoDescricaoSintetica())
                .origem(alerta.getOrigemSiglaSintetica())
                .unidadeDestino(alerta.getUnidadeDestinoSigla())
                .descricao(alerta.getDescricao())
                .mensagem(alerta.getMensagemSintetica())
                .dataHora(alerta.getDataHora())
                .dataHoraLeitura(alerta.getDataHoraLeitura())
                .build();
    }
}
