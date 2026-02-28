package sgc.subprocesso.dto;

import lombok.*;

import java.util.*;

/**
 * DTO para competência no contexto de ajustes do mapa.
 *
 * <p>Usado como parte de {@link MapaAjusteDto} (Response) e
 * {@link SalvarAjustesRequest} (Request).
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class CompetenciaAjusteDto {
    private final Long codCompetencia;
    private final String nome;


    @Builder.Default
    private final List<AtividadeAjusteDto> atividades = List.of();
}
