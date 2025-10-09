package sgc.subprocesso.dto;

import java.time.LocalDateTime;

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