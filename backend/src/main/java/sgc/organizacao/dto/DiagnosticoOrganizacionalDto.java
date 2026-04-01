package sgc.organizacao.dto;

import java.util.*;

public record DiagnosticoOrganizacionalDto(
        boolean possuiViolacoes,
        String resumo,
        int quantidadeTiposViolacao,
        int quantidadeOcorrencias,
        List<GrupoViolacaoOrganizacionalDto> grupos
) {

    public static DiagnosticoOrganizacionalDto semViolacoes() {
        return new DiagnosticoOrganizacionalDto(false, "", 0, 0, List.of());
    }
}
