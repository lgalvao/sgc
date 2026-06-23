package sgc.diagnostico.dto;

import lombok.*;

@Builder
public record UnidadeResumoDto(
        Long unidadeCodigo,
        String unidadeSigla,
        String unidadeNome,
        String situacaoSubprocesso,
        String responsavelTitulo
) {
}
