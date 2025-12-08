package sgc.diagnostico.model;

import lombok.Getter;

/**
 * Níveis de avaliação para importância e domínio de competências no diagnóstico.
 * Conforme CDU-02 do DRAFT-Diagnostico.md.
 */
@Getter
public enum NivelAvaliacao {
    NA("Não se aplica", 0),
    N1("1", 1),
    N2("2", 2),
    N3("3", 3),
    N4("4", 4),
    N5("5", 5),
    N6("6", 6);

    private final String label;
    private final int valor;

    NivelAvaliacao(String label, int valor) {
        this.label = label;
        this.valor = valor;
    }
}
