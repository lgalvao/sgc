package sgc.subprocesso.dto;

import lombok.Builder;

/**
 * DTO com dados de responsável (RUP) de uma unidade para exibição.
 */
@Builder
public record ResponsavelDto(
        String nome,
        String email,
        String titulo) {
}
