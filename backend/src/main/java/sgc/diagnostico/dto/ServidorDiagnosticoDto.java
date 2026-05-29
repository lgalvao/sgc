package sgc.diagnostico.dto;

import java.util.List;

public record ServidorDiagnosticoDto(
        String servidorTitulo,
        String servidorNome,
        String situacaoServidor,
        List<AvaliacaoCompetenciaDto> consenso
) {
}
