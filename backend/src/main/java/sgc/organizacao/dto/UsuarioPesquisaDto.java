package sgc.organizacao.dto;

import lombok.Builder;

@Builder
public record UsuarioPesquisaDto(
        String tituloEleitoral,
        String nome
) {
}
