package sgc.mapa;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sgc.mapa.dto.MapaDto;
import sgc.mapa.dto.MapaMapper;
import sgc.mapa.service.MapaService;

import java.net.URI;
import java.util.List;

/**
 * Controlador REST para gerenciar Mapas usando DTOs. Evita expor entidades JPA diretamente nas
 * APIs.
 */
@RestController
@RequestMapping("/api/mapas")
@RequiredArgsConstructor
@Tag(name = "Mapas", description = "Endpoints para gerenciamento de mapas de competências")
public class MapaController {
    private final MapaService mapaService;
    private final MapaMapper mapaMapper;

    /**
     * Retorna uma lista com todos os mapas de competências.
     *
     * @return Uma {@link List} de {@link MapaDto}.
     */
    @GetMapping
    @Operation(summary = "Lista todos os mapas")
    public List<MapaDto> listar() {
        return mapaService.listar().stream().map(mapaMapper::toDto).toList();
    }

    /**
     * Busca e retorna um mapa de competências específico pelo seu código.
     *
     * @param codigo O código do mapa a ser buscado.
     * @return Um {@link ResponseEntity} contendo o {@link MapaDto} correspondente.
     */
    @GetMapping("/{codigo}")
    @Operation(summary = "Obtém um mapa pelo código")
    public ResponseEntity<MapaDto> obterPorId(@PathVariable Long codigo) {
        var mapa = mapaService.obterPorCodigo(codigo);
        return ResponseEntity.ok(mapaMapper.toDto(mapa));
    }

    /**
     * Cria um novo mapa de competências.
     *
     * @param mapaDto O DTO com os dados do mapa a ser criado.
     * @return Um {@link ResponseEntity} com status 201 Created, o URI do novo mapa e o {@link
     * MapaDto} criado no corpo da resposta.
     */
    @PostMapping
    @Operation(summary = "Cria um novo mapa")
    public ResponseEntity<MapaDto> criar(@Valid @RequestBody MapaDto mapaDto) {
        var entidade = mapaMapper.toEntity(mapaDto);
        var salvo = mapaService.criar(entidade);
        URI uri = URI.create("/api/mapas/%d".formatted(salvo.getCodigo()));
        return ResponseEntity.created(uri).body(mapaMapper.toDto(salvo));
    }

    /**
     * Atualiza um mapa de competências existente.
     *
     * @param codMapa O código do mapa a ser atualizado.
     * @param mapaDto O DTO com os novos dados do mapa.
     * @return Um {@link ResponseEntity} com status 200 OK e o {@link MapaDto} atualizado.
     */
    @PostMapping("/{codMapa}/atualizar")
    @Operation(summary = "Atualiza um mapa existente")
    public ResponseEntity<MapaDto> atualizar(
            @PathVariable Long codMapa, @Valid @RequestBody MapaDto mapaDto) {
        var entidade = mapaMapper.toEntity(mapaDto);
        var atualizado = mapaService.atualizar(codMapa, entidade);
        return ResponseEntity.ok(mapaMapper.toDto(atualizado));
    }

    /**
     * Exclui um mapa de competências.
     *
     * @param codMapa O código do mapa a ser excluído.
     * @return Um {@link ResponseEntity} com status 204 No Content.
     */
    @PostMapping("/{codMapa}/excluir")
    @Operation(summary = "Exclui um mapa")
    public ResponseEntity<Void> excluir(@PathVariable Long codMapa) {
        mapaService.excluir(codMapa);
        return ResponseEntity.noContent().build();
    }
}
