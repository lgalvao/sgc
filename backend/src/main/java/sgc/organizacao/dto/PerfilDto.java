package sgc.organizacao.dto;

import lombok.*;
import sgc.comum.model.*;

@Builder
public record PerfilDto(
        @TituloEleitoral
        String usuarioTitulo,

        Long unidadeCodigo,
        String unidadeNome,
        String perfil,
        String descricao) {
}
