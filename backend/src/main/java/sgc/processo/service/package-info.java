/**
 * Módulo de Processos - Serviços de lógica de negócio.
 * 
 * <h2>Visão Geral</h2>
 * <p>Este pacote contém os serviços que implementam a lógica de negócio do módulo de processos.
 * Processos são orquestradores de alto nível que coordenam múltiplos subprocessos em diferentes
 * unidades organizacionais.</p>
 * 
 * <h2>Arquitetura de Services</h2>
 * 
 * <h3>Facade (Public API)</h3>
 * <ul>
 *   <li>{@link ProcessoFacade} - Ponto de entrada único para todas as operações de processo
 *       <ul>
 *         <li>Orquestra serviços especializados</li>
 *         <li>Expõe API simplificada para controllers</li>
 *         <li>Gerencia ciclo de vida completo de processos</li>
 *       </ul>
 *   </li>
 * </ul>
 * 
 * <h3>Services Especializados</h3>
 * <ul>
 *   <li>{@code ProcessoConsultaService} - Consultas especializadas e relatórios
 *       <ul>
 *         <li>Busca processos com filtros complexos</li>
 *         <li>Estatísticas e dashboards</li>
 *         <li>Queries otimizadas</li>
 *       </ul>
 *   </li>
 *   <li>{@code ProcessoNotificacaoService} - Envio de notificações
 *       <ul>
 *         <li>Lembretes de prazos</li>
 *         <li>Notificações de mudanças de estado</li>
 *         <li>Comunicação com unidades</li>
 *       </ul>
 *   </li>
 *   <li>{@code ProcessoInicializador} - Inicialização de processos
 *       <ul>
 *         <li>Criação de subprocessos para todas as unidades</li>
 *         <li>Cópia de mapas de referência</li>
 *         <li>Configuração inicial de workflow</li>
 *       </ul>
 *   </li>
 * </ul>
 * 
 * <h2>Fluxo de Uso</h2>
 * 
 * <h3>1. Criar Processo</h3>
 * <pre>{@code
 * // Controller
 * @PostMapping
 * public ProcessoDto criar(@RequestBody ProcessoDto dto) {
 *     return facade.criar(dto);
 * }
 * 
 * // Facade
 * public ProcessoDto criar(ProcessoDto dto) {
 *     Processo processo = mapper.toEntity(dto);
 *     Processo salvo = repo.save(processo);
 *     return mapper.toDto(salvo);
 * }
 * }</pre>
 * 
 * <h3>2. Iniciar Processo</h3>
 * <pre>{@code
 * // Controller
 * @PostMapping("/{id}/iniciar")
 * public void iniciar(@PathVariable Long id) {
 *     facade.iniciar(id, getCurrentUser());
 * }
 * 
 * // Facade
 * public void iniciar(Long id, Usuario usuario) {
 *     accessControl.verificarPermissao(usuario, INICIAR_PROCESSO, processo);
 *     inicializador.inicializar(processo);
 *     eventPublisher.publishEvent(new EventoProcessoIniciado(id));
 * }
 * 
 * // Inicializador
 * void inicializar(Processo processo) {
 *     // Cria subprocessos para todas as unidades
 *     List<Unidade> unidades = unidadeService.listarAtivas();
 *     unidades.forEach(unidade -> 
 *         subprocessoService.criar(processo, unidade)
 *     );
 *     
 *     processo.setSituacao(EM_ANDAMENTO);
 *     repo.save(processo);
 * }
 * }</pre>
 * 
 * <h3>3. Finalizar Processo</h3>
 * <pre>{@code
 * facade.finalizar(id, usuario);
 * // Verifica que todos os subprocessos estão homologados
 * // Publica mapas finalizados
 * // Muda situação para FINALIZADO
 * }</pre>
 * 
 * <h2>Tipos de Processo</h2>
 * 
 * <h3>MAPEAMENTO (Inicial)</h3>
 * <ul>
 *   <li>Primeiro mapeamento de competências da organização</li>
 *   <li>Unidades criam mapas do zero</li>
 *   <li>Fluxo: Cadastro → Validação → Homologação</li>
 * </ul>
 * 
 * <h3>REVISAO (Atualização)</h3>
 * <ul>
 *   <li>Atualização de mapas existentes</li>
 *   <li>Unidades revisam mapas vigentes</li>
 *   <li>Detecta impactos de mudanças</li>
 *   <li>Fluxo: Revisão Cadastro → Validação → Homologação</li>
 * </ul>
 * 
 * <h2>Estados do Processo (SituacaoProcesso)</h2>
 * <ul>
 *   <li><strong>NAO_INICIADO:</strong> Processo criado mas não iniciado</li>
 *   <li><strong>EM_ANDAMENTO:</strong> Subprocessos sendo executados</li>
 *   <li><strong>FINALIZADO:</strong> Todos os subprocessos concluídos</li>
 * </ul>
 * 
 * <h2>Responsabilidades do ProcessoFacade</h2>
 * <ul>
 *   <li>✅ CRUD de processos (criar, atualizar, excluir)</li>
 *   <li>✅ Iniciar processo (criar subprocessos para todas as unidades)</li>
 *   <li>✅ Finalizar processo (verificar conclusão de todos os subprocessos)</li>
 *   <li>✅ Enviar lembretes para unidades</li>
 *   <li>✅ Consultar status e estatísticas</li>
 *   <li>✅ Operações em bloco (aceitar/homologar múltiplos subprocessos)</li>
 *   <li>❌ NÃO gerencia workflows de subprocessos (responsabilidade de SubprocessoFacade)</li>
 * </ul>
 * 
 * <h2>Dependências</h2>
 * <p>Services deste pacote dependem de:</p>
 * <ul>
 *   <li><strong>sgc.subprocesso:</strong> SubprocessoFacade (criação e gestão de subprocessos)</li>
 *   <li><strong>sgc.organizacao:</strong> UnidadeService (listar unidades para criar subprocessos)</li>
 *   <li><strong>sgc.seguranca.acesso:</strong> AccessControlService (autorização)</li>
 *   <li><strong>sgc.notificacao:</strong> NotificacaoEmailService (envio de lembretes)</li>
 *   <li><strong>sgc.mapa:</strong> MapaService (cópia de mapas de referência)</li>
 * </ul>
 * 
 * <h2>Eventos de Domínio</h2>
 * <p>Este módulo publica eventos para comunicação assíncrona:</p>
 * <ul>
 *   <li>{@link EventoProcessoIniciado} - Quando processo é iniciado</li>
 *   <li>{@link EventoProcessoFinalizado} - Quando processo é finalizado</li>
 * </ul>
 * 
 * <h2>Segurança</h2>
 * <p>Ações permitidas apenas para <strong>ADMIN</strong>:</p>
 * <ul>
 *   <li>Criar processo</li>
 *   <li>Atualizar processo</li>
 *   <li>Excluir processo</li>
 *   <li>Iniciar processo</li>
 *   <li>Finalizar processo</li>
 *   <li>Enviar lembretes</li>
 * </ul>
 * 
 * <h2>Métricas Atuais</h2>
 * <ul>
 *   <li><strong>Facade:</strong> ProcessoFacade (19.458 bytes)</li>
 *   <li><strong>Services especializados:</strong> 2-3 services</li>
 *   <li><strong>Testes:</strong> 50+ testes (cobertura ~90%)</li>
 *   <li><strong>Visibilidade:</strong> Facade public, services package-private (ideal)</li>
 * </ul>
 * 
 * <h2>Diferenças em Relação a Subprocesso</h2>
 * <table>
 *   <tr>
 *     <th>Aspecto</th>
 *     <th>Processo</th>
 *     <th>Subprocesso</th>
 *   </tr>
 *   <tr>
 *     <td>Escopo</td>
 *     <td>Toda a organização</td>
 *     <td>Uma unidade específica</td>
 *   </tr>
 *   <tr>
 *     <td>Quantidade</td>
 *     <td>1 processo por vez</td>
 *     <td>N subprocessos (um por unidade)</td>
 *   </tr>
 *   <tr>
 *     <td>Responsável</td>
 *     <td>ADMIN (SEDOC)</td>
 *     <td>CHEFE da unidade</td>
 *   </tr>
 *   <tr>
 *     <td>Estados</td>
 *     <td>3 estados simples</td>
 *     <td>15 estados complexos</td>
 *   </tr>
 *   <tr>
 *     <td>Workflow</td>
 *     <td>Simples (criar → iniciar → finalizar)</td>
 *     <td>Complexo (cadastro + validação + homologação)</td>
 *   </tr>
 * </table>
 * 
 * @see ProcessoFacade
 * @see sgc.subprocesso.service.SubprocessoFacade
 * @see sgc.processo.model.Processo
 * @see sgc.processo.model.TipoProcesso
 * @see sgc.processo.model.SituacaoProcesso
 * @author Sistema SGC
 * @version 2.0
 * @since 1.0
 */
@NullMarked
package sgc.processo.service;

import org.jspecify.annotations.NullMarked;
