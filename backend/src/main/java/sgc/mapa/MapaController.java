package sgc.mapa;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaViews;

import java.net.URI;
import java.util.List;

/**
 * Controlador REST para gerenciar Mapas.
 */
@RestController
@RequestMapping("/api/mapas")
@RequiredArgsConstructor
@Tag(name = "Mapas", description = "Endpoints para gerenciamento de mapas de competências")
public class MapaController {
    private final MapaFacade mapaFacade;

    /**
     * Retorna uma lista com todos os mapas de competências.
     *
     * @return Uma {@link List} de {@link Mapa}.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'CHEFE')")
    @Operation(summary = "Lista todos os mapas")
    @JsonView(MapaViews.Publica.class)
    public List<Mapa> listar() {
        return mapaFacade.todosMapas();
    }

    /**
     * Busca e retorna um mapa de competências específico pelo seu código.
     *
     * @param codigo O código do mapa a ser buscado.
     * @return Um {@link ResponseEntity} contendo o {@link Mapa} correspondente.
     */
    @GetMapping("/{codigo}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'CHEFE')")
    @Operation(summary = "Obtém um mapa pelo código")
    @JsonView(MapaViews.Publica.class)
    public ResponseEntity<Mapa> obterPorId(@PathVariable Long codigo) {
        var mapa = mapaFacade.mapaPorCodigo(codigo);
        return ResponseEntity.ok(mapa);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cria um mapa")
    @JsonView(MapaViews.Publica.class)
    public ResponseEntity<Mapa> criar(@Valid @RequestBody Mapa mapa) {
        var salvo = mapaFacade.salvar(mapa);

        URI uri = URI.create("/api/mapas/%d".formatted(salvo.getCodigo()));
        return ResponseEntity.created(uri).body(salvo);
    }

    @PostMapping("/{codMapa}/atualizar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza um mapa existente")
    @JsonView(MapaViews.Publica.class)
    public ResponseEntity<Mapa> atualizar(@PathVariable Long codMapa, @Valid @RequestBody Mapa mapa) {
        var atualizado = mapaFacade.atualizar(codMapa, mapa);
        return ResponseEntity.ok(atualizado);
    }

    @PostMapping("/{codMapa}/excluir")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Exclui um mapa")
    public ResponseEntity<Void> excluir(@PathVariable Long codMapa) {
        mapaFacade.excluir(codMapa);
        return ResponseEntity.noContent().build();
    }
}
