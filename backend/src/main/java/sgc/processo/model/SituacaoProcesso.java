package sgc.processo.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SituacaoProcesso {
    CRIADO("Criado"),
    EM_ANDAMENTO("Em Andamento"),
    FINALIZADO("Finalizado");

    private final String label;
}
