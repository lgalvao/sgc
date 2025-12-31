package sgc.comum.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Utilitário para acesso estático a beans do Spring.
 */
@Component
public class BeanUtil implements ApplicationContextAware {
    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        context = applicationContext;
    }

    public static <T> T getBean(Class<T> beanClass) {
        if (context == null) {
            return null; // ou lançar exceção dependendo da estratégia
        }
        return context.getBean(beanClass);
    }
}
