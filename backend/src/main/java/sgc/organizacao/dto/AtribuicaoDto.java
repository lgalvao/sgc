package sgc.organizacao.dto;

import lombok.Builder;

import java.time.LocalDateTime;

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
