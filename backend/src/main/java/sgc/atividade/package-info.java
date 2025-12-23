/**
 * Módulo de Gestão de Atividades e Conhecimentos.
 * 
 * <p>Responsável por gerenciar atividades, conhecimentos associados
 * e suas relações com mapas de competências.</p>
 * 
 * <h2>API Pública</h2>
 * <ul>
 *   <li>{@link sgc.atividade.AtividadeService} - Facade de atividades</li>
 *   <li>{@link sgc.atividade.api.AtividadeDto} - DTO de atividade</li>
 *   <li>{@link sgc.atividade.api.ConhecimentoDto} - DTO de conhecimento</li>
 * </ul>
 * 
 * <h2>Dependências Permitidas</h2>
 * <ul>
 *   <li>comum - Componentes compartilhados</li>
 * </ul>
 * 
 * <h2>Eventos Publicados</h2>
 * <p>Este módulo atualmente não publica eventos, mas pode ser estendido no futuro
 * para notificar mudanças em atividades e conhecimentos.</p>
 * 
 * <h2>Eventos Consumidos</h2>
 * <p>Este módulo atualmente não consome eventos.</p>
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Gestão de Atividades",
    allowedDependencies = {"comum"}
)
package sgc.atividade;
