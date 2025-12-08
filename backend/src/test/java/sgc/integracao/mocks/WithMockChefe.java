package sgc.integracao.mocks;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockChefeSecurityContextFactory.class)
public @interface WithMockChefe {
    String value() default "111111111111";
}
