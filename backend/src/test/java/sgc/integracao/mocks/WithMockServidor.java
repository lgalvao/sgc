package sgc.integracao.mocks;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockServidorSecurityContextFactory.class)
public @interface WithMockServidor {
    String tituloEleitoral() default "222222222222";
    String nome() default "Servidor de Teste";
}
