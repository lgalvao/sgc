package sgc.organizacao.dto;

import lombok.*;
import sgc.comum.model.*;
import sgc.organizacao.model.*;

@Builder
public record PerfilDto(
        @TituloEleitoral
        String usuarioTitulo,

        Long unidadeCodigo,
        String unidadeNome,
        String perfil,
        String descricao) {

        public static PerfilDto from(Perfil perfil) {
                return new PerfilDto(null, null, null, perfil.name(), perfil.name());
        }
}
