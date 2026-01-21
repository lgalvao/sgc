/**
 * Services relacionados ao workflow e transições de estado de subprocessos.
 * <p>
 * Este pacote contém os serviços responsáveis por:
 * <ul>
 *   <li>Gerenciar o workflow de cadastro de subprocessos (disponibilização, homologação, etc.)</li>
 *   <li>Gerenciar o workflow de mapa de subprocessos (disponibilização, homologação, etc.)</li>
 *   <li>Executar transições de estado entre diferentes situações do subprocesso</li>
 * </ul>
 *
 * <h2>Organização Arquitetural</h2>
 * <p>
 * Estes services são especializados e devem ser acessados apenas através da
 * {@link sgc.subprocesso.service.SubprocessoFacade}, conforme ADR-001 (Facade Pattern)
 * e ADR-006 (Domain Aggregates Organization - Fase 4).
 * </p>
 *
 * @see sgc.subprocesso.service.SubprocessoFacade
 * @see sgc.subprocesso.eventos.EventoTransicaoSubprocesso
 * @since 1.0
 */
@NullMarked
package sgc.subprocesso.service.workflow;

import org.jspecify.annotations.NullMarked;
