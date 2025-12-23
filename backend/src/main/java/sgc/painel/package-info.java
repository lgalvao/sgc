/**
 * Módulo de Painel de Controle (Supporting).
 * 
 * <p>Fornece dados agregados para o dashboard da aplicação, centralizando
 * a lógica de negócio que coleta informações de diferentes módulos para
 * apresentar uma visão consolidada ao usuário.</p>
 * 
 * <h2>API Pública</h2>
 * <ul>
 *   <li>{@link sgc.painel.PainelService} - Facade para operações do painel</li>
 * </ul>
 * 
 * <h2>Dependências Permitidas</h2>
 * <ul>
 *   <li>processo - Consulta de processos para dashboard</li>
 *   <li>alerta - Consulta de alertas para dashboard</li>
 *   <li>unidade - Informações de unidades organizacionais</li>
 *   <li>sgrh - Informações de perfis e usuários</li>
 *   <li>comum - Componentes compartilhados</li>
 * </ul>
 * 
 * <h2>Características</h2>
 * <p>Módulo read-only que agrega dados de outros módulos para visualização.</p>
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Painel de Controle",
    allowedDependencies = {"processo", "alerta", "unidade", "sgrh", "comum"}
)
package sgc.painel;
