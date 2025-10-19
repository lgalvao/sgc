package sgc.integracao.mocks;

import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {
    TestExecutionEvent setupBefore() default TestExecutionEvent.TEST_METHOD;
    long tituloEleitoral();
    String nome() default "Usuario de Teste";
    String email() default "teste@sgc.com";
    long unidadeId();
    String[] perfis();
}
