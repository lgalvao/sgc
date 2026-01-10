/**
 * Mappers MapStruct para conversão entre entidades e DTOs do módulo de Processo.
 * 
 * <p>Este pacote contém interfaces MapStruct que realizam a conversão bidirecional
 * entre entidades JPA ({@link sgc.processo.model}) e DTOs ({@link sgc.processo.dto}).
 * 
 * <h2>Por que MapStruct?</h2>
 * <ul>
 *   <li><strong>Type-safe</strong>: Erros de mapeamento detectados em tempo de compilação</li>
 *   <li><strong>Performance</strong>: Código gerado = zero reflexão em runtime</li>
 *   <li><strong>Manutenível</strong>: Mappings declarativos, fáceis de entender</li>
 *   <li><strong>Testável</strong>: Mappers são injetáveis e testáveis</li>
 * </ul>
 * 
 * <h2>Padrão de Mappers</h2>
 * <pre>{@code
 * @Mapper(componentModel = "spring", uses = {...})
 * public interface ProcessoMapper {
 *     
 *     // Entidade → DTO
 *     ProcessoDto toDto(Processo entity);
 *     
 *     // DTO → Entidade
 *     @Mapping(target = "codigo", ignore = true)  // Gerado pelo BD
 *     @Mapping(target = "dataCriacao", ignore = true)  // Auditoria
 *     Processo toEntity(CriarProcessoDto dto);
 *     
 *     // Lista
 *     List<ProcessoDto> toDtoList(List<Processo> entities);
 * }
 * }</pre>
 * 
 * <h2>Convenções</h2>
 * <ul>
 *   <li><strong>Nome</strong>: {Entidade}Mapper (ex: ProcessoMapper)</li>
 *   <li><strong>@Mapper</strong>: Sempre com componentModel = "spring"</li>
 *   <li><strong>Métodos</strong>: toDto(), toEntity(), toDtoList()</li>
 *   <li><strong>@Mapping</strong>: Ignorar campos de auditoria, IDs gerados, etc.</li>
 * </ul>
 * 
 * <h2>Como Usar</h2>
 * <pre>{@code
 * @Service
 * public class ProcessoService {
 *     private final ProcessoMapper mapper;
 *     private final ProcessoRepo repo;
 *     
 *     public ProcessoDto obterProcesso(Long codigo) {
 *         Processo entity = repo.findById(codigo).orElseThrow();
 *         return mapper.toDto(entity);  // Conversão automática
 *     }
 * }
 * }</pre>
 * 
 * <h2>Mappers Aninhados</h2>
 * <p>Quando um DTO contém outros DTOs, use o parâmetro {@code uses}:
 * <pre>{@code
 * @Mapper(componentModel = "spring", uses = {UnidadeMapper.class})
 * public interface ProcessoMapper {
 *     // Automaticamente converte Unidade → UnidadeDto usando UnidadeMapper
 *     ProcessoDto toDto(Processo entity);
 * }
 * }</pre>
 * 
 * @see sgc.processo.dto DTOs do módulo
 * @see sgc.processo.model Entidades JPA
 * @since 1.0
 */
@NullMarked
package sgc.processo.mapper;

import org.jspecify.annotations.NullMarked;
