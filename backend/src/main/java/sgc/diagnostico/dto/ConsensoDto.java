package sgc.diagnostico.dto;


import java.util.List;

public record ConsensoDto(
        List<AvaliacaoCompetenciaDto> competencias,
        String situacaoServidor
) {
}