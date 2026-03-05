package sgc.organizacao.dto;

import lombok.Builder;
import sgc.organizacao.model.Usuario;
import java.time.LocalDateTime;

@Builder
public record ResponsavelDto(
        Usuario usuario,
        String tipo,
        LocalDateTime dataInicio,
        LocalDateTime dataFim
) {}
