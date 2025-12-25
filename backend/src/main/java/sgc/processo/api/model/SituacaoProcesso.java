package sgc.processo.api.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SituacaoProcesso {
    CRIADO("Criado"),
    EM_ANDAMENTO("Em andamento"),
    FINALIZADO("Finalizado");

    private final String label;
}
