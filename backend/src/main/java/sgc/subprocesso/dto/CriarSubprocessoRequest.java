package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * DTO de requisição para criar um novo subprocesso.
 *
 * <p>
 * Usado exclusivamente como entrada de API para o endpoint de criação.
 * A validação Bean Validation é aplicada neste DTO.
 */
@Builder
public record CriarSubprocessoRequest(
                @NotNull(message = "O código do processo é obrigatório") Long codProcesso,

                @NotNull(message = "O código da unidade é obrigatório") Long codUnidade,

                Long codMapa,

                @NotNull(message = "A data limite da etapa 1 é obrigatória")
                LocalDateTime dataLimiteEtapa1,
                LocalDateTime dataLimiteEtapa2) {
}
