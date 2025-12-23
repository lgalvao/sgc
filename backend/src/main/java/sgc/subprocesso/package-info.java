/**
 * Módulo de Gestão de Subprocessos do SGC.
 * 
 * <p>Responsável por gerenciar o workflow completo dos subprocessos,
 * incluindo cadastro, validação, mapa de competências e revisão.</p>
 * 
 * <h2>API Pública</h2>
 * <ul>
 *   <li>{@link sgc.subprocesso.api.SubprocessoDto} - DTO principal de subprocesso</li>
 *   <li>{@link sgc.subprocesso.api.SubprocessoDetalheDto} - DTO detalhado de subprocesso</li>
 * </ul>
 * 
 * <h2>Dependências Permitidas</h2>
 * <ul>
 *   <li>processo::api.eventos - Apenas eventos do módulo processo</li>
 *   <li>atividade - Gestão de atividades e conhecimentos</li>
 *   <li>mapa - Mapas de competências</li>
 *   <li>unidade - Estrutura organizacional</li>
 *   <li>sgrh - Integração com sistema de RH</li>
 *   <li>alerta - Gestão de alertas</li>
 *   <li>analise - Auditoria e análise</li>
 *   <li>notificacao - Sistema de notificações</li>
 *   <li>comum - Componentes transversais</li>
 * </ul>
 * 
 * <h2>Eventos Publicados</h2>
 * <ul>
 *   <li>Nenhum - Os eventos são publicados pelo módulo processo</li>
 * </ul>
 * 
 * <h2>Eventos Consumidos</h2>
 * <ul>
 *   <li>Eventos de Processo - Consumidos via listeners internos</li>
 * </ul>
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Gestão de Subprocessos",
    allowedDependencies = {"processo", "atividade", "mapa", "unidade", "sgrh", "alerta", "analise", "notificacao", "comum"}
)
package sgc.subprocesso;
