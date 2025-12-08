package sgc.diagnostico.model;

import lombok.Getter;

/**
 * Situação do diagnóstico de uma unidade.
 * Conforme seção "Situações do Subprocesso" do DRAFT-Diagnostico.md.
 */
@Getter
public enum SituacaoDiagnostico {
    EM_ANDAMENTO("Em andamento"),
    CONCLUIDO("Concluído"),
    VALIDADO("Validado"),
    HOMOLOGADO("Homologado");

    private final String label;

    SituacaoDiagnostico(String label) {
        this.label = label;
    }
}
