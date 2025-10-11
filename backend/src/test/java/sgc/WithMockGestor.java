package sgc;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockGestorSecurityContextFactory.class)
public @interface WithMockGestor {
    String value() default "gestor_unidade";
}