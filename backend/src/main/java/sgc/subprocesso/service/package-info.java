/**
 * Módulo de Subprocessos - Serviços de lógica de negócio.
 * 
 * <h2>Visão Geral</h2>
 * <p>Este pacote contém os serviços que implementam a lógica de negócio do módulo de subprocessos.
 * Segue o <strong>padrão Facade</strong> onde controllers interagem APENAS com {@link sgc.subprocesso.service.SubprocessoFacade},
 * que por sua vez orquestra os services especializados.</p>
 * 
 * <h2>Arquitetura de Services</h2>
 * 
 * <h3>Facade (Public API)</h3>
 * <ul>
 *   <li>{@link sgc.subprocesso.service.SubprocessoFacade} - Ponto de entrada único para todas as operações de subprocesso</li>
 * </ul>
 * 
 * <h3>Workflow Services ({@link sgc.subprocesso.service.workflow})</h3>
 * <p><strong>Função:</strong> Gerenciar transições de estado (workflows)</p>
 * <ul>
 *   <li>{@link sgc.subprocesso.service.workflow.SubprocessoWorkflowService} - Operações genéricas de workflow</li>
 *   <li>{@link sgc.subprocesso.service.workflow.SubprocessoTransicaoService} - Registra transições e executa workflows com análises</li>
 *   <li>{@link sgc.subprocesso.service.workflow.SubprocessoCadastroWorkflowService} - Workflow de cadastro de atividades</li>
 *   <li>{@link sgc.subprocesso.service.workflow.SubprocessoMapaWorkflowService} - Workflow de mapa de competências</li>
 * </ul>
 * 
 * <h3>CRUD Services ({@link sgc.subprocesso.service.crud})</h3>
 * <p><strong>Função:</strong> Operações básicas de persistência</p>
 * <ul>
 *   <li>{@link sgc.subprocesso.service.crud.SubprocessoCrudService} - CRUD básico (criar, atualizar, excluir, buscar)</li>
 *   <li>{@link sgc.subprocesso.service.crud.SubprocessoValidacaoService} - Validações de negócio</li>
 * </ul>
 * 
 * <h3>Detalhe Services ({@link sgc.subprocesso.service.detalhe})</h3>
 * <p><strong>Função:</strong> Consultas detalhadas e agregações</p>
 * <ul>
 *   <li>{@link sgc.subprocesso.service.detalhe.SubprocessoDetalheService} - Montagem de DTOs detalhados</li>
 * </ul>
 * 
 * <h3>Contexto Services ({@link sgc.subprocesso.service.contexto})</h3>
 * <p><strong>Função:</strong> Gerenciamento de contexto operacional</p>
 * <ul>
 *   <li>{@link sgc.subprocesso.service.contexto.SubprocessoContextoService} - Monta contexto de edição (subprocesso + mapa + atividades)</li>
 * </ul>
 * 
 * <h3>Mapa Services ({@link sgc.subprocesso.service.mapa})</h3>
 * <p><strong>Função:</strong> Operações de mapa de competências</p>
 * <ul>
 *   <li>{@link sgc.subprocesso.service.mapa.SubprocessoMapaService} - Operações específicas de mapa dentro de subprocesso</li>
 * </ul>
 * 
 * <h3>Notificacao Services ({@link sgc.subprocesso.service.notificacao})</h3>
 * <p><strong>Função:</strong> Envio de notificações</p>
 * <ul>
 *   <li>{@link sgc.subprocesso.service.notificacao.SubprocessoEmailService} - Envio de emails relacionados a subprocessos</li>
 * </ul>
 * 
 * <h3>Factory ({@link sgc.subprocesso.service.factory})</h3>
 * <p><strong>Função:</strong> Criação de entidades</p>
 * <ul>
 *   <li>{@link sgc.subprocesso.service.factory.SubprocessoFactory} - Factory para criação de subprocessos</li>
 * </ul>
 * 
 * <h3>Listeners ({@link sgc.subprocesso.service.listener})</h3>
 * <p><strong>Função:</strong> Processamento de eventos</p>
 * <ul>
 *   <li>{@link sgc.subprocesso.service.listener.SubprocessoComunicacaoListener} - Listener de eventos de comunicação</li>
 * </ul>
 * 
 * <h2>Fluxo de Uso</h2>
 * 
 * <h3>1. Controller → Facade</h3>
 * <pre>{@code
 * @RestController
 * public class SubprocessoCadastroController {
 *     private final SubprocessoFacade facade;
 *     
 *     @PostMapping("/{id}/cadastro/disponibilizar")
 *     public void disponibilizar(@PathVariable Long id) {
 *         facade.disponibilizarCadastro(id, getCurrentUser());
 *     }
 * }
 * }</pre>
 * 
 * <h3>2. Facade → Services Especializados</h3>
 * <pre>{@code
 * @Service
 * public class SubprocessoFacade {
 *     private final SubprocessoCadastroWorkflowService cadastroWorkflow;
 *     private final SubprocessoMapaWorkflowService mapaWorkflow;
 *     private final SubprocessoCrudService crudService;
 *     private final SubprocessoDetalheService detalheService;
 *     
 *     public void disponibilizarCadastro(Long id, Usuario usuario) {
 *         cadastroWorkflow.disponibilizar(id, usuario);
 *     }
 * }
 * }</pre>
 * 
 * <h3>3. Service → Validação de Acesso → Lógica de Negócio</h3>
 * <pre>{@code
 * @Service
 * class SubprocessoCadastroWorkflowService {
 *     private final AccessControlService accessControl;
 *     private final SubprocessoRepo repo;
 *     
 *     public void disponibilizar(Long id, Usuario usuario) {
 *         Subprocesso sp = repo.findById(id).orElseThrow();
 *         
 *         // Camada de Segurança
 *         accessControl.verificarPermissao(usuario, DISPONIBILIZAR_CADASTRO, sp);
 *         
 *         // Lógica de Negócio
 *         sp.setSituacao(CADASTRO_DISPONIBILIZADO);
 *         repo.save(sp);
 *         
 *         // Registro de Transição
 *         transicaoService.registrar(sp, ...);
 *     }
 * }
 * }</pre>
 * 
 * <h2>Responsabilidades dos Services</h2>
 * 
 * <h3>SubprocessoFacade</h3>
 * <ul>
 *   <li>✅ Orquestrar chamadas a múltiplos services</li>
 *   <li>✅ Expor API pública simplificada</li>
 *   <li>✅ Delegar para services especializados</li>
 *   <li>❌ NÃO deve conter lógica de negócio complexa</li>
 * </ul>
 * 
 * <h3>Workflow Services</h3>
 * <ul>
 *   <li>✅ Gerenciar transições de estado</li>
 *   <li>✅ Validar regras de negócio para transições</li>
 *   <li>✅ Chamar AccessControlService para autorização</li>
 *   <li>✅ Registrar transições e análises</li>
 *   <li>❌ NÃO devem fazer verificações de acesso diretas (usar AccessControlService)</li>
 * </ul>
 * 
 * <h3>CRUD Services</h3>
 * <ul>
 *   <li>✅ Operações básicas de persistência</li>
 *   <li>✅ Validações simples de dados</li>
 *   <li>✅ Conversão entidade ↔ DTO</li>
 *   <li>❌ NÃO devem gerenciar workflows</li>
 * </ul>
 * 
 * <h2>Dependências</h2>
 * <p>Services deste pacote dependem de:</p>
 * <ul>
 *   <li><strong>sgc.seguranca.acesso:</strong> AccessControlService (autorização)</li>
 *   <li><strong>sgc.analise:</strong> AnaliseService (registro de análises)</li>
 *   <li><strong>sgc.mapa:</strong> MapaFacade, CompetenciaService (mapas de competências)</li>
 *   <li><strong>sgc.organizacao:</strong> UsuarioService, UnidadeService (estrutura organizacional)</li>
 *   <li><strong>sgc.notificacao:</strong> NotificacaoEmailService (opcional, via eventos)</li>
 * </ul>
 * 
 * <h2>Comunicação com Outros Módulos</h2>
 * <ul>
 *   <li><strong>Síncrona:</strong> Via injeção de dependência (ex: mapaService.salvar(...))</li>
 *   <li><strong>Assíncrona:</strong> Via Spring Events (ex: EventoTransicaoEfetuada)</li>
 * </ul>
 * 
 * <h2>Métricas Atuais</h2>
 * <ul>
 *   <li><strong>Services totais:</strong> 12 (organizados em 8 sub-pacotes lógicos)</li>
 *   <li><strong>Linhas de código:</strong> ~2.200</li>
 *   <li><strong>Testes:</strong> 200+ testes (cobertura ~95%)</li>
 *   <li><strong>Estrutura:</strong> Modularizada por responsabilidade funcional</li>
 * </ul>
 * 
 * @see sgc.subprocesso.service.SubprocessoFacade
 * @see sgc.subprocesso.service.workflow
 * @see sgc.subprocesso.service.crud
 * @see sgc.subprocesso.service.detalhe
 * @see sgc.subprocesso.service.contexto
 * @see sgc.subprocesso.service.mapa
 * @see sgc.subprocesso.service.notificacao
 * @see sgc.subprocesso.service.factory
 * @see sgc.subprocesso.service.listener
 * @see sgc.seguranca.acesso.AccessControlService
 * @author Sistema SGC
 * @version 2.0
 * @since 1.0
 */
@NullMarked
package sgc.subprocesso.service;

import org.jspecify.annotations.NullMarked;
