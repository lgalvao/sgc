/**
 * Módulo de Auditoria e Análise de Processos do SGC.
 * 
 * <p>Responsável por realizar auditorias, revisões e análises
 * de processos e subprocessos do sistema.</p>
 * 
 * <h2>API Pública</h2>
 * <ul>
 *   <li>{@link sgc.analise.AnaliseService} - Facade principal para operações de análise</li>
 *   <li>{@link sgc.analise.api.AnaliseHistoricoDto} - DTO para histórico de análises</li>
 *   <li>{@link sgc.analise.api.AnaliseValidacaoHistoricoDto} - DTO para validação de histórico</li>
 *   <li>{@link sgc.analise.api.CriarAnaliseApiRequest} - DTO para criação de análise via API</li>
 *   <li>{@link sgc.analise.api.CriarAnaliseRequest} - DTO para criação de análise</li>
 * </ul>
 * 
 * <h2>Dependências Permitidas</h2>
 * <ul>
 *   <li>processo - Para acesso a informações de processos</li>
 *   <li>subprocesso - Para acesso a informações de subprocessos</li>
 *   <li>comum - Para componentes compartilhados</li>
 * </ul>
 * 
 * <h2>Eventos Publicados</h2>
 * <p>Nenhum evento é publicado por este módulo no momento.</p>
 * 
 * <h2>Eventos Consumidos</h2>
 * <p>Nenhum evento é consumido por este módulo no momento.</p>
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Auditoria e Análise",
    allowedDependencies = {"processo", "subprocesso", "comum"}
)
package sgc.analise;
