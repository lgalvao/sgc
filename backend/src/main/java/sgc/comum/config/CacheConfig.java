package sgc.comum.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração de cache da aplicação.
 * 
 * <p>Utilizado para armazenar em memória dados que mudam raramente,
 * como a árvore de unidades organizacionais.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("arvoreUnidades", "unidadeDescendentes");
    }
}
