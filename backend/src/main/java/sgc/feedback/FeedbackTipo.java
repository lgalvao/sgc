package sgc.feedback;

import com.fasterxml.jackson.annotation.*;

/**
 * Tipo do feedback enviado pelo usuário durante UAT.
 */
public enum FeedbackTipo {
    BUG,
    SUGESTAO,
    QUESTAO,
    ELOGIO;

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static FeedbackTipo fromJson(String valor) {
        return valueOf(valor.toUpperCase());
    }
}
