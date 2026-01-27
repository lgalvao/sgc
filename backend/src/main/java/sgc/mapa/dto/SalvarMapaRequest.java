package sgc.mapa.dto;

import java.util.List;

import org.jspecify.annotations.Nullable;

import jakarta.validation.Valid;
import lombok.Builder;

/**
 * DTO de requisição para salvar mapa. CDU-09 item 1 a 4.
 */
@Builder
public record SalvarMapaRequest(
        @Nullable @sgc.seguranca.sanitizacao.SanitizarHtml String observacoes,
        @Valid List<CompetenciaMapaDto> competencias) {
}
