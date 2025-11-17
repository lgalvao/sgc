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

    @Autowired
    private UsuarioRepo usuarioRepo;
    @Autowired
    private UnidadeRepo unidadeRepo;

    @Override
    public SecurityContext createSecurityContext(WithMockChefe annotation) {
        Unidade unidade = unidadeRepo.findById(10L).orElseThrow();

        // Busca o usuário ou cria um novo para evitar instâncias duplicadas
        Usuario usuario = usuarioRepo.findById(annotation.value())
            .orElseGet(() -> {
                Usuario novoUsuario = new Usuario(
                    annotation.value(),
                    "Chefe Teste",
                    "chefe@teste.com",
                    "123",
                    unidade,
                    Set.of(Perfil.CHEFE)
                );
                return usuarioRepo.save(novoUsuario);
            });

        // Garante que a unidade está correta no usuário do contexto, pois o usuário
        // pode existir no banco de dados com uma unidade diferente.
        usuario.setUnidade(unidade);
        usuarioRepo.save(usuario);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            usuario, null, usuario.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        return context;
    }
}