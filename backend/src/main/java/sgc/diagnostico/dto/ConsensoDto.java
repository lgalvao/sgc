package sgc.diagnostico.dto;

import lombok.Builder;
import java.util.List;

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
