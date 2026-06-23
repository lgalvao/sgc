package sgc.diagnostico.dto;

import lombok.*;

import java.util.*;

@Builder
public record AutoavaliacaoDto(
        List<AvaliacaoCompetenciaDto> competencias,
        String situacaoServidor,
        boolean podeEditar,
        boolean podeConcluirAutoavaliacao,
        boolean habilitarConcluirAutoavaliacao
) {
}
