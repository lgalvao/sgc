package sgc.diagnostico.dto;

import lombok.Builder;
import java.util.List;

@Builder
public record ServidorDiagnosticoDto(
        String servidorTitulo,
        String servidorNome,
        String situacaoServidor,
        List<AvaliacaoCompetenciaDto> consenso,
        boolean podeManterConsenso,
        boolean podeImpossibilitar,
        boolean podePermitirAvaliacao
) {
}
