/**
 * Mappers de conversão entre entidades JPA e DTOs do módulo de Mapa de Competências.
 *
 * <h2>Visão Geral</h2>
 * <p>
 * Este pacote contém interfaces e classes de mapeamento implementadas com MapStruct que convertem
 * entidades JPA ({@link sgc.mapa.model.Mapa}, {@link sgc.mapa.model.Atividade},
 * {@link sgc.mapa.model.Competencia}, {@link sgc.mapa.model.Conhecimento}) em DTOs e vice-versa.
 * Os mappers garantem que a camada de API nunca exponha entidades JPA diretamente.
 * </p>
 *
 * <h2>Mappers Disponíveis</h2>
 *
 * <h3>{@link sgc.mapa.mapper.MapaMapper}</h3>
 * <p>
 * Mapper simples para conversões básicas de Mapa.
 * </p>
 * <ul>
 *   <li>{@code toDto(Mapa)} - Converte entidade para {@link sgc.mapa.dto.MapaDto}</li>
 *   <li>{@code toEntity(MapaDto)} - Converte DTO para entidade</li>
 *   <li>Uso: Operações CRUD básicas, listagens</li>
 * </ul>
 *
 * <h3>{@link sgc.mapa.mapper.MapaCompletoMapper}</h3>
 * <p>
 * Mapper especializado para construir {@link sgc.mapa.dto.MapaCompletoDto} com dados completos
 * de competências e atividades associadas.
 * </p>
 * <ul>
 *   <li>{@code toDto(Mapa, Long, List<Competencia>)} - Monta DTO completo com competências</li>
 *   <li>{@code toDto(Competencia)} - Converte competência para {@link sgc.mapa.dto.CompetenciaMapaDto}</li>
 *   <li>{@code mapAtividadesCodigos(Set<Atividade>)} - Mapeia atividades para códigos</li>
 *   <li>Uso: CDU-10 (Validar Mapa), CDU-12 (Verificar Impactos)</li>
 * </ul>
 *
 * <h3>{@link sgc.mapa.mapper.AtividadeMapper}</h3>
 * <p>
 * Mapper para atividades de um mapa de competências.
 * </p>
 * <ul>
 *   <li>{@code toDto(Atividade)} - Converte para {@link sgc.mapa.dto.AtividadeDto}</li>
 *   <li>{@code toEntity(AtividadeDto)} - Converte DTO para entidade</li>
 *   <li>Mapeamento: {@code mapa.codigo → mapaCodigo} (relacionamento com mapa)</li>
 *   <li>Ignora: {@code mapa}, {@code conhecimentos}, {@code competencias} (ao converter de DTO)</li>
 *   <li>Uso: Gestão de atividades em CDU-10, CDU-16</li>
 * </ul>
 *
 * <h3>{@link sgc.mapa.mapper.ConhecimentoMapper}</h3>
 * <p>
 * Mapper para conhecimentos associados a atividades.
 * </p>
 * <ul>
 *   <li>{@code toDto(Conhecimento)} - Converte para {@link sgc.mapa.dto.ConhecimentoDto}</li>
 *   <li>{@code toEntity(ConhecimentoDto)} - Converte DTO para entidade</li>
 *   <li>Mapeamento: {@code atividade.codigo → atividadeCodigo} (relacionamento FK)</li>
 *   <li>Resolução de FK: {@code map(Long)} - Resolve código de atividade usando {@link sgc.mapa.model.AtividadeRepo}</li>
 *   <li>Uso: Gestão de conhecimentos em mapas de competências</li>
 * </ul>
 *
 * <h2>Padrões de Implementação</h2>
 *
 * <h3>1. Uso de MapStruct</h3>
 * <p>
 * Mappers são interfaces ou classes abstratas anotadas com {@code @Mapper(componentModel = "spring")}.
 * MapStruct gera implementações em tempo de compilação.
 * </p>
 * <pre>{@code
 * @Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
 * public interface MapaMapper {
 *     MapaDto toDto(Mapa mapa);
 *     Mapa toEntity(MapaDto mapaDto);
 * }
 * }</pre>
 *
 * <h3>2. Mapeamentos de Relacionamentos</h3>
 * <p>
 * Relacionamentos JPA são mapeados para códigos (Long) nos DTOs.
 * </p>
 * <pre>{@code
 * @Mapper(componentModel = "spring")
 * public abstract class AtividadeMapper {
 *     
 *     // Entidade → DTO: mapa.codigo vira mapaCodigo
 *     @Mapping(source = "mapa.codigo", target = "mapaCodigo")
 *     public abstract AtividadeDto toDto(Atividade atividade);
 *     
 *     // DTO → Entidade: mapaCodigo é ignorado (relacionamento gerenciado no service)
 *     @Mapping(target = "mapa", ignore = true)
 *     @Mapping(target = "conhecimentos", ignore = true)
 *     @Mapping(target = "competencias", ignore = true)
 *     public abstract Atividade toEntity(AtividadeDto atividadeDto);
 * }
 * }</pre>
 *
 * <h3>3. Resolução de Foreign Keys</h3>
 * <p>
 * Quando é necessário resolver FKs, use método {@code map(Long)} com injeção de repository.
 * </p>
 * <pre>{@code
 * @Component
 * @Mapper(componentModel = "spring")
 * public abstract class ConhecimentoMapper {
 *     
 *     @Autowired
 *     protected AtividadeRepo atividadeRepo;
 *     
 *     @Mapping(source = "atividadeCodigo", target = "atividade")
 *     public abstract Conhecimento toEntity(ConhecimentoDto dto);
 *     
 *     // Método de resolução de FK
 *     public Atividade map(Long codigo) {
 *         return codigo != null
 *             ? atividadeRepo.findById(codigo)
 *                 .orElseThrow(() -> ErroEntidadeDeveriaExistir.fkViolada(
 *                     "Atividade", codigo, "ConhecimentoMapper"))
 *             : null;
 *     }
 * }
 * }</pre>
 *
 * <h3>4. Mappers Compostos</h3>
 * <p>
 * Para DTOs complexos, use métodos {@code default} com lógica customizada.
 * </p>
 * <pre>{@code
 * @Mapper(componentModel = "spring")
 * public interface MapaCompletoMapper {
 *     
 *     // Método default para lógica complexa
 *     default MapaCompletoDto toDto(Mapa mapa, Long codSubprocesso, 
 *                                    List<Competencia> competencias) {
 *         return MapaCompletoDto.builder()
 *             .codigo(mapa == null ? null : mapa.getCodigo())
 *             .subprocessoCodigo(codSubprocesso)
 *             .observacoes(mapa == null ? null : mapa.getObservacoesDisponibilizacao())
 *             .competencias(competencias == null ? null : 
 *                 competencias.stream().map(this::toDto).toList())
 *             .build();
 *     }
 *     
 *     @Mapping(target = "atividadesCodigos", source = "atividades", 
 *              qualifiedByName = "mapAtividadesCodigos")
 *     CompetenciaMapaDto toDto(Competencia competencia);
 *     
 *     @Named("mapAtividadesCodigos")
 *     default List<Long> mapAtividadesCodigos(Set<Atividade> atividades) {
 *         if (atividades == null) return null;
 *         return atividades.stream()
 *             .filter(Objects::nonNull)
 *             .map(Atividade::getCodigo)
 *             .filter(Objects::nonNull)
 *             .collect(Collectors.toList());
 *     }
 * }
 * }</pre>
 *
 * <h3>5. Injeção de Dependências</h3>
 * <p>
 * Para mappers que precisam de repositories ou services, use {@code @Autowired} em classes abstratas.
 * Adicione {@code @Component} além de {@code @Mapper}.
 * </p>
 * <pre>{@code
 * @Component  // Necessário para autowiring
 * @Mapper(componentModel = "spring")
 * public abstract class ConhecimentoMapper {
 *     
 *     @Autowired  // Injeta repository
 *     protected AtividadeRepo atividadeRepo;
 *     
 *     // ... métodos de mapeamento
 * }
 * }</pre>
 *
 * <h2>Casos de Uso</h2>
 *
 * <h3>CDU-10: Validar Mapa</h3>
 * <ul>
 *   <li>{@code MapaCompletoMapper} - Monta mapa completo para validação</li>
 *   <li>{@code AtividadeMapper} - Converte atividades para visualização</li>
 *   <li>{@code ConhecimentoMapper} - Converte conhecimentos associados</li>
 * </ul>
 *
 * <h3>CDU-12: Verificar Impactos</h3>
 * <ul>
 *   <li>{@code MapaCompletoMapper} - Carrega estado atual do mapa</li>
 *   <li>{@code CompetenciaMapaDto} - Compara competências antes/depois</li>
 * </ul>
 *
 * <h3>CDU-16: Ajustar Mapa</h3>
 * <ul>
 *   <li>{@code MapaMapper} - Conversões básicas de mapa</li>
 *   <li>{@code AtividadeMapper} - Atualiza/cria atividades ajustadas</li>
 *   <li>{@code ConhecimentoMapper} - Atualiza/cria conhecimentos</li>
 * </ul>
 *
 * <h2>Boas Práticas</h2>
 *
 * <h3>1. Null-Safety</h3>
 * <ul>
 *   <li>Sempre verifique {@code null} antes de mapear coleções</li>
 *   <li>Use {@code filter(Objects::nonNull)} em streams</li>
 *   <li>Operadores ternários para valores opcionais</li>
 * </ul>
 *
 * <h3>2. Performance</h3>
 * <ul>
 *   <li>Evite carregar relacionamentos desnecessários (use {@code ignore = true})</li>
 *   <li>Para listas grandes, considere {@code Stream} em vez de loops</li>
 *   <li>Use {@code unmappedTargetPolicy = ReportingPolicy.IGNORE} para DTOs parciais</li>
 * </ul>
 *
 * <h3>3. Validação de FKs</h3>
 * <ul>
 *   <li>Sempre valide FKs ao resolver relacionamentos</li>
 *   <li>Use {@link sgc.comum.erros.ErroEntidadeDeveriaExistir#fkViolada} para erros de FK</li>
 *   <li>Inclua nome do mapper no erro para debugging</li>
 * </ul>
 *
 * <h3>4. Testes</h3>
 * <ul>
 *   <li>Teste mapeamentos bidirecionais (toDto + toEntity)</li>
 *   <li>Teste com valores {@code null}</li>
 *   <li>Teste resolução de FKs (casos válidos e inválidos)</li>
 *   <li>Teste mapeamentos de coleções vazias</li>
 * </ul>
 *
 * <h2>Relação com Outros Pacotes</h2>
 *
 * <ul>
 *   <li>{@link sgc.mapa.dto} - DTOs mapeados por este pacote</li>
 *   <li>{@link sgc.mapa.model} - Entidades JPA mapeadas</li>
 *   <li>{@link sgc.mapa.service} - Services que usam estes mappers</li>
 *   <li>{@link sgc.comum.erros} - Erros lançados durante mapeamento</li>
 * </ul>
 *
 * @see sgc.mapa.dto
 * @see sgc.mapa.model
 * @see sgc.mapa.service.MapaFacade
 * @see <a href="https://mapstruct.org/">MapStruct Documentation</a>
 */
@org.jspecify.annotations.NullMarked
package sgc.mapa.mapper;
