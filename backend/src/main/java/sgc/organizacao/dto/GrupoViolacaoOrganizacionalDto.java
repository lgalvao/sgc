package sgc.organizacao.dto;

import java.util.*;

public record GrupoViolacaoOrganizacionalDto(
        String tipo,
        int quantidadeOcorrencias,
        List<String> ocorrencias
) {
}
