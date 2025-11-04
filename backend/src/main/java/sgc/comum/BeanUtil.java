package sgc.comum;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Utilitário para acesso estático a beans gerenciados pelo Spring.
 * <p>
 * Esta classe implementa {@link ApplicationContextAware} para obter uma referência
 * ao contexto da aplicação Spring, permitindo que classes não gerenciadas
 * (como as factories de anotações de segurança) acessem beans de serviço.
 */
@Component
// TODO essa classe está me cheirando a gambiarra. Precisa mesmo?
public class BeanUtil implements ApplicationContextAware {
    private static ApplicationContext context;

    /**
     * Injeta o {@link ApplicationContext} do Spring no momento da inicialização.
     * Este método é chamado automaticamente pelo Spring.
     *
     * @param applicationContext o contexto da aplicação a ser injetado.
     * @throws BeansException se houver um erro ao processar o contexto.
     */
    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    /**
     * Obtém uma instância de um bean gerenciado pelo Spring a partir de sua classe.
     *
     * @param beanClass a classe do bean a ser recuperado.
     * @param <T> o tipo do bean.
     * @return a instância do bean.
     */
    public static <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }
}
