package sgc.integracao.mocks;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.stereotype.Component;
import sgc.comum.BeanUtil;
import sgc.sgrh.Perfil;
import sgc.sgrh.Usuario;
import sgc.sgrh.UsuarioRepo;

import java.util.Set;

@Component
public class WithMockChefeSecurityContextFactory implements WithSecurityContextFactory<WithMockChefe> {

    @Override
    public SecurityContext createSecurityContext(WithMockChefe annotation) {
        UsuarioRepo usuarioRepo = BeanUtil.getBean(UsuarioRepo.class);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        long chefeId;
        try {
            chefeId = Long.parseLong(annotation.value());
        } catch (NumberFormatException e) {
            chefeId = 333333333333L; // Default value
        }

        final Long finalChefeId = chefeId;
        Usuario usuario = usuarioRepo.findByTituloEleitoral(finalChefeId)
            .orElseGet(() -> {
                Usuario newUser = new Usuario();
                newUser.setTituloEleitoral(finalChefeId);
                newUser.setNome("Chefe User");
                newUser.setEmail("chefe@example.com");
                newUser.setPerfis(Set.of(Perfil.CHEFE));
                return usuarioRepo.save(newUser);
            });

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
        context.setAuthentication(token);
        return context;
    }
}