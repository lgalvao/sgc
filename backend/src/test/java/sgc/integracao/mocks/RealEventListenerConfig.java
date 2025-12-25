package sgc.integracao.mocks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.event.TransactionalEventListener;
import sgc.alerta.AlertaService;
import sgc.notificacao.NotificacaoEmailService;
import sgc.notificacao.internal.services.NotificacaoModelosService;
import sgc.processo.api.eventos.EventoProcessoFinalizado;
import sgc.processo.api.eventos.EventoProcessoIniciado;
import sgc.processo.api.model.ProcessoRepo;
import sgc.sgrh.SgrhService;
import sgc.subprocesso.internal.model.SubprocessoRepo;

/**
 * Configuração de teste que fornece um EventoProcessoListener SÍNCRONO.
 * Usado em testes de integração que precisam verificar o comportamento completo
 * dos event listeners, incluindo envio de notificações.
 * 
 * <p>Esta configuração usa @EventListener e @TransactionalEventListener
 * em vez de @ApplicationModuleListener + @Async, permitindo processamento
 * síncrono dos eventos durante os testes.
 */
@TestConfiguration
public class RealEventListenerConfig {

    @Bean
    @Primary
    public SyncEventoProcessoListener syncEventoProcessoListener(
            AlertaService servicoAlertas,
            NotificacaoEmailService notificacaoEmailService,
            NotificacaoModelosService notificacaoModelosService,
            SgrhService sgrhService,
            ProcessoRepo processoRepo,
            SubprocessoRepo repoSubprocesso) {
        
        return new SyncEventoProcessoListener(
                servicoAlertas,
                notificacaoEmailService,
                notificacaoModelosService,
                sgrhService,
                processoRepo,
                repoSubprocesso);
    }

    /**
     * Listener síncrono de eventos de processo para testes.
     * Replica a lógica do EventoProcessoListener mas sem @Async.
     */
    @Slf4j
    @RequiredArgsConstructor
    public static class SyncEventoProcessoListener {
        private final AlertaService servicoAlertas;
        private final NotificacaoEmailService notificacaoEmailService;
        private final NotificacaoModelosService notificacaoModelosService;
        private final SgrhService sgrhService;
        private final ProcessoRepo processoRepo;
        private final SubprocessoRepo repoSubprocesso;

        @EventListener
        public void aoIniciarProcesso(EventoProcessoIniciado evento) {
            log.debug("[TESTE] Processando evento de processo iniciado (síncrono): {}", evento.getCodProcesso());
            // Delega para a mesma lógica do listener real
            sgc.notificacao.internal.listeners.EventoProcessoListener delegado =
                new sgc.notificacao.internal.listeners.EventoProcessoListener(
                    servicoAlertas,
                    notificacaoEmailService,
                    notificacaoModelosService,
                    sgrhService,
                    processoRepo,
                    repoSubprocesso);
            delegado.aoIniciarProcesso(evento);
        }

        @EventListener
        public void aoFinalizarProcesso(EventoProcessoFinalizado evento) {
            log.debug("[TESTE] Processando evento de processo finalizado (síncrono): {}", evento.getCodProcesso());
            // Delega para a mesma lógica do listener real
            sgc.notificacao.internal.listeners.EventoProcessoListener delegado =
                new sgc.notificacao.internal.listeners.EventoProcessoListener(
                    servicoAlertas,
                    notificacaoEmailService,
                    notificacaoModelosService,
                    sgrhService,
                    processoRepo,
                    repoSubprocesso);
            delegado.aoFinalizarProcesso(evento);
        }
    }
}
