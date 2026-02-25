package sgc.subprocesso.dto;

import lombok.Builder;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

/**
 * DTO de requisição para atualizar um subprocesso existente.
 *
 * <p>
 * Usado exclusivamente como entrada de API para o endpoint de atualização.
 * Diferente da criação, não requer codProcesso pois o subprocesso já existe.
 */
@Builder
public record AtualizarSubprocessoRequest(
        Long codUnidade,
        Long codMapa,
        @Nullable LocalDateTime dataLimiteEtapa1,
        @Nullable LocalDateTime dataFimEtapa1,
        @Nullable LocalDateTime dataLimiteEtapa2,
        @Nullable LocalDateTime dataFimEtapa2) {
}
