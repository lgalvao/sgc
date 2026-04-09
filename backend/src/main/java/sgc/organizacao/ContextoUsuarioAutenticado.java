package sgc.organizacao;

import sgc.organizacao.model.*;

import java.util.*;

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
