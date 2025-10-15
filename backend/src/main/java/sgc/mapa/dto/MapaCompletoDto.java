package sgc.mapa.dto;

import java.util.List;
import java.util.ArrayList;

/**
 * DTO que representa um mapa completo com todas as suas competências 
 * e os vínculos com atividades aninhados.
 * <p>
 * Usado para operações agregadas de leitura e escrita do mapa.
 */
public record MapaCompletoDto(
    Long codigo,
    Long subprocessoCodigo,
    String observacoes,
    List<CompetenciaMapaDto> competencias
) {
    public MapaCompletoDto {
        competencias = new ArrayList<>(competencias);
    }

    @Override
    public List<CompetenciaMapaDto> competencias() {
        return new ArrayList<>(competencias);
    }
}