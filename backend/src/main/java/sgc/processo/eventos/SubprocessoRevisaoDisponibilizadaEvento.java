package sgc.processo.eventos;

import lombok.Getter;

@Getter
public class SubprocessoRevisaoDisponibilizadaEvento {

    private final Long subprocessoId;

    public SubprocessoRevisaoDisponibilizadaEvento(Long subprocessoId) {
        this.subprocessoId = subprocessoId;
    }

}