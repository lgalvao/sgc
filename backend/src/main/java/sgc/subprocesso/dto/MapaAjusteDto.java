package sgc.subprocesso.dto;

import lombok.Builder;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * DTO de resposta para visualização do mapa na tela de ajustes.
 *
 * <p>Usado exclusivamente como resposta de API pelo endpoint
 * {@code GET /subprocessos/{codigo}/mapa-ajuste}.
 *
 * <p>Para enviar ajustes, use {@link SalvarAjustesRequest}.
 */
@Getter
@Builder
public class MapaAjusteDto {

    private final @Nullable Long codMapa;
    private final String unidadeNome;
    private final List<CompetenciaAjusteDto> competencias;
    private final @org.jspecify.annotations.Nullable String justificativaDevolucao;
}
