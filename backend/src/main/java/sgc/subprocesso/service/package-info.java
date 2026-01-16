/**
 * Módulo de Subprocessos - Serviços de lógica de negócio.
 * 
 * <h2>Visão Geral</h2>
 * <p>Este pacote contém os serviços que implementam a lógica de negócio do módulo de subprocessos.
 * Segue o <strong>padrão Facade</strong> onde controllers interagem APENAS com {@link sgc.subprocesso.service.SubprocessoFacade},
 * que por sua vez orquestra os services especializados.</p>
 * 
 * <h2>Arquitetura de Services (Fase 4 - Reorganização em Sub-pacotes)</h2>
 * 
 * <h3>Facade (Public API)</h3>
 * <ul>
 *   <li>{@link sgc.subprocesso.service.SubprocessoFacade} - Ponto de entrada único para todas as operações de subprocesso</li>
 * </ul>
 * 
 * <h3>Sub-pacotes por Responsabilidade</h3>
 * 
 * <h4>📦 workflow/ - Services de Workflow e Transições</h4>
 * <p>Localizado em {@link sgc.subprocesso.service.workflow}</p>
 * <ul>
 *   <li>{@code SubprocessoCadastroWorkflowService} - Workflow de cadastro de atividades</li>
 *   <li>{@code SubprocessoMapaWorkflowService} - Workflow de mapa de competências</li>
 *   <li>{@code SubprocessoTransicaoService} - Registro de transições e execução de workflows</li>
 * </ul>
 * 
 * <h4>📦 crud/ - Services de CRUD e Validação</h4>
 * <p>Localizado em {@link sgc.subprocesso.service.crud}</p>
 * <ul>
 *   <li>{@code SubprocessoCrudService} - Operações CRUD básicas</li>
 *   <li>{@code SubprocessoValidacaoService} - Validações de regras de negócio</li>
 * </ul>
 * 
 * <h4>📦 notificacao/ - Services de Comunicação</h4>
 * <p>Localizado em {@link sgc.subprocesso.service.notificacao}</p>
 * <ul>
 *   <li>{@code SubprocessoEmailService} - Envio de emails</li>
 *   <li>{@code SubprocessoComunicacaoListener} - Listener assíncrono de eventos (Fase 3)</li>
 * </ul>
 * 
 * <h4>📦 factory/ - Factory de Criação</h4>
 * <p>Localizado em {@link sgc.subprocesso.service.factory}</p>
 * <ul>
 *   <li>{@code SubprocessoFactory} - Criação e inicialização de subprocessos (usado por ProcessoInicializador)</li>
 * </ul>
 * 
 * <h4>📁 service/ (raiz) - Services de Suporte</h4>
 * <ul>
 *   <li>{@code SubprocessoContextoService} - Montagem de contexto de edição</li>
 *   <li>{@code SubprocessoMapaService} - Operações específicas de mapa</li>
 *   <li>{@code SubprocessoDetalheService} - Montagem de DTOs detalhados</li>
 *   <li>{@code SubprocessoWorkflowService} - Operações genéricas de workflow</li>
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
 *   <li><strong>sgc.analise:</strong> AnaliseFacade (registro de análises)</li>
 *   <li><strong>sgc.mapa:</strong> MapaFacade, CompetenciaService (mapas de competências)</li>
 *   <li><strong>sgc.organizacao:</strong> UsuarioFacade, UnidadeService (estrutura organizacional)</li>
 *   <li><strong>sgc.notificacao:</strong> NotificacaoEmailService (opcional, via eventos)</li>
 * </ul>
 * 
 * <h2>Comunicação com Outros Módulos</h2>
 * <ul>
 *   <li><strong>Síncrona:</strong> Via injeção de dependência (ex: mapaService.salvar(...))</li>
 *   <li><strong>Assíncrona:</strong> Via Spring Events assíncronos (Fase 3, ex: EventoTransicaoSubprocesso)</li>
 * </ul>
 * 
 * <h2>Histórico de Reorganização Arquitetural</h2>
 * <ul>
 *   <li><strong>Fase 1:</strong> Análise e documentação da estrutura atual</li>
 *   <li><strong>Fase 2:</strong> Encapsulamento via ArchUnit (detectar violações de acesso)</li>
 *   <li><strong>Fase 3:</strong> Listeners assíncronos (desacoplamento completo entre módulos)</li>
 *   <li><strong>Fase 4 (ATUAL):</strong> Reorganização em sub-pacotes temáticos
 *       <ul>
 *         <li>✅ Criados sub-pacotes: workflow/, crud/, notificacao/, factory/</li>
 *         <li>✅ Services movidos para sub-pacotes apropriados</li>
 *         <li>✅ Diretório decomposed/ unificado com service/</li>
 *         <li>✅ Imports atualizados em todo o codebase</li>
 *         <li>✅ Testes reorganizados (281 testes passando)</li>
 *       </ul>
 *   </li>
 * </ul>
 * 
 * <h2>Próximas Fases</h2>
 * <ul>
 *   <li><strong>Fase 5:</strong> Consolidar services (13 → 6-7)
 *       <ul>
 *         <li>🎯 Unificar workflows em SubprocessoWorkflowService único</li>
 *         <li>🎯 Mover lógica de SubprocessoDetalheService para Facade</li>
 *         <li>🎯 Resolver violações ArchUnit detectadas na Fase 2</li>
 *       </ul>
 *   </li>
 *   <li><strong>Fase 6:</strong> Documentação final
 *       <ul>
 *         <li>🎯 Atualizar ARCHITECTURE.md</li>
 *         <li>🎯 Criar guias de desenvolvimento</li>
 *       </ul>
 *   </li>
 * </ul>
 * 
 * <h2>Métricas Atuais (Pós-Fase 4)</h2>
 * <ul>
 *   <li><strong>Services totais:</strong> 13 (inalterado, apenas reorganizados)</li>
 *   <li><strong>Sub-pacotes criados:</strong> 4 (workflow, crud, notificacao, factory)</li>
 *   <li><strong>Linhas de código:</strong> ~2.500</li>
 *   <li><strong>Testes:</strong> 281 testes (100% passando)</li>
 *   <li><strong>Diretório decomposed/:</strong> Removido (unificado com service/)</li>
 * </ul>
 * 
 * @see sgc.subprocesso.service.SubprocessoFacade
 * @see sgc.subprocesso.service.workflow
 * @see sgc.subprocesso.service.crud
 * @see sgc.subprocesso.service.notificacao
 * @see sgc.subprocesso.service.factory
 * @see sgc.seguranca.acesso.AccessControlService
 * @author Sistema SGC
 * @version 2.0 (Fase 4 - Reorganização em Sub-pacotes)
 * @since 1.0
 */
@NullMarked
package sgc.subprocesso.service;

import org.jspecify.annotations.NullMarked;
