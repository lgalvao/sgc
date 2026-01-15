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
 * <h2>Services Principais</h2>
 * <ul>
 *   <li>{@link sgc.subprocesso.service.workflow.SubprocessoCadastroWorkflowService} - Workflow de cadastro</li>
 *   <li>{@link sgc.subprocesso.service.workflow.SubprocessoMapaWorkflowService} - Workflow de mapa</li>
 *   <li>{@link sgc.subprocesso.service.workflow.SubprocessoTransicaoService} - Transições de estado</li>
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
package sgc.subprocesso.service.workflow;
