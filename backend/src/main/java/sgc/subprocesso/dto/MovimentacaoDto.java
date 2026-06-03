package sgc.subprocesso.dto;

import lombok.*;

import java.time.*;

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
        String usuarioTitulo,
        String usuarioNome,
        String descricao
) {
}
