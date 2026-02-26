package sgc.subprocesso.dto;

import lombok.*;

import java.time.*;

/**
 * DTO de resposta representando uma movimentação no histórico.
 */
@Builder
public record MovimentacaoDto(
        Long codigo,
        LocalDateTime dataHora,

        Long unidadeOrigemCodigo,
        String unidadeOrigemSigla,
        String unidadeOrigemNome,
        Long unidadeDestinoCodigo,
        String unidadeDestinoSigla,
        String unidadeDestinoNome,
        String descricao
) {
}
