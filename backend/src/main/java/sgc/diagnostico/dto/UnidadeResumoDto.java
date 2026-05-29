package sgc.diagnostico.dto;


public record UnidadeResumoDto(
        Long unidadeCodigo,
        String unidadeSigla,
        String unidadeNome,
        String situacaoSubprocesso
) {
}
