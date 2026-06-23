package sgc.diagnostico.dto;

import lombok.*;

import java.util.*;

@Builder
public record ConsensoDto(
        String servidorNome,
        List<ConsensoCompetenciaDto> competencias,
        String situacaoServidor,
        boolean podeEditar,
        boolean podeConcluirAvaliacao,
        boolean habilitarConcluirAvaliacao,
        boolean podeAprovarConsenso,
        boolean habilitarAprovarConsenso
) {
}
