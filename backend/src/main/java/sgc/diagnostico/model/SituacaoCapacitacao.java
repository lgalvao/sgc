package sgc.diagnostico.model;

import lombok.Getter;

/**
 * Situação de capacitação de servidor para cada competência.
 * Conforme CDU-07 do DRAFT-Diagnostico.md.
 */
@Getter
public enum SituacaoCapacitacao {
    NA("Não se aplica"),
    AC("A capacitar"),
    EC("Em capacitação"),
    C("Capacitado"),
    I("Instrutor");

    private final String label;

    SituacaoCapacitacao(String label) {
        this.label = label;
    }
}
