package sgc.diagnostico.dto;


import java.util.List;

public record AutoavaliacaoDto(
        List<AvaliacaoCompetenciaDto> competencias,
        String situacaoServidor
) {
}
