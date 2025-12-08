package sgc.diagnostico.model;

import lombok.Getter;

/**
 * Situação do servidor no processo de diagnóstico.
 * Conforme seção "Situações relacionadas ao servidor" do DRAFT-Diagnostico.md.
 */
@Getter
public enum SituacaoServidorDiagnostico {
    AUTOAVALIACAO_NAO_REALIZADA("Autoavaliação não realizada"),
    AUTOAVALIACAO_CONCLUIDA("Autoavaliação concluída"),
    CONSENSO_CRIADO("Avaliação de consenso criada"),
    CONSENSO_APROVADO("Avaliação de consenso aprovada"),
    AVALIACAO_IMPOSSIBILITADA("Avaliação impossibilitada");

    private final String label;

    SituacaoServidorDiagnostico(String label) {
        this.label = label;
    }
}
