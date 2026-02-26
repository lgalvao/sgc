package sgc.integracao.mocks;

import org.springframework.security.test.context.support.*;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {
    String tituloEleitoral();

    String nome() default "Usuario de Teste";

    String email() default "teste@sgc.com";

    long unidadeId();

    String[] perfis();
}
