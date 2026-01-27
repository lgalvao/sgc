package sgc.mapa.dto;

import jakarta.validation.Valid;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * DTO de requisição para salvar mapa. CDU-09 item 1 a 4.
 */
@Builder
public record SalvarMapaRequest(
        @Nullable String observacoes,
        @Valid List<CompetenciaMapaDto> competencias) {
}
