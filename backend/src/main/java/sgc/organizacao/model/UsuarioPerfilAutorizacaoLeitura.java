package sgc.organizacao.model;

import lombok.*;
import org.jspecify.annotations.*;

@Builder
public record UsuarioPerfilAutorizacaoLeitura(
        String usuarioTitulo,
        Perfil perfil,
        Long unidadeCodigo,
        @Nullable String unidadeNome,
        @Nullable String unidadeSigla,
        @Nullable TipoUnidade unidadeTipo,
        @Nullable SituacaoUnidade unidadeSituacao
) {
}
