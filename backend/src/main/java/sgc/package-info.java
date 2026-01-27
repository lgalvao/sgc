/**
 * Pacote raiz do Sistema de Gestão de Competências (SGC).
 *
 * <h2>Visão Geral</h2>
 * <p>O SGC é um sistema desenvolvido em Spring Boot 4 + Java 21 que gerencia o mapeamento
 * de competências organizacionais através de processos, subprocessos, atividades e mapas.
 *
 * <h2>Módulos Principais</h2>
 * <ul>
 *   <li>{@link sgc.processo} - Gerenciamento de processos de mapeamento</li>
 *   <li>{@link sgc.subprocesso} - Gerenciamento de subprocessos por unidade organizacional</li>
 *   <li>{@link sgc.mapa} - Mapas de competências, atividades e conhecimentos</li>
 *   <li>{@link sgc.organizacao} - Estrutura organizacional (usuários, unidades, perfis)</li>
 *   <li>{@link sgc.seguranca} - Autenticação, autorização e controle de acesso</li>
 *   <li>{@link sgc.relatorio} - Geração de relatórios (PDF)</li>
 *   <li>{@link sgc.painel} - Dashboard e visualizações</li>
 *   <li>{@link sgc.analise} - Análise de riscos e gaps de competências</li>
 *   <li>{@link sgc.notificacao} - Notificações e emails</li>
 *   <li>{@link sgc.alerta} - Alertas e lembretes</li>
 *   <li>{@link sgc.configuracao} - Configurações do sistema</li>
 *   <li>{@link sgc.comum} - Utilitários, erros e configurações compartilhadas</li>
 * </ul>
 *
 * <h2>Arquitetura</h2>
 *
 * <h3>Camadas</h3>
 * <pre>
 * Controllers (REST API)
 *     ↓
 * Facades (Orquestração)
 *     ↓
 * Services (Lógica de Negócio)
 *     ↓
 * Repositories (Acesso a Dados)
 *     ↓
 * Entidades JPA
 * </pre>
 *
 * <h3>Padrões Arquiteturais</h3>
 * <ul>
 *   <li><strong>Facade Pattern</strong>: Controllers usam apenas Facades para orquestração</li>
 *   <li><strong>Security in 3 Layers</strong>: @PreAuthorize → AccessControlService → Services</li>
 *   <li><strong>DTOs Obrigatórios</strong>: Entidades JPA nunca expostas em APIs</li>
 *   <li><strong>Domain Events</strong>: Comunicação assíncrona via Spring Events</li>
 *   <li><strong>Repository Pattern</strong>: Spring Data JPA</li>
 * </ul>
 *
 * <h2>Convenções de Nomenclatura</h2>
 * <ul>
 *   <li><strong>Controllers</strong>: {Recurso}Controller (ex: ProcessoController)</li>
 *   <li><strong>Facades</strong>: {Recurso}Facade (ex: ProcessoFacade)</li>
 *   <li><strong>Services</strong>: {Recurso}Service (ex: ProcessoService)</li>
 *   <li><strong>Repositories</strong>: {Entidade}Repo (ex: ProcessoRepo)</li>
 *   <li><strong>DTOs</strong>: {Entidade}Dto (ex: ProcessoDto)</li>
 *   <li><strong>Mappers</strong>: {Entidade}Mapper (ex: ProcessoMapper)</li>
 *   <li><strong>Eventos</strong>: Evento{Recurso}{Ação} (ex: EventoProcessoCriado)</li>
 * </ul>
 *
 * <h2>Convenções de Código</h2>
 * <ul>
 *   <li><strong>Idioma</strong>: Inteiramente em Português Brasileiro (código, comentários, mensagens)</li>
 *   <li><strong>IDs</strong>: Usar "codigo" em vez de "id" (convenção do projeto)</li>
 *   <li><strong>Classes</strong>: PascalCase</li>
 *   <li><strong>Métodos</strong>: camelCase</li>
 *   <li><strong>Tabelas</strong>: UPPER_CASE</li>
 *   <li><strong>Colunas</strong>: snake_case</li>
 * </ul>
 *
 * <h2>Segurança</h2>
 * <p>O sistema implementa controle de acesso em 3 camadas:
 * <ol>
 *   <li><strong>HTTP Layer</strong>: @PreAuthorize nos Controllers (roles básicas)</li>
 *   <li><strong>Authorization Layer</strong>: AccessControlService (regras detalhadas)</li>
 *   <li><strong>Business Layer</strong>: Services (sem verificações de acesso)</li>
 * </ol>
 *
 * <p>Ver {@link sgc.seguranca.acesso} para detalhes da arquitetura de segurança.
 *
 * <h2>Testes</h2>
 * <ul>
 *   <li><strong>Unitários</strong>: JUnit 5 + Mockito</li>
 *   <li><strong>Integração</strong>: @SpringBootTest + H2</li>
 *   <li><strong>E2E</strong>: Playwright (frontend)</li>
 * </ul>
 *
 * <h2>Documentação</h2>
 * <ul>
 *   <li>{@code /docs/ARCHITECTURE.md} - Visão geral da arquitetura</li>
 *   <li>{@code /docs/SECURITY-REFACTORING-COMPLETE.md} - Arquitetura de segurança</li>
 *   <li>{@code /AGENTS.md} - Guia para agentes de desenvolvimento</li>
 *   <li>{@code /regras/backend-padroes.md} - Padrões detalhados do backend</li>
 * </ul>
 *
 * @version 2.0
 * @see sgc.processo Módulo de processos
 * @see sgc.subprocesso Módulo de subprocessos
 * @see sgc.mapa Módulo de mapas de competências
 * @see sgc.seguranca.acesso Controle de acesso centralizado
 * @since 1.0
 */
@NullMarked
package sgc;

import org.jspecify.annotations.NullMarked;
