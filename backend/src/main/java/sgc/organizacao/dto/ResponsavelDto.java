package sgc.organizacao.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ResponsavelDto(
        UsuarioResumoDto usuario,
        String tipo,
        LocalDateTime dataInicio,
        LocalDateTime dataFim
) {
}
