package sgc.processo.eventos;

public class SubprocessoRevisaoDisponibilizadaEvento {

    private final Long subprocessoId;

    public SubprocessoRevisaoDisponibilizadaEvento(Long subprocessoId) {
        this.subprocessoId = subprocessoId;
    }

    public Long getSubprocessoId() {
        return subprocessoId;
    }
}