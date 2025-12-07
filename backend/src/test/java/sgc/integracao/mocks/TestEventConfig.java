package sgc.integracao.mocks;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import sgc.notificacao.EventoProcessoListener;
import sgc.processo.eventos.EventoProcessoIniciado;

/**
 * Configuração de teste que substitui o EventoProcessoListener real por um no-op para evitar
 * problemas com transações em testes de integração.
 */
@TestConfiguration
@Profile("test")
public class TestEventConfig {
    @Bean
    @Primary
    public EventoProcessoListener eventoProcessoListener() {
        return new EventoProcessoListener(null, null, null, null, null, null) {
            @Override
            public void aoIniciarProcesso(EventoProcessoIniciado evento) {
                // No-op para testes
            }
        };
    }
}
