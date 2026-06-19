package sgc.organizacao.dto;

import java.util.List;

public record GrupoViolacaoOrganizacionalDto(
        String tipo,
        int quantidadeOcorrencias,
        List<String> ocorrencias
) {
}
