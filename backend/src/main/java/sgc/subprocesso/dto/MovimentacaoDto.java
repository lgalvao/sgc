package sgc.subprocesso.dto;

import lombok.Builder;

import java.time.LocalDateTime;

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
) {}
