package sgc.integracao.mocks;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {
    String tituloEleitoral();

    String nome() default "Usuario de Teste";

    String email() default "teste@sgc.com";

    long unidadeId();

    String[] perfis();
}
