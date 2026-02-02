package sgc.comum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import java.time.Clock;

/**
 * Configuração geral do Spring Data para serialização de Pagination.
 *
 * <p>Esta classe é responsável pela configuração do suporte a Web do Spring Data, particularmente
 * para o modo de serialização de Page via DTO.
 *
 * <p>Nota: ConfigAplicacao é uma classe separada (@ConfigurationProperties) que carrega
 * propriedades da aplicação (ambiente, URLs). As duas classes servem propósitos diferentes e devem
 * ser mantidas separadas.
 */
@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class Config {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
