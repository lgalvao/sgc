package sgc.organizacao;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.model.Usuario;

@Service
@RequiredArgsConstructor
public class OrganizacaoFacade {
    private final UsuarioFacade usuarioFacade;
    private final UnidadeFacade unidadeFacade;

    public Usuario obterUsuarioAutenticado() {
        return usuarioFacade.obterUsuarioAutenticado();
    }

    public @Nullable String extrairTituloUsuario(@Nullable Object principal) {
        return usuarioFacade.extrairTituloUsuario(principal);
    }

    public Usuario buscarPorLogin(String login) {
        return usuarioFacade.buscarPorLogin(login);
    }

    public UnidadeDto buscarUnidadePorSigla(String sigla) {
        return unidadeFacade.buscarPorSigla(sigla);
    }
}
