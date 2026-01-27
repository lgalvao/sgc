/**
 * Mappers de conversão entre entidades JPA e DTOs do módulo de Subprocesso.
 *
 * <h2>Visão Geral</h2>
 * <p>
 * Este pacote contém interfaces de mapeamento implementadas com MapStruct que convertem
 * entidades JPA ({@link sgc.subprocesso.model.Subprocesso}) em DTOs e vice-versa.
 * Os mappers garantem que a camada de API nunca exponha entidades JPA diretamente.
 * </p>
 *
 * <h2>Mappers Disponíveis</h2>
 *
 * <h3>{@link sgc.subprocesso.mapper.SubprocessoMapper}</h3>
 * <p>
 * Mapper principal para conversões básicas de Subprocesso.
 * </p>
 * <ul>
 *   <li>{@code toDto(Subprocesso)} - Converte entidade para {@link sgc.subprocesso.dto.SubprocessoDto}</li>
 *   <li>{@code toEntity(SubprocessoDto)} - Converte DTO para entidade (usado raramente)</li>
 *   <li>{@code updateEntity(SubprocessoDto, Subprocesso)} - Atualiza entidade existente</li>
 * </ul>
 *
 * <h3>{@link sgc.subprocesso.mapper.SubprocessoDetalheMapper}</h3>
 * <p>
 * Mapper especializado para construir {@link sgc.subprocesso.dto.SubprocessoDetalheDto}
 * com permissões, validações e contexto de edição.
 * </p>
 * <ul>
 *   <li>{@code toDto(Subprocesso, Usuario)} - Monta DTO completo com permissões calculadas</li>
 *   <li>Integra com {@link sgc.seguranca.acesso.AccessControlService} para permissões</li>
 *   <li>Calcula flags de edição baseadas em estado e perfil do usuário</li>
 * </ul>
 *
 * <h3>{@link sgc.subprocesso.mapper.MapaAjusteMapper}</h3>
 * <p>
 * Mapper para ajustes de mapa (CDU-16).
 * </p>
 * <ul>
 *   <li>{@code toDto(Subprocesso)} - Monta {@link sgc.subprocesso.dto.MapaAjusteDto}</li>
 *   <li>{@code toEntity(MapaAjusteDto)} - Aplica ajustes à entidade</li>
 *   <li>Suporta ajustes em atividades, competências e conhecimentos</li>
 * </ul>
 *
 * <h3>{@link sgc.subprocesso.mapper.MovimentacaoMapper}</h3>
 * <p>
 * Mapper para histórico de movimentações (transições de estado).
 * </p>
 * <ul>
 *   <li>{@code toDto(Movimentacao)} - Converte {@link sgc.subprocesso.model.Movimentacao}</li>
 *   <li>Inclui dados do usuário, situação anterior/nova, timestamp</li>
 * </ul>
 *
 * <h2>Padrões de Implementação</h2>
 *
 * <h3>1. Uso de MapStruct</h3>
 * <p>
 * Todos os mappers são interfaces anotadas com {@code @Mapper(componentModel = "spring")}.
 * MapStruct gera implementações em tempo de compilação.
 * </p>
 * <pre>{@code
 * @Mapper(componentModel = "spring")
 * public interface SubprocessoMapper {
 *     SubprocessoDto toDto(Subprocesso entity);
 *
 *     @Mapping(target = "codigo", ignore = true)
 *     @Mapping(target = "versao", ignore = true)
 *     Subprocesso toEntity(SubprocessoDto dto);
 * }
 * }</pre>
 *
 * <h3>2. Mapeamentos Customizados</h3>
 * <p>
 * Quando a conversão não é direta, use {@code @Mapping} ou métodos {@code default}.
 * </p>
 * <pre>{@code
 * @Mapper(componentModel = "spring")
 * public interface SubprocessoDetalheMapper {
 *
 *     @Mapping(target = "permissoes", source = ".", qualifiedByName = "calcularPermissoes")
 *     SubprocessoDetalheDto toDto(Subprocesso subprocesso, @Context Usuario usuario);
 *
 *     @Named("calcularPermissoes")
 *     default SubprocessoPermissoesDto calcularPermissoes(Subprocesso sp, @Context Usuario usuario) {
 *         // Lógica customizada
 *     }
 * }
 * }</pre>
 *
 * <h3>3. Injeção de Dependências</h3>
 * <p>
 * Mappers podem injetar services para cálculos complexos usando {@code @Mapper(uses = {...})}.
 * </p>
 * <pre>{@code
 * @Mapper(componentModel = "spring", uses = {AccessControlService.class})
 * public interface SubprocessoDetalheMapper {
 *     // AccessControlService será injetado automaticamente
 * }
 * }</pre>
 *
 * <h3>4. Contexto de Mapeamento</h3>
 * <p>
 * Use {@code @Context} para passar informações adicionais (como o usuário autenticado).
 * </p>
 * <pre>{@code
 * SubprocessoDetalheDto toDto(Subprocesso subprocesso, @Context Usuario usuario);
 * }</pre>
 *
 * <h2>Fluxo de Uso</h2>
 *
 * <h3>Em Services/Facades</h3>
 * <pre>{@code
 * @Service
 * public class SubprocessoFacade {
 *
 *     private final SubprocessoRepo repo;
 *     private final SubprocessoMapper mapper;
 *     private final SubprocessoDetalheMapper detalheMapper;
 *
 *     public SubprocessoDto buscar(Long codigo) {
 *         Subprocesso entity = repo.findById(codigo).orElseThrow(...);
 *         return mapper.toDto(entity);  // Conversão simples
 *     }
 *
 *     public SubprocessoDetalheDto buscarDetalhes(Long codigo, Usuario usuario) {
 *         Subprocesso entity = repo.findById(codigo).orElseThrow(...);
 *         return detalheMapper.toDto(entity, usuario);  // Com contexto
 *     }
 * }
 * }</pre>
 *
 * <h2>Boas Práticas</h2>
 *
 * <h3>1. Nunca Expor Entidades JPA</h3>
 * <ul>
 *   <li>Controllers <b>SEMPRE</b> retornam DTOs, nunca entidades</li>
 *   <li>Evita lazy loading issues, vazamento de dados internos, acoplamento</li>
 * </ul>
 *
 * <h3>2. Mapeamento Unidirecional Preferencial</h3>
 * <ul>
 *   <li>Preferir {@code Entity → DTO} (leitura)</li>
 *   <li>Para escrita, usar DTOs de Request específicos + lógica no Service</li>
 *   <li>Evitar {@code DTO → Entity} genérico (perde validações)</li>
 * </ul>
 *
 * <h3>3. Performance</h3>
 * <ul>
 *   <li>MapStruct gera código eficiente em tempo de compilação (sem reflection)</li>
 *   <li>Evitar N+1 queries: usar {@code @EntityGraph} ou DTOs projetados</li>
 *   <li>Para listagens grandes, considerar projeções JPA diretas</li>
 * </ul>
 *
 * <h3>4. Null-Safety</h3>
 * <ul>
 *   <li>Marcar pacote com {@code @NullMarked}</li>
 *   <li>MapStruct respeita anotações de null-safety</li>
 *   <li>Gerar código defensivo quando necessário</li>
 * </ul>
 *
 * <h2>Configuração do MapStruct</h2>
 * <p>
 * Configuração no {@code build.gradle.kts}:
 * </p>
 * <pre>{@code
 * dependencies {
 *     implementation("org.mapstruct:mapstruct:1.5.5.Final")
 *     annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
 * }
 * }</pre>
 *
 * <h2>Testes</h2>
 * <p>
 * Mappers devem ser testados para garantir mapeamentos corretos:
 * </p>
 * <pre>{@code
 * @SpringBootTest
 * class SubprocessoMapperTest {
 *
 *     @Autowired
 *     private SubprocessoMapper mapper;
 *
 *     @Test
 *     void deveMapearSubprocessoParaDto() {
 *         Subprocesso entity = criarSubprocesso();
 *         SubprocessoDto dto = mapper.toDto(entity);
 *
 *         assertThat(dto.codigo()).isEqualTo(entity.getCodigo());
 *         assertThat(dto.descricao()).isEqualTo(entity.getDescricao());
 *     }
 * }
 * }</pre>
 *
 * <h2>Referências</h2>
 * <ul>
 *   <li>{@link sgc.subprocesso.dto} - DTOs de subprocesso</li>
 *   <li>{@link sgc.subprocesso.model} - Entidades JPA</li>
 *   <li>{@link sgc.processo.mapper} - Mappers de processo (padrão similar)</li>
 *   <li>ADR-004: DTO Pattern - Padrão de DTOs obrigatórios</li>
 *   <li>MapStruct Documentation: https://mapstruct.org</li>
 *   <li>/regras/backend-padroes.md - Convenções de mapeamento</li>
 * </ul>
 *
 * @see sgc.subprocesso.dto
 * @see sgc.subprocesso.model
 * @see sgc.processo.mapper
 * @since 1.0
 */
@NullMarked
package sgc.subprocesso.mapper;

import org.jspecify.annotations.NullMarked;
