package sgc.integracao.mocks;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import sgc.alerta.AlertaService;
import sgc.notificacao.EventoProcessoListener;
import sgc.notificacao.NotificacaoEmailService;
import sgc.notificacao.NotificacaoModelosService;
import sgc.processo.eventos.EventoProcessoIniciado;
import sgc.processo.service.ProcessoService;
import sgc.organizacao.UsuarioService;
import sgc.subprocesso.service.SubprocessoService;

/**
 * Configuração de teste que permite a execução de todos os eventos, exceto aoIniciarProcesso
 * que é substituído por um no-op. Injeta dependências reais (que podem ser mocks do contexto)
 * para garantir que os outros métodos do listener funcionem (ex: criar alertas, enviar emails).
 */
@TestConfiguration
@Profile("test")
public class TestEventConfig {

    @Bean
    @Primary
    public EventoProcessoListener eventoProcessoListener(
            AlertaService servicoAlertas,
            NotificacaoEmailService notificacaoEmailService,
            NotificacaoModelosService notificacaoModelosService,
            UsuarioService usuarioService,
            ProcessoService processoService,
            SubprocessoService subprocessoService) {

        // Retorna uma subclasse anônima com as dependências reais injetadas
        return new EventoProcessoListener(
                servicoAlertas,
                notificacaoEmailService,
                notificacaoModelosService,
                usuarioService,
                processoService,
                subprocessoService) {

            @Override
            public void aoIniciarProcesso(EventoProcessoIniciado evento) {
                // No-op para testes: evita envio massivo de e-mails/alertas na inicialização de processos
            }
        };
    }
}
