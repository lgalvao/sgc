package sgc.processo.model;

import lombok.*;

@Getter
@RequiredArgsConstructor
public enum TipoProcesso {
    MAPEAMENTO("Mapeamento"),
    REVISAO("Revisão"),
    DIAGNOSTICO("Diagnóstico");

    private final String label;
}
