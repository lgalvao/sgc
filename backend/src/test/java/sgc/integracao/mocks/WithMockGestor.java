package sgc.integracao.mocks;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockGestorSecurityContextFactory.class)
public @interface WithMockGestor {
    String value() default "222222222222";
}
