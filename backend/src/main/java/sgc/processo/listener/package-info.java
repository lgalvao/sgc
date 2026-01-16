/**
 * Listeners de eventos de processo.
 *
 * <p>Este pacote contém os listeners que reagem a eventos de domínio publicados
 * pelo módulo de processo. Os listeners são responsáveis por orquestrar ações
 * assíncronas como criação de alertas e envio de notificações.
 *
 * <h2>Componentes</h2>
 * <ul>
 *   <li>{@link sgc.processo.listener.EventoProcessoListener} - Listener principal que
 *       processa eventos de processo iniciado e finalizado, criando alertas e
 *       enviando e-mails para as unidades participantes.</li>
 * </ul>
 *
 * <h2>Arquitetura</h2>
 * <p>Os listeners são componentes Spring que usam {@code @EventListener} para
 * se inscrever nos eventos publicados via {@code ApplicationEventPublisher}.
 * São executados de forma assíncrona ({@code @Async}) para não bloquear as
 * transações principais do workflow.
 *
 * @see sgc.processo.eventos
 * @see sgc.notificacao
 * @see sgc.alerta
 */
@org.jspecify.annotations.NullMarked
package sgc.processo.listener;
