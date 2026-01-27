/**
 * Eventos de domínio do módulo de Subprocesso.
 *
 * <p>Este pacote contém os eventos de domínio que representam transições de estado
 * e ações significativas no ciclo de vida de um {@link sgc.subprocesso.model.Subprocesso}.
 *
 * <h2>Design Unificado ⭐</h2>
 * <p>Diferente de outros módulos que têm múltiplos eventos, o módulo de subprocesso
 * usa um <strong>evento unificado</strong>: {@link sgc.subprocesso.eventos.EventoTransicaoSubprocesso}.
 *
 * <p>Este evento representa TODAS as transições de estado possíveis, utilizando um
 * enum {@link sgc.subprocesso.eventos.TipoTransicao} para diferenciar o tipo específico.
 *
 * <h2>Vantagens do Design Unificado</h2>
 * <ul>
 *   <li><strong>Menos classes</strong>: 1 evento em vez de 15+ eventos separados</li>
 *   <li><strong>Consistência</strong>: Todos os dados disponíveis em um único lugar</li>
 *   <li><strong>Simplicidade</strong>: Listeners podem filtrar por tipo se necessário</li>
 *   <li><strong>Extensibilidade</strong>: Adicionar novo tipo de transição = adicionar enum, não nova classe</li>
 * </ul>
 *
 * <h2>Tipos de Transição Suportados</h2>
 * <p>Via {@link sgc.subprocesso.eventos.TipoTransicao}:
 * <ul>
 *   <li>{@code CADASTRO_DISPONIBILIZADO} - Cadastro disponibilizado pelo CHEFE</li>
 *   <li>{@code CADASTRO_DEVOLVIDO} - Cadastro devolvido pelo GESTOR</li>
 *   <li>{@code CADASTRO_ACEITO} - Cadastro aceito pelo GESTOR</li>
 *   <li>{@code CADASTRO_HOMOLOGADO} - Cadastro homologado pelo ADMIN</li>
 *   <li>{@code REVISAO_CADASTRO_DISPONIBILIZADA} - Revisão de cadastro disponibilizada</li>
 *   <li>{@code REVISAO_CADASTRO_DEVOLVIDA} - Revisão de cadastro devolvida</li>
 *   <li>{@code REVISAO_CADASTRO_ACEITA} - Revisão de cadastro aceita</li>
 *   <li>{@code REVISAO_CADASTRO_HOMOLOGADA} - Revisão de cadastro homologada</li>
 *   <li>{@code MAPA_DISPONIBILIZADO} - Mapa disponibilizado para validação</li>
 *   <li>{@code SUGESTOES_APRESENTADAS} - Sugestões apresentadas ao mapa</li>
 *   <li>{@code MAPA_VALIDADO} - Mapa validado pelo CHEFE</li>
 *   <li>{@code MAPA_DEVOLVIDO} - Mapa devolvido pelo GESTOR</li>
 *   <li>{@code MAPA_ACEITO} - Mapa aceito pelo GESTOR</li>
 *   <li>{@code MAPA_HOMOLOGADO} - Mapa homologado pelo ADMIN</li>
 *   <li>{@code MAPA_AJUSTADO} - Mapa ajustado pelo ADMIN</li>
 * </ul>
 *
 * <h2>Como Usar</h2>
 *
 * <h3>Publicar uma Transição:</h3>
 * <pre>{@code
 * @Service
 * public class SubprocessoCadastroWorkflowService {
 *     private final ApplicationEventPublisher eventPublisher;
 *
 *     public void disponibilizarCadastro(Long codSubprocesso, Usuario usuario) {
 *         // ... lógica de transição
 *         eventPublisher.publishEvent(new EventoTransicaoSubprocesso(
 *             codSubprocesso,
 *             SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO,
 *             TipoTransicao.CADASTRO_DISPONIBILIZADO,
 *             usuario.getTituloEleitoral()
 *         ));
 *     }
 * }
 * }</pre>
 *
 * <h3>Escutar Transições (Todas):</h3>
 * <pre>{@code
 * @Component
 * public class SubprocessoAuditoriaListener {
 *     @EventListener
 *     public void aoTransicao(EventoTransicaoSubprocesso evento) {
 *         // Captura TODAS as transições para auditoria
 *         logger.info("Subprocesso {} transitou para {}",
 *             evento.codigoSubprocesso(), evento.novaSituacao());
 *     }
 * }
 * }</pre>
 *
 * <h3>Escutar Transições Específicas:</h3>
 * <pre>{@code
 * @Component
 * public class EmailListener {
 *     @EventListener
 *     public void aoTransicao(EventoTransicaoSubprocesso evento) {
 *         if (evento.tipoTransicao() == TipoTransicao.CADASTRO_DISPONIBILIZADO) {
 *             // Enviar email apenas quando cadastro for disponibilizado
 *         }
 *     }
 * }
 * }</pre>
 *
 * <h2>Padrão Recomendado</h2>
 * <p>Este design unificado é <strong>recomendado</strong> para outros módulos que tenham
 * múltiplas transições de estado similares. Considere usar este padrão em vez de
 * criar muitos eventos separados.
 *
 * @see sgc.processo.eventos Eventos de processo (eventos separados)
 * @see sgc.mapa.evento Eventos de mapa
 * @since 1.0
 */
@NullMarked
package sgc.subprocesso.eventos;

import org.jspecify.annotations.NullMarked;
