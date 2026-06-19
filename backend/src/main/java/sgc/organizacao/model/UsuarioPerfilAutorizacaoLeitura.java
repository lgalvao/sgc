package sgc.organizacao.model;

import lombok.Builder;
import org.jspecify.annotations.Nullable;

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
