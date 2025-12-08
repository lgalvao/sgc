package sgc.diagnostico.dto;

/**
 * Request para concluir autoavaliação de servidor.
 * Conforme CDU-02 do DRAFT-Diagnostico.md.
 */
public record ConcluirAutoavaliacaoRequest(
        String justificativaAtraso  // Obrigatória se fora do prazo
) {
}
