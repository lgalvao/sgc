package sgc.mapa;

import com.fasterxml.jackson.annotation.*;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.*;
import jakarta.validation.*;
import lombok.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.*;
import sgc.mapa.model.*;
import sgc.mapa.service.*;

import java.net.*;
import java.util.*;

/**
 * Controlador REST para gerenciar Mapas.
 */
@RestController
@RequestMapping("/api/mapas")
@RequiredArgsConstructor
@Tag(name = "Mapas", description = "Endpoints para gerenciamento de mapas de competências")
public class MapaController {
    private final MapaManutencaoService mapaManutencaoService;

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
        return mapaManutencaoService.listarTodosMapas();
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
        var mapa = mapaManutencaoService.buscarMapaPorCodigo(codigo);
        return ResponseEntity.ok(mapa);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cria um mapa")
    @JsonView(MapaViews.Publica.class)
    public ResponseEntity<Mapa> criar(@Valid @RequestBody Mapa mapa) {
        var salvo = mapaManutencaoService.salvarMapa(mapa);
        Long codigo = Objects.requireNonNull(salvo.getCodigo(), "Código do mapa não pode ser nulo");

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{codigo}")
                .buildAndExpand(codigo)
                .toUri();

        return ResponseEntity.created(uri).body(salvo);
    }

    @PostMapping("/{codMapa}/atualizar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza um mapa existente")
    @JsonView(MapaViews.Publica.class)
    public ResponseEntity<Mapa> atualizar(@PathVariable Long codMapa, @Valid @RequestBody Mapa mapa) {
        Mapa existente = mapaManutencaoService.buscarMapaPorCodigo(codMapa)
                .setDataHoraDisponibilizado(mapa.getDataHoraDisponibilizado())
                .setObservacoesDisponibilizacao(mapa.getObservacoesDisponibilizacao())
                .setDataHoraHomologado(mapa.getDataHoraHomologado());

        var atualizado = mapaManutencaoService.salvarMapa(existente);
        return ResponseEntity.ok(atualizado);
    }

    @PostMapping("/{codMapa}/excluir")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Exclui um mapa")
    public ResponseEntity<Void> excluir(@PathVariable Long codMapa) {
        mapaManutencaoService.excluirMapa(codMapa);
        return ResponseEntity.noContent().build();
    }
}
