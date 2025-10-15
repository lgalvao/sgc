package sgc.mapa.dto;

import java.util.List;

/**
 * DTO que representa um mapa completo com todas as suas competências 
 * e os vínculos com atividades aninhados.
 * <p>
 * Usado para operações agregadas de leitura e escrita do mapa.
 */
import java.util.ArrayList;

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