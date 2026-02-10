package sgc.mapa;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sgc.mapa.dto.MapaDto;
import sgc.mapa.mapper.MapaMapper;
import sgc.mapa.service.MapaFacade;

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
    private final MapaFacade mapaFacade;
    private final MapaMapper mapaMapper;

    /**
     * Retorna uma lista com todos os mapas de competências.
     *
     * @return Uma {@link List} de {@link MapaDto}.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'CHEFE')")
    @Operation(summary = "Lista todos os mapas")
    public List<MapaDto> listar() {
        return mapaFacade.listar().stream()
                .map(mapaMapper::toDto)
                .toList();
    }

    /**
     * Busca e retorna um mapa de competências específico pelo seu código.
     *
     * @param codigo O código do mapa a ser buscado.
     * @return Um {@link ResponseEntity} contendo o {@link MapaDto} correspondente.
     */
    @GetMapping("/{codigo}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'CHEFE')")
    @Operation(summary = "Obtém um mapa pelo código")
    public ResponseEntity<MapaDto> obterPorId(@PathVariable Long codigo) {
        var mapa = mapaFacade.obterPorCodigo(codigo);
        var dto = mapaMapper.toDto(mapa);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cria um mapa")
    public ResponseEntity<MapaDto> criar(@Valid @RequestBody MapaDto mapaDto) {
        var entidade = mapaMapper.toEntity(mapaDto);
        var salvo = mapaFacade.salvar(entidade);

        URI uri = URI.create("/api/mapas/%d".formatted(salvo.getCodigo()));
        var salvoDto = mapaMapper.toDto(salvo);
        return ResponseEntity.created(uri).body(salvoDto);
    }

    @PostMapping("/{codMapa}/atualizar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza um mapa existente")
    public ResponseEntity<MapaDto> atualizar(@PathVariable Long codMapa, @Valid @RequestBody MapaDto mapaDto) {
        var entidade = mapaMapper.toEntity(mapaDto);
        var atualizado = mapaFacade.atualizar(codMapa, entidade);
        var atualizadoDto = mapaMapper.toDto(atualizado);
        return ResponseEntity.ok(atualizadoDto);
    }

    @PostMapping("/{codMapa}/excluir")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Exclui um mapa")
    public ResponseEntity<Void> excluir(@PathVariable Long codMapa) {
        mapaFacade.excluir(codMapa);
        return ResponseEntity.noContent().build();
    }
}
