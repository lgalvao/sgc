/**
 * Módulo de Subprocessos - Serviços de lógica de negócio.
 * 
 * <h2>Visão Geral</h2>
 * <p>Este pacote contém os serviços que implementam a lógica de negócio do módulo de subprocessos.
 * Segue o <strong>padrão Facade</strong> onde controllers interagem APENAS com {@link SubprocessoFacade},
 * que por sua vez orquestra os services especializados.</p>
 * 
 * <h2>Arquitetura de Services</h2>
 * 
 * <h3>Facade (Public API)</h3>
 * <ul>
 *   <li>{@link SubprocessoFacade} - Ponto de entrada único para todas as operações de subprocesso</li>
 * </ul>
 * 
 * <h3>Workflow Services (Especializado)</h3>
 * <p><strong>Função:</strong> Gerenciar transições de estado (workflows)</p>
 * <ul>
 *   <li>{@code SubprocessoCadastroWorkflowService} - Workflow de cadastro de atividades
 *       <ul>
 *         <li>Disponibilizar cadastro/revisão</li>
 *         <li>Devolver, aceitar, homologar cadastro</li>
 *         <li>Operações em bloco</li>
 *       </ul>
 *   </li>
 *   <li>{@code SubprocessoMapaWorkflowService} - Workflow de mapa de competências
 *       <ul>
 *         <li>Editar mapa (adicionar/remover competências)</li>
 *         <li>Disponibilizar mapa para validação</li>
 *         <li>Apresentar sugestões, validar</li>
 *         <li>Devolver, aceitar, homologar validação</li>
 *         <li>Ajustar mapa pós-homologação</li>
 *       </ul>
 *   </li>
 *   <li>{@code SubprocessoTransicaoService} - Registra transições de estado no histórico</li>
 *   <li>{@code SubprocessoWorkflowExecutor} - Executor comum de workflows (análise + transição)</li>
 * </ul>
 * 
 * <h3>CRUD Services</h3>
 * <p><strong>Função:</strong> Operações básicas de persistência</p>
 * <ul>
 *   <li>{@code SubprocessoService} - CRUD básico (criar, atualizar, excluir, buscar)</li>
 * </ul>
 * 
 * <h3>Support Services</h3>
 * <p><strong>Função:</strong> Serviços auxiliares especializados</p>
 * <ul>
 *   <li>{@code SubprocessoContextoService} - Monta contexto de edição (subprocesso + mapa + atividades)</li>
 *   <li>{@code SubprocessoPermissaoCalculator} - Calcula permissões do usuário para um subprocesso</li>
 *   <li>{@code SubprocessoEmailService} - Envio de emails relacionados a subprocessos</li>
 *   <li>{@code SubprocessoMapaService} - Operações específicas de mapa dentro de subprocesso</li>
 *   <li>{@code SubprocessoFactory} - Factory para criação de subprocessos</li>
 * </ul>
 * 
 * <h3>Services Decompostos (Subpacote)</h3>
 * <p>Localizado em {@link sgc.subprocesso.service.decomposed}</p>
 * <ul>
 *   <li>{@code SubprocessoCrudService} - Operações CRUD especializadas</li>
 *   <li>{@code SubprocessoDetalheService} - Montagem de DTOs detalhados</li>
 *   <li>{@code SubprocessoValidacaoService} - Validações de negócio</li>
 *   <li>{@code SubprocessoWorkflowService} - Operações de workflow (decomposed)</li>
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
 *     private final SubprocessoService subprocessoService;
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
 *   <li><strong>sgc.mapa:</strong> MapaService, CompetenciaService (mapas de competências)</li>
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
 * <h2>Oportunidades de Melhoria</h2>
 * <ul>
 *   <li>🎯 Consolidar SubprocessoCadastroWorkflowService + SubprocessoMapaWorkflowService → único WorkflowService</li>
 *   <li>🎯 Mover lógica de SubprocessoContextoService para SubprocessoFacade</li>
 *   <li>🎯 Tornar todos os services (exceto Facade) package-private</li>
 *   <li>🎯 Reduzir de 12 → ~6 services (50% redução)</li>
 * </ul>
 * 
 * <h2>Métricas Atuais</h2>
 * <ul>
 *   <li><strong>Services totais:</strong> 12</li>
 *   <li><strong>Linhas de código:</strong> ~2.200</li>
 *   <li><strong>Testes:</strong> 200+ testes (cobertura ~95%)</li>
 *   <li><strong>Visibilidade:</strong> Todos public (deveria ser package-private exceto Facade)</li>
 * </ul>
 * 
 * @see SubprocessoFacade
 * @see sgc.subprocesso.service.decomposed
 * @see sgc.seguranca.acesso.AccessControlService
 * @author Sistema SGC
 * @version 2.0
 * @since 1.0
 */
@NullMarked
package sgc.subprocesso.service;

import org.jspecify.annotations.NullMarked;
