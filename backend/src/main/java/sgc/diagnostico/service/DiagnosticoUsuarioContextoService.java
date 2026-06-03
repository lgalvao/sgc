package sgc.diagnostico.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroAcessoNegado;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.service.UsuarioService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiagnosticoUsuarioContextoService {
    private final UsuarioService usuarioService;

    public Usuario usuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Usuario usuario) {
            return usuario;
        }
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new ErroAcessoNegado("Nenhum usuário autenticado no contexto");
        }

        Usuario usuario = usuarioService.buscar(authentication.getName());
        usuarioService.carregarAuthorities(usuario);
        return usuario;
    }
}
