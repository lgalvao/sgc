package sgc.integracao.mocks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import sgc.organizacao.model.*;

import java.util.HashSet;
import java.util.Set;

public class WithMockChefeSecurityContextFactory
        implements WithSecurityContextFactory<WithMockChefe> {
    @Autowired(required = false)
    private UsuarioRepo usuarioRepo;

    @Autowired(required = false)
    private UnidadeRepo unidadeRepo;
    
    @Autowired(required = false)
    private UsuarioPerfilRepo usuarioPerfilRepo;

    @Override
    public SecurityContext createSecurityContext(WithMockChefe annotation) {
        Unidade unidade = null;
        if (unidadeRepo != null) {
            try {
                unidade = unidadeRepo.findById(10L).orElse(null);
            } catch (Exception e) {
                System.err.println(e.getMessage());
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
                // Carregar atribuições do banco de dados se o usuário existir
                if (usuario != null && usuarioPerfilRepo != null) {
                    var atribuicoes = usuarioPerfilRepo.findByUsuarioTitulo(annotation.value());
                    usuario.setAtribuicoes(new HashSet<>(atribuicoes));
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }

        if (usuario == null) {
            usuario = Usuario.builder()
                    .tituloEleitoral(annotation.value())
                    .nome("Chefe Teste")
                    .email("chefe@teste.com")
                    .ramal("123")
                    .unidadeLotacao(unidade)
                    .build();

            Set<UsuarioPerfil> atribuicoes = new HashSet<>();
            atribuicoes.add(
                            UsuarioPerfil.builder()
                                    .usuario(usuario)
                                    .unidade(unidade)
                                    .perfil(Perfil.CHEFE)
                                    .build());
            usuario.setAtribuicoes(atribuicoes);

        }

        // Garante que a unidade está correta no usuário do contexto
        usuario.setUnidadeLotacao(unidade);

        Set<UsuarioPerfil> atribuicoes = new HashSet<>(usuario.getAtribuicoes());
        if (atribuicoes.stream().noneMatch(a -> a.getPerfil() == Perfil.CHEFE)) {
            // Usuário existe mas não tem perfil CHEFE, adicionar com a unidade carregada
            atribuicoes.add(
                            UsuarioPerfil.builder()
                                    .usuario(usuario)
                                    .unidade(unidade)
                                    .perfil(Perfil.CHEFE)
                                    .build());
            usuario.setAtribuicoes(atribuicoes);
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        return context;
    }
}
