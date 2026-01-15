package sgc.subprocesso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

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
    private final List<AtividadeAjusteDto> atividades;
}
