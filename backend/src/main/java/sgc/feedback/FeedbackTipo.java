package sgc.feedback;

import com.fasterxml.jackson.annotation.*;

/**
 * Tipo do feedback enviado pelo usuário durante homologação.
 */
public enum FeedbackTipo {
    BUG,
    SUGESTAO,
    QUESTAO,
    ELOGIO;

    @JsonCreator
    public static FeedbackTipo fromJson(String valor) {
        return valueOf(valor.toUpperCase());
    }

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }
}
