/**
 * Módulo de Gestão de Alertas do SGC.
 * 
 * <p>Responsável por criar, gerenciar e notificar alertas para usuários
 * relacionados a processos, subprocessos e outras entidades do sistema.</p>
 * 
 * <h2>API Pública</h2>
 * <ul>
 *   <li>{@link sgc.alerta.AlertaService} - Facade principal para operações de alertas</li>
 *   <li>{@link sgc.alerta.api.AlertaDto} - DTO para transferência de dados de alertas</li>
 * </ul>
 * 
 * <h2>Dependências Permitidas</h2>
 * <ul>
 *   <li>sgrh - Para obter informações de usuários</li>
 *   <li>comum - Para componentes compartilhados</li>
 * </ul>
 * 
 * <h2>Eventos Publicados</h2>
 * <p>Nenhum evento é publicado por este módulo no momento.</p>
 * 
 * <h2>Eventos Consumidos</h2>
 * <ul>
 *   <li>EventoProcessoIniciado - Cria alertas ao iniciar processo</li>
 *   <li>EventoSubprocessoCriado - Cria alertas para novos subprocessos</li>
 * </ul>
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Gestão de Alertas",
    allowedDependencies = {"sgrh", "comum"}
)
package sgc.alerta;
