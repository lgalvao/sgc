package sgc.integracao.mocks;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import sgc.sgrh.modelo.Perfil;
import sgc.sgrh.modelo.Usuario;

import java.util.Set;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.context.SecurityContext;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.test.context.support.WithSecurityContextFactory;

import sgc.sgrh.modelo.Perfil;

import sgc.sgrh.modelo.Usuario;

import sgc.unidade.modelo.Unidade;



import java.util.Set;



public class WithMockChefeSecurityContextFactory implements WithSecurityContextFactory<WithMockChefe> {



    @Override

    public SecurityContext createSecurityContext(WithMockChefe annotation) {

        SecurityContext context = SecurityContextHolder.createEmptyContext();

        long chefeId;

        try {

            chefeId = Long.parseLong(annotation.value());

        } catch (NumberFormatException e) {

            chefeId = 333333333333L; // Default value

        }



        Usuario usuario = new Usuario();

        usuario.setTituloEleitoral(chefeId);

        usuario.setNome("Chefe User");

        usuario.setEmail("chefe@example.com");

        usuario.setPerfis(Set.of(Perfil.CHEFE));

        usuario.setUnidade(new Unidade("Unidade Mock", "UM"));



        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());

        context.setAuthentication(token);

        return context;

    }

}




