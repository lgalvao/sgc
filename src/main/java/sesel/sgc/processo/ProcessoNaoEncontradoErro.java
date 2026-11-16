package sesel.sgc.processo;

import java.util.UUID;

public class ProcessoNaoEncontradoErro extends RuntimeException {
    public ProcessoNaoEncontradoErro(String id) {
        super("Processo não encontrado com ID: " + id);
    }
    
    public ProcessoNaoEncontradoErro(UUID id) {
        this(id != null ? id.toString() : "null");
    }
}
