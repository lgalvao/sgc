package sgc.organizacao.model;

import org.jspecify.annotations.*;

public record UsuarioPerfilAutorizacaoLeitura(
        String usuarioTitulo,
        Perfil perfil,
        Long unidadeCodigo,
        @Nullable String unidadeNome,
        @Nullable String unidadeSigla,
        TipoUnidade unidadeTipo,
        SituacaoUnidade unidadeSituacao
) {
}
