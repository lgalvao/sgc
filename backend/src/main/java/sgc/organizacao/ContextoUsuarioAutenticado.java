package sgc.organizacao;

import lombok.Builder;
import sgc.organizacao.model.Perfil;

import java.util.Objects;

@Builder
public record ContextoUsuarioAutenticado(
        String usuarioTitulo,
        Long unidadeAtivaCodigo,
        Perfil perfil
) {
    public ContextoUsuarioAutenticado {
        Objects.requireNonNull(usuarioTitulo, "usuarioTitulo obrigatorio");
        Objects.requireNonNull(unidadeAtivaCodigo, "unidadeAtivaCodigo obrigatorio");
        Objects.requireNonNull(perfil, "perfil obrigatorio");
    }
}
