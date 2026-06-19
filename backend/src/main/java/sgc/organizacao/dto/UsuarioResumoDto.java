package sgc.organizacao.dto;

import lombok.Builder;

@Builder
public record UsuarioResumoDto(
        String tituloEleitoral,
        String matricula,
        String nome,
        String email,
        String ramal
) {
}
