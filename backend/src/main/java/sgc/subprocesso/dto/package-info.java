/**
 * Objetos de Transferência de Dados (DTOs) do módulo de Subprocesso.
 *
 * <h2>Visão Geral</h2>
 * <p>
 * Este pacote contém todos os DTOs utilizados para comunicação entre o frontend e backend
 * no contexto de subprocessos. Os DTOs garantem que entidades JPA nunca sejam expostas
 * diretamente nas APIs REST, seguindo o padrão arquitetural obrigatório do SGC.
 * </p>
 *
 * <h2>Categorias de DTOs</h2>
 *
 * <h3>1. DTOs de Consulta (Response)</h3>
 * <ul>
 *   <li>{@link sgc.subprocesso.dto.SubprocessoDto} - Dados básicos de subprocesso</li>
 *   <li>{@link sgc.subprocesso.dto.SubprocessoDetalheDto} - Detalhes completos com permissões</li>
 *   <li>{@link sgc.subprocesso.dto.SubprocessoPermissoesDto} - Permissões calculadas</li>
 *   <li>{@link sgc.subprocesso.dto.SubprocessoSituacaoDto} - Estado atual e histórico</li>
 *   <li>{@link sgc.subprocesso.dto.ContextoEdicaoDto} - Contexto para edição de atividades</li>
 * </ul>
 *
 * <h3>2. DTOs de Comando (Request)</h3>
 * <ul>
 *   <li>{@link sgc.subprocesso.dto.AlterarDataLimiteRequest} - Alteração de data limite (ADMIN)</li>
 *   <li>{@link sgc.subprocesso.dto.ReabrirProcessoRequest} - Reabertura de cadastro/revisão (ADMIN)</li>
 *   <li>{@link sgc.subprocesso.dto.ProcessarEmBlocoRequest} - Operações em bloco</li>
 * </ul>
 *
 * <h3>3. DTOs de Workflow - Cadastro</h3>
 * <ul>
 *   <li>{@link sgc.subprocesso.dto.AceitarCadastroRequest} - Aceitar cadastro (GESTOR/ADMIN)</li>
 *   <li>{@link sgc.subprocesso.dto.DevolverCadastroRequest} - Devolver cadastro (GESTOR/ADMIN)</li>
 *   <li>{@link sgc.subprocesso.dto.HomologarCadastroRequest} - Homologar cadastro (ADMIN)</li>
 *   <li>{@link sgc.subprocesso.dto.SubprocessoCadastroDto} - Dados de cadastro</li>
 *   <li>{@link sgc.subprocesso.dto.ValidacaoCadastroDto} - Validações de cadastro</li>
 * </ul>
 *
 * <h3>4. DTOs de Workflow - Mapa</h3>
 * <ul>
 *   <li>{@link sgc.subprocesso.dto.DisponibilizarMapaRequest} - Disponibilizar mapa (ADMIN)</li>
 *   <li>{@link sgc.subprocesso.dto.ApresentarSugestoesRequest} - Apresentar sugestões (CHEFE)</li>
 *   <li>{@link sgc.subprocesso.dto.DevolverValidacaoRequest} - Devolver validação (GESTOR/ADMIN)</li>
 *   <li>{@link sgc.subprocesso.dto.AnaliseValidacaoDto} - Análise de validação</li>
 *   <li>{@link sgc.subprocesso.dto.SugestoesDto} - Sugestões do CHEFE</li>
 * </ul>
 *
 * <h3>5. DTOs de Ajuste de Mapa</h3>
 * <ul>
 *   <li>{@link sgc.subprocesso.dto.MapaAjusteDto} - Ajustes no mapa</li>
 *   <li>{@link sgc.subprocesso.dto.SalvarAjustesRequest} - Salvar ajustes (ADMIN)</li>
 *   <li>{@link sgc.subprocesso.dto.SubmeterMapaAjustadoRequest} - Submeter mapa ajustado (ADMIN)</li>
 *   <li>{@link sgc.subprocesso.dto.AtividadeAjusteDto} - Ajuste de atividade</li>
 *   <li>{@link sgc.subprocesso.dto.CompetenciaAjusteDto} - Ajuste de competência</li>
 *   <li>{@link sgc.subprocesso.dto.ConhecimentoAjusteDto} - Ajuste de conhecimento</li>
 * </ul>
 *
 * <h3>6. DTOs de Atividades e Competências</h3>
 * <ul>
 *   <li>{@link sgc.subprocesso.dto.AtividadeVisualizacaoDto} - Visualização de atividade</li>
 *   <li>{@link sgc.subprocesso.dto.AtividadeOperacaoResp} - Resposta de operação em atividade</li>
 *   <li>{@link sgc.subprocesso.dto.ConhecimentoVisualizacaoDto} - Visualização de conhecimento</li>
 *   <li>{@link sgc.subprocesso.dto.CompetenciaRequest} - Requisição de competência</li>
 *   <li>{@link sgc.subprocesso.dto.ImportarAtividadesRequest} - Importar atividades</li>
 * </ul>
 *
 * <h3>7. DTOs Auxiliares</h3>
 * <ul>
 *   <li>{@link sgc.subprocesso.dto.MovimentacaoDto} - Movimentação de subprocesso</li>
 *   <li>{@link sgc.subprocesso.dto.RespostaDto} - Resposta genérica</li>
 *   <li>{@link sgc.subprocesso.dto.ErroValidacaoDto} - Erro de validação</li>
 * </ul>
 *
 * <h2>Princípios de Design</h2>
 *
 * <h3>1. Separação de Responsabilidades</h3>
 * <ul>
 *   <li><b>DTOs de Request</b>: Dados que vêm do cliente (validados com Bean Validation)</li>
 *   <li><b>DTOs de Response</b>: Dados enviados ao cliente (sem validações)</li>
 *   <li><b>DTOs Bidirecionais</b>: Evitados sempre que possível</li>
 * </ul>
 *
 * <h3>2. Imutabilidade Preferencial</h3>
 * <ul>
 *   <li>DTOs de Response devem ser imutáveis (usar records quando apropriado)</li>
 *   <li>DTOs de Request podem ter setters para facilitar deserialização JSON</li>
 * </ul>
 *
 * <h3>3. Validação de Entrada</h3>
 * <ul>
 *   <li>Usar {@code @NotNull}, {@code @NotBlank}, {@code @Size} em DTOs de Request</li>
 *   <li>Validações customizadas devem ser documentadas</li>
 *   <li>Validações de negócio ficam nos Services, não nos DTOs</li>
 * </ul>
 *
 * <h3>4. Nomenclatura Consistente</h3>
 * <ul>
 *   <li><b>Req</b>: DTOs de Request (entrada de operações de comando)</li>
 *   <li><b>Dto</b>: DTOs de Response ou DTOs genéricos</li>
 *   <li><b>Request</b>: DTOs de Request mais complexos (forma extensa)</li>
 * </ul>
 *
 * <h2>Mapeamento de DTOs</h2>
 * <p>
 * Os DTOs são mapeados de/para entidades JPA usando {@link sgc.subprocesso.mapper.SubprocessoMapper}
 * e {@link sgc.subprocesso.mapper.SubprocessoDetalheMapper}, implementados com MapStruct.
 * </p>
 *
 * <h2>Padrões de Uso</h2>
 *
 * <h3>Em Controllers</h3>
 * <pre>{@code
 * @PostMapping("/{id}/cadastro/aceitar")
 * public ResponseEntity<RespostaDto> aceitarCadastro(
 *     @PathVariable Long id,
 *     @RequestBody @Valid AceitarCadastroRequest request,
 *     Authentication auth
 * ) {
 *     // Controller recebe DTO, delega para Facade
 *     RespostaDto resposta = subprocessoFacade.aceitarCadastro(id, request, auth);
 *     return ResponseEntity.ok(resposta);
 * }
 * }</pre>
 *
 * <h3>Em Facades/Services</h3>
 * <pre>{@code
 * public SubprocessoDetalheDto buscarDetalhes(Long codigo, Usuario usuario) {
 *     Subprocesso subprocesso = subprocessoRepo.findById(codigo).orElseThrow(...);
 *     
 *     // Verifica permissões
 *     accessControlService.verificarPermissao(usuario, VISUALIZAR_SUBPROCESSO, subprocesso);
 *     
 *     // Monta DTO com mapper
 *     return subprocessoDetalheMapper.toDto(subprocesso, usuario);
 * }
 * }</pre>
 *
 * <h2>Segurança</h2>
 * <ul>
 *   <li>DTOs <b>NUNCA</b> devem expor dados sensíveis (senhas, tokens)</li>
 *   <li>Permissões são calculadas dinamicamente no backend, nunca confiadas no cliente</li>
 *   <li>DTOs de Response devem incluir apenas dados que o usuário tem permissão para ver</li>
 * </ul>
 *
 * <h2>Referências</h2>
 * <ul>
 *   <li>{@link sgc.subprocesso.mapper} - Mappers de DTOs</li>
 *   <li>{@link sgc.subprocesso.service.SubprocessoFacade} - Facade principal</li>
 *   <li>{@link sgc.processo.dto} - DTOs de processo (padrão similar)</li>
 *   <li>ADR-004: DTO Pattern - Arquitetura de DTOs obrigatórios</li>
 *   <li>/regras/backend-padroes.md - Convenções de DTOs</li>
 * </ul>
 *
 * @since 1.0
 * @see sgc.subprocesso.mapper
 * @see sgc.subprocesso.service.SubprocessoFacade
 * @see sgc.processo.dto
 */
@NullMarked
package sgc.subprocesso.dto;

import org.jspecify.annotations.NullMarked;
