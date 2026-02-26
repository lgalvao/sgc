package sgc.integracao.mocks;

import org.springframework.security.test.context.support.*;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockGestorSecurityContextFactory.class)
public @interface WithMockGestor {
    String value() default "222222222222";
}
