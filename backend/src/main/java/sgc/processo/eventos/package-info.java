/**
 * Eventos de domínio do módulo de Processo.
 *
 * <p>Este pacote contém os eventos de domínio que representam mudanças de estado
 * ou ações significativas no ciclo de vida de um {@link sgc.processo.model.Processo}.
 *
 * <h2>Eventos Implementados</h2>
 * <ul>
 *   <li>{@link sgc.processo.eventos.EventoProcessoCriado} - Disparado quando um processo é criado</li>
 *   <li>{@link sgc.processo.eventos.EventoProcessoIniciado} - Disparado quando um processo é iniciado</li>
 *   <li>{@link sgc.processo.eventos.EventoProcessoFinalizado} - Disparado quando um processo é finalizado</li>
 * </ul>
 *
 * <h2>Padrão de Eventos</h2>
 * <p>Todos os eventos seguem as convenções:
 * <ul>
 *   <li>Nome começa com "Evento" seguido do recurso e ação (ex: {@code EventoProcessoCriado})</li>
 *   <li>São classes imutáveis (records ou classes com campos final)</li>
 *   <li>Contêm apenas os dados essenciais para os listeners</li>
 *   <li>São publicados via {@code ApplicationEventPublisher} do Spring</li>
 * </ul>
 *
 * <h2>Como Usar</h2>
 *
 * <h3>Publicar um Evento:</h3>
 * <pre>{@code
 * @Service
 * public class ProcessoFacade {
 *     private final ApplicationEventPublisher eventPublisher;
 *
 *     public void criarProcesso(CriarProcessoDto dto) {
 *         // ... lógica de criação
 *         eventPublisher.publishEvent(new EventoProcessoCriado(processo.getCodigo()));
 *     }
 * }
 * }</pre>
 *
 * <h3>Escutar um Evento:</h3>
 * <pre>{@code
 * @Component
 * public class NotificacaoListener {
 *     @EventListener
 *     public void aoProcessoCriado(EventoProcessoCriado evento) {
 *         // ... lógica de notificação
 *     }
 * }
 * }</pre>
 *
 * <h2>Benefícios</h2>
 * <ul>
 *   <li><strong>Desacoplamento</strong>: Módulos comunicam-se via eventos, não chamadas diretas</li>
 *   <li><strong>Extensibilidade</strong>: Novos listeners podem ser adicionados sem modificar código existente</li>
 *   <li><strong>Auditoria</strong>: Todos os eventos importantes são capturados</li>
 *   <li><strong>Testabilidade</strong>: Listeners podem ser testados independentemente</li>
 * </ul>
 *
 * @see sgc.subprocesso.eventos Eventos de subprocesso (usa padrão unificado)
 * @see sgc.mapa.evento Eventos de mapa
 * @since 1.0
 */
@NullMarked
package sgc.processo.eventos;

import org.jspecify.annotations.NullMarked;
