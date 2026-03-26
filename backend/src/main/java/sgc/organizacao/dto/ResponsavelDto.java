package sgc.organizacao.dto;

import lombok.*;
import java.time.*;

@Builder
public record ResponsavelDto(
        UsuarioResumoDto usuario,
        String tipo,
        LocalDateTime dataInicio,
        LocalDateTime dataFim
) {}
