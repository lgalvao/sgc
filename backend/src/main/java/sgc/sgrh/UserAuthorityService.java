package sgc.sgrh;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import sgc.comum.modelo.AdministradorRepo;
import sgc.unidade.modelo.Unidade;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class UserAuthorityService {

    private final AdministradorRepo administradorRepo;

    public UserAuthorityService(AdministradorRepo administradorRepo) {
        this.administradorRepo = administradorRepo;
    }

    public Collection<? extends GrantedAuthority> determineAuthorities(Usuario usuario) {
        if (administradorRepo.existsByUsuario(usuario)) {
            return Stream.of(new SimpleGrantedAuthority("ROLE_ADMIN")).collect(Collectors.toList());
        }

        Unidade unidade = usuario.getUnidade();
        if (unidade != null && usuario.equals(unidade.getTitular())) {
            switch (unidade.getTipo()) {
                case INTERMEDIARIA:
                    return Stream.of(new SimpleGrantedAuthority("ROLE_GESTOR")).collect(Collectors.toList());
                case OPERACIONAL:
                case INTEROPERACIONAL:
                    return Stream.of(new SimpleGrantedAuthority("ROLE_CHEFE")).collect(Collectors.toList());
                default:
                    // Fallback para qualquer outro tipo de unidade com titular
                    return Stream.of(new SimpleGrantedAuthority("ROLE_CHEFE")).collect(Collectors.toList());
            }
        }

        return Stream.of(new SimpleGrantedAuthority("ROLE_SERVIDOR")).collect(Collectors.toList());
    }
}