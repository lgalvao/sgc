package sgc.comum.config;

import com.github.benmanes.caffeine.cache.*;
import org.springframework.cache.*;
import org.springframework.cache.annotation.*;
import org.springframework.cache.caffeine.*;
import org.springframework.context.annotation.*;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CACHE_DIAGNOSTICO_ORGANIZACIONAL = "diagnosticoOrganizacional";
    public static final String CACHE_ARVORE_UNIDADES = "arvoreUnidades";
    public static final String CACHE_MAPA_HIERARQUIA_UNIDADES = "mapaHierarquiaUnidades";
    public static final String CACHE_UNIDADES_COM_MAPA = "unidadesComMapa";
    public static final String CACHE_UNIDADE_ADMIN = "unidadeAdmin";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                CACHE_DIAGNOSTICO_ORGANIZACIONAL,
                CACHE_ARVORE_UNIDADES,
                CACHE_MAPA_HIERARQUIA_UNIDADES,
                CACHE_UNIDADES_COM_MAPA,
                CACHE_UNIDADE_ADMIN
        );
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .recordStats()
                .maximumSize(100)
                .expireAfterWrite(java.time.Duration.ofMinutes(5)));
        cacheManager.registerCustomCache(CACHE_UNIDADE_ADMIN, Caffeine.newBuilder()
                .recordStats()
                .maximumSize(1)
                .expireAfterWrite(java.time.Duration.ofHours(12))
                .build());
        return cacheManager;
    }
}
