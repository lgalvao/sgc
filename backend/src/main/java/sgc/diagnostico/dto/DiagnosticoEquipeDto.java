package sgc.diagnostico.dto;

import java.util.List;

public record DiagnosticoEquipeDto(
        List<Item> servidores
) {
    public record Item(
            String servidorTitulo,
            String servidorNome,
            String situacaoServidor
    ) {}
}