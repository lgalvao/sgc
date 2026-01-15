/**
 * Factory para criação e inicialização de subprocessos.
 * <p>
 * Este pacote contém a factory responsável por:
 * <ul>
 *   <li>Criar novas instâncias de subprocessos</li>
 *   <li>Inicializar subprocessos com valores padrão</li>
 *   <li>Configurar estado inicial de subprocessos</li>
 * </ul>
 *
 * <h2>Factory Principal</h2>
 * <ul>
 *   <li>{@link sgc.subprocesso.service.factory.SubprocessoFactory} - Factory de criação</li>
 * </ul>
 *
 * <h2>Uso Cross-Module</h2>
 * <p>
 * <strong>Atenção:</strong> Esta factory é utilizada por outros módulos (ex: {@code ProcessoInicializador}),
 * portanto deve permanecer <strong>pública</strong> e com API estável.
 * </p>
 *
 * <h2>Organização Arquitetural</h2>
 * <p>
 * A factory pode ser acessada diretamente por outros módulos devido ao seu papel na inicialização
 * de processos. Dentro do módulo subprocesso, deve ser acessada através da
 * {@link sgc.subprocesso.service.SubprocessoFacade}, conforme ADR-001 (Facade Pattern)
 * e ADR-006 (Domain Aggregates Organization - Fase 4).
 * </p>
 *
 * @see sgc.subprocesso.service.SubprocessoFacade
 * @see sgc.subprocesso.model.Subprocesso
 * @see sgc.processo.service.ProcessoInicializador
 * @since 1.0
 */
@NullMarked
package sgc.subprocesso.service.factory;

import org.jspecify.annotations.NullMarked;
