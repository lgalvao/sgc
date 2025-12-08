package sgc.diagnostico.dto;

/**
 * Request para concluir diagnóstico da unidade.
 * Conforme CDU-09 do DRAFT-Diagnostico.md.
 */
public record ConcluirDiagnosticoRequest(
        String justificativa  // Obrigatória se houver pendências
) {
}
