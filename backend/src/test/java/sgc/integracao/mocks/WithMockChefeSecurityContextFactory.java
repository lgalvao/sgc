package sgc.integracao.mocks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.stereotype.Component;
import sgc.sgrh.CustomUserDetailsService;

@Component
public class WithMockChefeSecurityContextFactory implements WithSecurityContextFactory<WithMockChefe> {

    private final CustomUserDetailsService userDetailsService;

    @Autowired
    public WithMockChefeSecurityContextFactory(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public SecurityContext createSecurityContext(WithMockChefe customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        UserDetails principal = userDetailsService.loadUserByUsername("chefe");
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, "password", principal.getAuthorities());
        context.setAuthentication(auth);
        return context;
    }
}