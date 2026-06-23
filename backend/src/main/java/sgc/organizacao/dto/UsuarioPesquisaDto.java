package sgc.organizacao.dto;

import lombok.*;

@Builder
public record UsuarioPesquisaDto(
        String tituloEleitoral,
        String nome
) {
}
