package sgc.subprocesso.dto;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * DTO com as sugestões do CHEFE (CDU-19 item 8).
 */
@Builder
public record SugestoesDto(
        String sugestoes,
        LocalDateTime dataHora) {
}
