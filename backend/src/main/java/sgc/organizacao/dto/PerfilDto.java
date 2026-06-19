package sgc.organizacao.dto;

import lombok.Builder;
import sgc.comum.model.TituloEleitoral;

@Builder
public record PerfilDto(
        @TituloEleitoral
        String usuarioTitulo,

        Long unidadeCodigo,
        String unidadeNome,
        String perfil,
        String descricao) {
}
