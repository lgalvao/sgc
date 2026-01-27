/**
 * Data Transfer Objects (DTOs) do módulo de Processo.
 *
 * <p>Este pacote contém todas as classes DTO usadas para transferir dados relacionados
 * a processos entre as camadas de apresentação e serviço.
 *
 * <h2>Princípios de DTOs</h2>
 * <ul>
 *   <li><strong>NUNCA expor entidades JPA</strong>: DTOs são a interface pública da API</li>
 *   <li><strong>Imutabilidade</strong>: Usar records ou classes com campos final</li>
 *   <li><strong>Validação</strong>: Usar Jakarta Validation (@NotNull, @Valid, etc.)</li>
 *   <li><strong>Nomenclatura</strong>: Sufixo "Dto" obrigatório</li>
 * </ul>
 *
 * <h2>Conversão com Mappers</h2>
 * <p>A conversão entre entidades e DTOs é feita via {@link sgc.processo.mapper mappers MapStruct}:
 * <pre>{@code
 * // Entidade → DTO (via Mapper)
 * ProcessoDto dto = processoMapper.toDto(processoEntity);
 *
 * // DTO → Entidade (via Mapper)
 * Processo entity = processoMapper.toEntity(dto);
 * }</pre>
 *
 * <h2>Tipos de DTOs</h2>
 * <ul>
 *   <li><strong>Input DTOs</strong>: Recebem dados do frontend (ex: CriarProcessoDto)</li>
 *   <li><strong>Output DTOs</strong>: Retornam dados para o frontend (ex: ProcessoDto)</li>
 *   <li><strong>Summary DTOs</strong>: Versões resumidas para listagens (ex: ProcessoResumoDto)</li>
 * </ul>
 *
 * <h2>Exemplo de DTO</h2>
 * <pre>{@code
 * public record CriarProcessoDto(
 *     @NotBlank String titulo,
 *     @NotNull TipoProcesso tipo,
 *     @Valid ParametrosProcessoDto parametros
 * ) {}
 * }</pre>
 *
 * @see sgc.processo.mapper Mappers para conversão Entidade ↔ DTO
 * @see sgc.processo.model Entidades JPA (nunca expostas diretamente)
 * @since 1.0
 */
@NullMarked
package sgc.processo.dto;

import org.jspecify.annotations.NullMarked;
