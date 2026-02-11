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
 *   <li>{@link sgc.processo.service.ProcessoFacade} - Ponto de entrada único para todas as operações de processo
 *       <ul>
 *         <li>Orquestra serviços especializados</li>
 *         <li>Expõe API simplificada para controllers</li>
 *         <li>Gerencia ciclo de vida completo de processos</li>
 *         <li>✅ REFATORADO: Reduzido de 530 para 340 linhas (-36%)</li>
 *       </ul>
 *   </li>
 * </ul>
 *
 * <h3>Services Especializados</h3>
 * <ul>
 *   <li>{@code ProcessoConsultaService} - Consultas especializadas e relatórios
 *       <ul>
 *         <li>Busca processos com filtros complexos</li>
 *         <li>Listagens de subprocessos elegíveis</li>
 *         <li>Queries otimizadas</li>
 *         <li>✅ EXPANDIDO: Incluiu listarUnidadesBloqueadasPorTipo e listarSubprocessosElegiveis</li>
 *       </ul>
 *   </li>
 *   <li>{@code ProcessoValidador} - Validações de regras de negócio
 *       <ul>
 *         <li>Validação de unidades sem mapa</li>
 *         <li>Validação de finalização de processo</li>
 *         <li>Validação de homologação de subprocessos</li>
 *         <li>✅ NOVO: Criado na refatoração P4</li>
 *       </ul>
 *   </li>
 *   <li>{@code ProcessoAcessoService} - Controle de acesso a processos
 *       <ul>
 *         <li>Verificação hierárquica de acesso</li>
 *         <li>Busca de unidades descendentes</li>
 *         <li>Checagem de permissões baseada em perfil</li>
 *         <li>✅ NOVO: Criado na refatoração P4</li>
 *       </ul>
 *   </li>
 *   <li>{@code ProcessoFinalizador} - Finalização de processos
 *       <ul>
 *         <li>Coordena validações de finalização</li>
 *         <li>Torna mapas vigentes</li>
 *         <li>Publica eventos de finalização</li>
 *         <li>✅ NOVO: Criado na refatoração P4</li>
 *       </ul>
 *   </li>
 *   <li>{@code ProcessoInicializador} - Inicialização de processos
 *       <ul>
 *         <li>Criação de subprocessos para todas as unidades</li>
 *         <li>Cópia de mapas de referência</li>
 *         <li>Configuração inicial de workflow</li>
 *         <li>✅ JÁ EXISTIA: Criado em refatoração anterior</li>
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
 *     // Valida unidades
 *     processoValidador.getMensagemErroUnidadesSemMapa(...);
 *
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
 *     processoInicializador.inicializar(processo);
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
 * // Delega para ProcessoFinalizador que:
 * // - Valida que todos os subprocessos estão homologados (via ProcessoValidador)
 * // - Publica mapas finalizados
 * // - Muda situação para FINALIZADO
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
 *   <li>✅ Iniciar processo (delega para ProcessoInicializador)</li>
 *   <li>✅ Finalizar processo (delega para ProcessoFinalizador)</li>
 *   <li>✅ Enviar lembretes para unidades</li>
 *   <li>✅ Consultar status (delega para ProcessoConsultaService)</li>
 *   <li>✅ Verificar acesso (delega para ProcessoAcessoService)</li>
 *   <li>❌ NÃO gerencia workflows de subprocessos (responsabilidade de SubprocessoFacade)</li>
 * </ul>
 *
 * <h2>Dependências</h2>
 * <p>Services deste pacote dependem de:</p>
 * <ul>
 *   <li><strong>sgc.subprocesso:</strong> SubprocessoFacade (criação e gestão de subprocessos)</li>
 *   <li><strong>sgc.organizacao:</strong> UnidadeFacade (listar unidades para criar subprocessos)</li>
 *   <li><strong>sgc.seguranca.acesso:</strong> AccessControlService (autorização)</li>
 *   <li><strong>sgc.notificacao:</strong> NotificacaoEmailService (envio de lembretes)</li>
 *   <li><strong>sgc.mapa:</strong> MapaService (cópia de mapas de referência)</li>
 * </ul>
 *
 * <h2>Eventos de Domínio</h2>
 * <p>Este módulo publica eventos para comunicação assíncrona:</p>
 * <ul>
 *   <li>{@link sgc.processo.eventos.EventoProcessoIniciado} - Quando processo é iniciado</li>
 *   <li>{@link sgc.processo.eventos.EventoProcessoFinalizado} - Quando processo é finalizado</li>
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
 * <h2>Métricas Atuais (Pós-Refatoração P4)</h2>
 * <ul>
 *   <li><strong>Facade:</strong> ProcessoFacade (340 linhas, antes: 530)</li>
 *   <li><strong>Services especializados:</strong> 5 services (+3 novos)</li>
 *   <li><strong>Redução:</strong> -190 linhas de código (-36%)</li>
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
 *     <td>ADMIN</td>
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
 * @author Sistema SGC
 * @version 2.1 - Refatoração P4 (2026-01-11)
 * @see sgc.processo.service.ProcessoFacade
 * @see sgc.subprocesso.service.SubprocessoFacade
 * @see sgc.processo.model.Processo
 * @see sgc.processo.model.TipoProcesso
 * @see sgc.processo.model.SituacaoProcesso
 * @since 1.0
 */
@NullMarked
package sgc.processo.service;

import org.jspecify.annotations.NullMarked;
