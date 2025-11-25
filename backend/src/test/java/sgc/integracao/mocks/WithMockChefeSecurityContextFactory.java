package sgc.integracao.mocks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import sgc.sgrh.model.Perfil;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioRepo;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import java.util.Set;

public class WithMockChefeSecurityContextFactory implements WithSecurityContextFactory<WithMockChefe> {

    @Autowired(required = false)
    private UsuarioRepo usuarioRepo;
    @Autowired(required = false)
    private UnidadeRepo unidadeRepo;

    @Override
    public SecurityContext createSecurityContext(WithMockChefe annotation) {
        Unidade unidade = null;
        boolean dbAvailable = false;
        if (unidadeRepo != null) {
            try {
                unidade = unidadeRepo.findById(10L).orElse(null);
                dbAvailable = true;
            } catch (Exception e) {
                unidade = null;
            }
        }
        if (unidade == null) {
             unidade = new Unidade("Unidade Mock", "SESEL");
             unidade.setCodigo(10L);
        }

        Usuario usuario = null;
        if (usuarioRepo != null) {
            try {
                usuario = usuarioRepo.findById(annotation.value()).orElse(null);
            } catch (Exception e) {
                usuario = null;
            }
        }

        if (usuario == null) {
            usuario = new Usuario(
                annotation.value(),
                "Chefe Teste",
                "chefe@teste.com",
                "123",
                unidade,
                Set.of(Perfil.CHEFE)
            );
            if (dbAvailable && usuarioRepo != null) {
                try { usuarioRepo.save(usuario); } catch (Exception e) { }
            }
        }

        // Garante que a unidade está correta no usuário do contexto
        usuario.setUnidade(unidade);
        usuario.setPerfis(Set.of(Perfil.CHEFE));
        if (dbAvailable && usuarioRepo != null) {
            try { usuarioRepo.save(usuario); } catch (Exception e) { }
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            usuario, null, usuario.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        return context;
    }
}