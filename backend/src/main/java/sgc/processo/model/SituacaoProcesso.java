package sgc.processo.model;

import lombok.*;

@Getter
@RequiredArgsConstructor
public enum SituacaoProcesso {
    CRIADO("Criado"),
    EM_ANDAMENTO("Em andamento"),
    FINALIZADO("Finalizado");

    private final String label;
}
