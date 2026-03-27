package sgc.organizacao.dto;

import lombok.*;
import org.jspecify.annotations.*;
import sgc.comum.model.*;
import sgc.organizacao.model.*;

@Builder
public record PerfilDto(
        @TituloEleitoral
        @Nullable String usuarioTitulo,

        @Nullable Long unidadeCodigo,
        @Nullable String unidadeNome,
        String perfil,
        String descricao) {

    public static PerfilDto from(Perfil perfil) {
        return new PerfilDto(null, null, null, perfil.name(), perfil.name());
    }
}
