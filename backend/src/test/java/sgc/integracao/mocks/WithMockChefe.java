package sgc.integracao.mocks;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockChefeSecurityContextFactory.class)
public @interface WithMockChefe {
    String value() default "111111111111";
}
