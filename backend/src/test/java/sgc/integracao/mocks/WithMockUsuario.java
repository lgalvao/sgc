package sgc.integracao.mocks;

import org.springframework.security.test.context.support.*;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockUsuarioSecurityContextFactory.class)
public @interface WithMockUsuario {
    String tituloEleitoral() default "222222222222";
    String nome() default "Usuário Comum";
}
