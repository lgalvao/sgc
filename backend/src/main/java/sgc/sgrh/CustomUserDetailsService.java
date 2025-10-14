package sgc.sgrh;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepo usuarioRepo;
    private final UserAuthorityService userAuthorityService;

    public CustomUserDetailsService(UsuarioRepo usuarioRepo, UserAuthorityService userAuthorityService) {
        this.usuarioRepo = usuarioRepo;
        this.userAuthorityService = userAuthorityService;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepo.findById(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));

        usuario.setAuthorities(userAuthorityService.determineAuthorities(usuario));

        return usuario;
    }
}