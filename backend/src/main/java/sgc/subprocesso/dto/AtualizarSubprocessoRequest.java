package sgc.subprocesso.dto;

import lombok.Builder;

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
                LocalDateTime dataLimiteEtapa1,
                LocalDateTime dataFimEtapa1,
                LocalDateTime dataLimiteEtapa2,
                LocalDateTime dataFimEtapa2) {
}
