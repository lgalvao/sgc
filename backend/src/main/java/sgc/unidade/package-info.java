/**
 * Módulo de Estrutura Organizacional (Foundation).
 * 
 * <p>Gerencia unidades organizacionais, sua hierarquia e tipos.
 * Este é um módulo foundation usado por diversos módulos de domínio.</p>
 * 
 * <h2>API Pública</h2>
 * <ul>
 *   <li>{@link sgc.unidade.service.UnidadeService} - Facade para operações de unidades</li>
 *   <li>{@link sgc.unidade.api.AtribuicaoTemporariaDto} - DTO para atribuições temporárias</li>
 *   <li>{@link sgc.unidade.api.CriarAtribuicaoTemporariaReq} - Request para criação de atribuição temporária</li>
 * </ul>
 * 
 * <h2>Dependências Permitidas</h2>
 * <ul>
 *   <li>sgrh - Para informações de usuários e unidades externas</li>
 *   <li>processo - Para vincular unidades a processos</li>
 *   <li>mapa - Para vincular unidades a mapas de competências</li>
 *   <li>comum - Componentes compartilhados</li>
 * </ul>
 * 
 * <h2>Eventos</h2>
 * <p>Este módulo não publica eventos atualmente.</p>
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Estrutura Organizacional",
    allowedDependencies = {"sgrh", "processo", "mapa", "comum"}
)
package sgc.unidade;
