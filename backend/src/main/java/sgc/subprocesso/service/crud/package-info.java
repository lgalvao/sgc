/**
 * Services relacionados a operações CRUD e validações de subprocessos.
 * <p>
 * Este pacote contém os serviços responsáveis por:
 * <ul>
 *   <li>Operações básicas de CRUD (Create, Read, Update, Delete) de subprocessos</li>
 *   <li>Validações de regras de negócio de subprocessos</li>
 *   <li>Verificações de integridade de dados</li>
 * </ul>
 *
 * <h2>Services Principais</h2>
 * <ul>
 *   <li>{@link sgc.subprocesso.service.crud.SubprocessoCrudService} - Operações CRUD básicas</li>
 *   <li>{@link sgc.subprocesso.service.crud.SubprocessoValidacaoService} - Validações de negócio</li>
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
 * @see sgc.subprocesso.model.Subprocesso
 * @since 1.0
 */
package sgc.subprocesso.service.crud;
