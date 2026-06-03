package sgc.organizacao.dto;

import lombok.*;

import java.time.*;

@Builder
public record AtribuicaoDto(
        Long codigo,
        Long unidadeCodigo,
        String unidadeSigla,
        UsuarioResumoDto usuario,
        LocalDateTime dataInicio,
        LocalDateTime dataTermino,
        String justificativa) {
}
