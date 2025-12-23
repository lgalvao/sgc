/**
 * Módulo de Mapa de Competências (Core Domain).
 * 
 * <p>Gerencia mapas de competências, competências técnicas, e suas relações
 * com atividades e conhecimentos. Um mapa agrega competências de uma unidade
 * organizacional e é central para os processos de revisão e análise.</p>
 * 
 * <h2>API Pública</h2>
 * <ul>
 *   <li>{@link sgc.mapa.MapaService} - Facade para operações de CRUD de mapas</li>
 *   <li>{@link sgc.mapa.api.MapaDto} - DTO principal para transferência de dados</li>
 *   <li>{@link sgc.mapa.api.MapaCompletoDto} - DTO completo com competências</li>
 * </ul>
 * 
 * <h2>Dependências Permitidas</h2>
 * <ul>
 *   <li>atividade - Vinculação com atividades e conhecimentos</li>
 *   <li>subprocesso - Consulta situação de subprocessos</li>
 *   <li>unidade - Mapas associados a unidades organizacionais</li>
 *   <li>sgrh - Informações de usuários</li>
 *   <li>comum - Componentes compartilhados</li>
 * </ul>
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Mapa de Competências",
    allowedDependencies = {"atividade", "subprocesso", "unidade", "sgrh", "comum"}
)
package sgc.mapa;
