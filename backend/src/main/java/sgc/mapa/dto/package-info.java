/**
 * Objetos de Transferência de Dados (DTOs) do módulo de Mapa de Competências.
 *
 * <h2>Visão Geral</h2>
 * <p>
 * Este pacote contém DTOs para visualização e manipulação de Mapas de Competências.
 * Os mapas representam a consolidação final de atividades e conhecimentos após
 * homologação do cadastro, exibindo competências atribuídas aos servidores.
 * </p>
 *
 * <h2>DTOs Principais</h2>
 *
 * <h3>{@link sgc.mapa.dto.MapaDto}</h3>
 * <p>
 * Dados básicos de um mapa de competências.
 * </p>
 * <ul>
 *   <li>Código, nome, unidade, data de criação</li>
 *   <li>Situação (CRIADO, HOMOLOGADO, AJUSTADO)</li>
 *   <li>Usado em listagens e referências</li>
 * </ul>
 *
 * <h3>{@link sgc.mapa.dto.MapaCompletoDto}</h3>
 * <p>
 * Visão completa do mapa incluindo atividades e competências.
 * </p>
 * <ul>
 *   <li>Lista de {@link sgc.mapa.dto.AtividadeResponse} com competências</li>
 *   <li>Lista de {@link sgc.mapa.dto.CompetenciaMapaDto} consolidadas</li>
 *   <li>Usado em visualizações detalhadas e relatórios</li>
 * </ul>
 *
 * <h3>{@link sgc.mapa.dto.ImpactoMapaDto}</h3>
 * <p>
 * Análise de impactos de revisões no mapa existente (CDU-12).
 * </p>
 * <ul>
 *   <li>{@link sgc.mapa.dto.AtividadeImpactadaDto} - Atividades novas/removidas/alteradas</li>
 *   <li>{@link sgc.mapa.dto.CompetenciaImpactadaDto} - Competências afetadas</li>
 *   <li>Estatísticas de impacto (quantidades, percentuais)</li>
 * </ul>
 *
 * <h2>DTOs de Atividades</h2>
 *
 * <h3>{@link sgc.mapa.dto.AtividadeResponse}</h3>
 * <p>
 * Representa uma atividade no contexto do mapa.
 * </p>
 * <ul>
 *   <li>Descrição, código</li>
 *   <li>Conhecimentos associados ({@link sgc.mapa.dto.ConhecimentoResponse})</li>
 * </ul>
 *
 * <h3>{@link sgc.mapa.dto.AtividadeImpactadaDto}</h3>
 * <p>
 * Atividade com indicação de impacto em revisões.
 * </p>
 * <ul>
 *   <li>Tipo de impacto: NOVA, REMOVIDA, ALTERADA, INALTERADA</li>
 *   <li>Comparação: estado anterior vs. estado novo</li>
 *   <li>Usada em análises de CDU-12</li>
 * </ul>
 *
 * <h2>DTOs de Competências e Conhecimentos</h2>
 *
 * <h3>{@link sgc.mapa.dto.CompetenciaMapaDto}</h3>
 * <p>
 * Competência atribuída no mapa.
 * </p>
 * <ul>
 *   <li>Descrição, nível (BÁSICO, INTERMEDIÁRIO, AVANÇADO)</li>
 *   <li>Essencialidade (ESSENCIAL, DESEJÁVEL)</li>
 *   <li>Pode estar associada a atividades ou competências gerais da unidade</li>
 * </ul>
 *
 * <h3>{@link sgc.mapa.dto.CompetenciaImpactadaDto}</h3>
 * <p>
 * Competência com indicação de impacto.
 * </p>
 * <ul>
 *   <li>Tipo de impacto: NOVA, REMOVIDA, NÍVEL_ALTERADO, INALTERADA</li>
 *   <li>Nível anterior vs. nível novo</li>
 * </ul>
 *
 * <h3>{@link sgc.mapa.dto.ConhecimentoResponse}</h3>
 * <p>
 * Conhecimento associado a uma atividade.
 * </p>
 * <ul>
 *   <li>Descrição do conhecimento</li>
 * </ul>
 *
 * <h2>DTOs de Operação</h2>
 *
 * <h3>{@link sgc.mapa.dto.SalvarMapaRequest}</h3>
 * <p>
 * Requisição para salvar ajustes no mapa (CDU-16 - ADMIN).
 * </p>
 * <ul>
 *   <li>Código do subprocesso</li>
 *   <li>Lista de atividades ajustadas</li>
 *   <li>Lista de competências ajustadas</li>
 *   <li>Observações do ajuste</li>
 * </ul>
 *
 * <h3>{@link sgc.mapa.dto.ResultadoOperacaoConhecimento}</h3>
 * <p>
 * Resposta de operações em conhecimentos.
 * </p>
 * <ul>
 *   <li>Sucesso/falha</li>
 *   <li>Mensagem descritiva</li>
 *   <li>Dados do conhecimento criado/atualizado</li>
 * </ul>
 *
 * <h2>DTOs de Visualização</h2>
 * <p>
 * O subpacote {@link sgc.mapa.dto.visualizacao} contém DTOs especializados
 * para diferentes contextos de visualização:
 * </p>
 * <ul>
 *   <li>Visualização para CHEFE (edição de atividades)</li>
 *   <li>Visualização para GESTOR (análise de validação)</li>
 *   <li>Visualização para ADMIN (ajustes finais)</li>
 *   <li>Visualização de comparação (antes/depois)</li>
 * </ul>
 *
 * <h2>Casos de Uso</h2>
 *
 * <h3>CDU-10: Validar Mapa (CHEFE)</h3>
 * <pre>{@code
 * // Frontend solicita mapa para validação
 * MapaCompletoDto mapa = mapaService.buscarMapaCompleto(codSubprocesso);
 * 
 * // CHEFE valida e pode apresentar sugestões
 * ApresentarSugestoesRequest sugestoes = ...;
 * mapaService.apresentarSugestoes(codSubprocesso, sugestoes);
 * }</pre>
 *
 * <h3>CDU-12: Verificar Impactos (CHEFE/GESTOR/ADMIN)</h3>
 * <pre>{@code
 * // Comparar mapa atual vs. mapa de revisão
 * ImpactoMapaDto impactos = mapaService.analisarImpactos(codSubprocesso);
 * 
 * // impactos contém:
 * // - atividadesNovas, atividadesRemovidas, atividadesAlteradas
 * // - competenciasNovas, competenciasRemovidas, competenciasAlteradas
 * // - estatísticas de impacto
 * }</pre>
 *
 * <h3>CDU-16: Ajustar Mapa (ADMIN)</h3>
 * <pre>{@code
 * // ADMIN pode ajustar mapa homologado
 * SalvarMapaRequest ajustes = new SalvarMapaRequest();
 * ajustes.setAtividades(...);
 * ajustes.setCompetencias(...);
 * 
 * mapaService.salvarAjustes(codSubprocesso, ajustes);
 * }</pre>
 *
 * <h2>Mapeamento de Entidades</h2>
 * <p>
 * Os DTOs são mapeados de/para entidades JPA usando mappers em {@link sgc.mapa.mapper}:
 * </p>
 * <ul>
 *   <li>MapaMapper - Conversões básicas</li>
 *   <li>MapaCompletoMapper - Mapa completo com atividades/competências</li>
 *   <li>ImpactoMapaMapper - Análise de impactos</li>
 * </ul>
 *
 * <h2>Segurança e Permissões</h2>
 * <p>
 * DTOs de mapa respeitam permissões de acesso:
 * </p>
 * <ul>
 *   <li><b>CHEFE</b>: Visualiza mapa da própria unidade, pode validar/sugerir</li>
 *   <li><b>GESTOR</b>: Visualiza mapas de unidades subordinadas, pode aceitar/devolver</li>
 *   <li><b>ADMIN</b>: Visualiza todos os mapas, pode ajustar após homologação</li>
 * </ul>
 *
 * <h2>Performance</h2>
 * <p>
 * Mapas completos podem ser grandes (muitas atividades/competências):
 * </p>
 * <ul>
 *   <li>Usar {@code @EntityGraph} para evitar N+1 queries</li>
 *   <li>Considerar paginação para listagens grandes</li>
 *   <li>Cache de mapas homologados (imutáveis)</li>
 * </ul>
 *
 * <h2>Princípios de Design</h2>
 * <ul>
 *   <li><b>Imutabilidade</b>: DTOs de resposta são imutáveis (records)</li>
 *   <li><b>Separação</b>: DTOs de visualização diferentes para diferentes perfis</li>
 *   <li><b>Validação</b>: Bean Validation em DTOs de request</li>
 *   <li><b>Null-safety</b>: Campos nullable explicitamente marcados</li>
 * </ul>
 *
 * <h2>Referências</h2>
 * <ul>
 *   <li>{@link sgc.mapa.mapper} - Mappers de mapa</li>
 *   <li>{@link sgc.mapa.service} - Services de mapa</li>
 *   <li>{@link sgc.mapa.model} - Entidades de mapa</li>
 *   <li>{@link sgc.subprocesso.dto} - DTOs de subprocesso (contexto relacionado)</li>
 *   <li>ADR-004: DTO Pattern - Padrão de DTOs obrigatórios</li>
 *   <li>/reqs/cdu-10.md - Validar mapa</li>
 *   <li>/reqs/cdu-12.md - Verificar impactos</li>
 *   <li>/reqs/cdu-16.md - Ajustar mapa</li>
 * </ul>
 *
 * @since 1.0
 * @see sgc.mapa.mapper
 * @see sgc.mapa.service
 * @see sgc.mapa.dto.visualizacao
 */
@NullMarked
package sgc.mapa.dto;

import org.jspecify.annotations.NullMarked;
