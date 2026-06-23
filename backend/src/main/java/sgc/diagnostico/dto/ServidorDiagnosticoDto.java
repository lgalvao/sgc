package sgc.diagnostico.dto;

import lombok.*;

import java.util.*;

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
