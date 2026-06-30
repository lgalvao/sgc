package sgc.diagnostico.dto;

import lombok.*;

import java.util.*;

@Builder
public record DiagnosticoEquipeDto(
        List<Item> servidores
) {
    @Builder
    public record Item(
            String servidorTitulo,
            String servidorNome,
            String situacaoServidor,
            boolean podeManterConsenso,
            boolean podeImpossibilitar,
            boolean podePermitirAvaliacao
    ) {
    }
}
