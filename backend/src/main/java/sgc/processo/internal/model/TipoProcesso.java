package sgc.processo.internal.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoProcesso {
    MAPEAMENTO("Mapeamento"),
    REVISAO("Revisão"),
    DIAGNOSTICO("Diagnóstico");

    private final String label;
}
