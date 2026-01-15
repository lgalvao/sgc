/**
 * Services relacionados a notificações, emails e comunicação de subprocessos.
 * <p>
 * Este pacote contém os serviços responsáveis por:
 * <ul>
 *   <li>Envio de emails relacionados a eventos de subprocessos</li>
 *   <li>Criação de alertas para usuários</li>
 *   <li>Processamento assíncrono de eventos de comunicação</li>
 * </ul>
 *
 * <h2>Services Principais</h2>
 * <ul>
 *   <li>{@link sgc.subprocesso.service.notificacao.SubprocessoEmailService} - Envio de emails</li>
 *   <li>{@link sgc.subprocesso.service.notificacao.SubprocessoComunicacaoListener} - Listener assíncrono de eventos</li>
 * </ul>
 *
 * <h2>Organização Arquitetural</h2>
 * <p>
 * Estes services processam eventos de forma <strong>assíncrona</strong> (ADR-002, Fase 3).
 * Falhas na comunicação não devem afetar o workflow principal de subprocessos.
 * </p>
 *
 * <p>
 * Os services são especializados e devem ser acessados apenas através da
 * {@link sgc.subprocesso.service.SubprocessoFacade}, conforme ADR-001 (Facade Pattern)
 * e ADR-006 (Domain Aggregates Organization - Fase 4).
 * </p>
 *
 * @see sgc.subprocesso.service.SubprocessoFacade
 * @see sgc.subprocesso.eventos.EventoTransicaoSubprocesso
 * @since 1.0
 */
@NullMarked
package sgc.subprocesso.service.notificacao;

import org.jspecify.annotations.NullMarked;
