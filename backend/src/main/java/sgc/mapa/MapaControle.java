package sgc.mapa;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sgc.mapa.dto.MapaDto;
import sgc.mapa.dto.MapaMapper;

import java.net.URI;
import java.util.List;

/**
 * Controlador REST para gerenciar Mapas usando DTOs.
 * Evita expor entidades JPA diretamente nas APIs.
 */
@RestController
@RequestMapping("/api/mapas")
@RequiredArgsConstructor
@Tag(name = "Mapas", description = "Endpoints para gerenciamento de mapas de competências")
public class MapaControle {
    private static final PolicyFactory HTML_SANITIZER_POLICY = new HtmlPolicyBuilder()
            .toFactory();

    private final MapaService mapaService;
    private final MapaCrudService mapaCrudService;
    private final MapaMapper mapaMapper;

    private MapaDto sanitizarEMapearMapaDto(MapaDto mapaDto) {
        var sanitizedObservacoesDisponibilizacao = HTML_SANITIZER_POLICY.sanitize(mapaDto.getObservacoesDisponibilizacao());
        var sanitizedSugestoes = HTML_SANITIZER_POLICY.sanitize(mapaDto.getSugestoes());

        return MapaDto.builder()
                .codigo(mapaDto.getCodigo())
                .dataHoraDisponibilizado(mapaDto.getDataHoraDisponibilizado())
                .observacoesDisponibilizacao(sanitizedObservacoesDisponibilizacao)
                .sugestoesApresentadas(mapaDto.getSugestoesApresentadas())
                .dataHoraHomologado(mapaDto.getDataHoraHomologado())
                .sugestoes(sanitizedSugestoes)
                .build();
    }

    /**
     * Retorna uma lista com todos os mapas de competências.
     *
     * @return Uma {@link List} de {@link MapaDto}.
     */
    @GetMapping
    @Operation(summary = "Lista todos os mapas")
    public List<MapaDto> listar() {
        return mapaCrudService.listar()
                .stream()
                .map(mapaMapper::toDTO)
                .toList();
    }

    /**
     * Busca e retorna um mapa de competências específico pelo seu ID.
     *
     * @param id O ID do mapa a ser buscado.
     * @return Um {@link ResponseEntity} contendo o {@link MapaDto} correspondente.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtém um mapa pelo ID")
    public ResponseEntity<MapaDto> obterPorId(@PathVariable Long id) {
        var mapa = mapaCrudService.obterPorId(id);
        return ResponseEntity.ok(mapaMapper.toDTO(mapa));
    }

    /**
     * Cria um novo mapa de competências.
     * <p>
     * Os campos de texto do DTO são sanitizados para remover HTML antes da persistência.
     *
     * @param mapaDto O DTO com os dados do mapa a ser criado.
     * @return Um {@link ResponseEntity} com status 201 Created, o URI do novo mapa
     *         e o {@link MapaDto} criado no corpo da resposta.
     */
    @PostMapping
    @Operation(summary = "Cria um novo mapa")
    public ResponseEntity<MapaDto> criar(@Valid @RequestBody MapaDto mapaDto) {
        var sanitizedMapaDto = sanitizarEMapearMapaDto(mapaDto);

        var entidade = mapaMapper.toEntity(sanitizedMapaDto);
        var salvo = mapaCrudService.criar(entidade);
        URI uri = URI.create("/api/mapas/%d".formatted(salvo.getCodigo()));
        return ResponseEntity.created(uri).body(mapaMapper.toDTO(salvo));
    }

    /**
     * Atualiza um mapa de competências existente.
     * <p>
     * Os campos de texto do DTO são sanitizados para remover HTML antes da atualização.
     *
     * @param id      O ID do mapa a ser atualizado.
     * @param mapaDto O DTO com os novos dados do mapa.
     * @return Um {@link ResponseEntity} com status 200 OK e o {@link MapaDto} atualizado.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um mapa existente")
    public ResponseEntity<MapaDto> atualizar(@PathVariable Long id, @Valid @RequestBody MapaDto mapaDto) {
        var sanitizedMapaDto = sanitizarEMapearMapaDto(mapaDto);

        var entidade = mapaMapper.toEntity(sanitizedMapaDto);
        var atualizado = mapaCrudService.atualizar(id, entidade);
        return ResponseEntity.ok(mapaMapper.toDTO(atualizado));
    }

    /**
     * Exclui um mapa de competências.
     *
     * @param id O ID do mapa a ser excluído.
     * @return Um {@link ResponseEntity} com status 204 No Content.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui um mapa")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        mapaCrudService.excluir(id);
        return ResponseEntity.noContent().build();
    }

}