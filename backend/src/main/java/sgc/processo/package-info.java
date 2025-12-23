/**
 * Módulo Orquestrador de Processos do SGC.
 * 
 * <p>Responsável por gerenciar o ciclo de vida completo dos processos,
 * coordenando subprocessos, mapas de competências e atividades.</p>
 * 
 * <h2>API Pública</h2>
 * <ul>
 *   <li>{@link sgc.processo.api.ProcessoDto} - DTO principal de processo</li>
 *   <li>{@link sgc.processo.api.ProcessoDetalheDto} - DTO detalhado de processo</li>
 *   <li>{@link sgc.processo.api.eventos} - Eventos de domínio publicados</li>
 * </ul>
 * 
 * <h2>Dependências Permitidas</h2>
 * <ul>
 *   <li>subprocesso - Gerenciamento de subprocessos</li>
 *   <li>mapa - Mapas de competências</li>
 *   <li>unidade - Estrutura organizacional</li>
 *   <li>sgrh - Integração com sistema de RH</li>
 *   <li>comum - Componentes transversais</li>
 * </ul>
 * 
 * <h2>Eventos Publicados</h2>
 * <ul>
 *   <li>EventoProcessoCriado - Quando processo é criado</li>
 *   <li>EventoProcessoIniciado - Quando processo é iniciado</li>
 *   <li>EventoProcessoFinalizado - Quando processo é finalizado</li>
 *   <li>Eventos de Subprocesso - Diversos eventos relacionados ao workflow de subprocessos</li>
 * </ul>
 * 
 * <h2>Eventos Consumidos</h2>
 * <ul>
 *   <li>Nenhum evento é consumido diretamente por este módulo</li>
 * </ul>
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Orquestrador de Processos",
    allowedDependencies = {"subprocesso", "mapa", "unidade", "sgrh", "comum"}
)
package sgc.processo;
