package sgc.integracao.mocks;

import org.springframework.security.test.context.support.*;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockChefeSecurityContextFactory.class)
public @interface WithMockChefe {
    String value() default "111111111111";
}
