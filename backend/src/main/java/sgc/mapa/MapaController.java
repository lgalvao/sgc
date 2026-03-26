package sgc.mapa;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.*;
import jakarta.validation.*;
import lombok.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.*;
import sgc.mapa.dto.*;
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
@PreAuthorize("isAuthenticated()")
public class MapaController {
    private final MapaManutencaoService mapaManutencaoService;

    /**
     * Retorna uma lista com todos os mapas de competências.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'CHEFE')")
    @Operation(summary = "Lista todos os mapas")
    public List<MapaResumoDto> listar() {
        return mapaManutencaoService.mapas().stream()
                .map(MapaResumoDto::fromEntity)
                .toList();
    }

    /**
     * Busca e retorna um mapa de competências específico pelo seu código.
     */
    @GetMapping("/{codigo}")
    @PreAuthorize("hasPermission(#codigo, 'Mapa', 'VISUALIZAR_SUBPROCESSO')")
    @Operation(summary = "Obtém um mapa pelo código")
    public ResponseEntity<MapaResumoDto> obterPorCodigo(@PathVariable Long codigo) {
        var mapa = mapaManutencaoService.mapaCodigo(codigo);
        return ResponseEntity.ok(MapaResumoDto.fromEntity(mapa));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cria um mapa")
    public ResponseEntity<MapaResumoDto> criar(@Valid @RequestBody CriarMapaRequest request) {
        var salvo = mapaManutencaoService.criarMapa(request);
        Long codigo = Objects.requireNonNull(salvo.getCodigo(), "Código do mapa não pode ser nulo");

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{codigo}")
                .buildAndExpand(codigo)
                .toUri();

        return ResponseEntity.created(uri).body(MapaResumoDto.fromEntity(salvo));
    }

    @PostMapping("/{codMapa}/atualizar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza um mapa existente")
    public ResponseEntity<MapaResumoDto> atualizar(@PathVariable Long codMapa, @Valid @RequestBody AtualizarMapaRequest request) {
        var atualizado = mapaManutencaoService.atualizarMapa(codMapa, request);
        return ResponseEntity.ok(MapaResumoDto.fromEntity(atualizado));
    }

    @PostMapping("/{codMapa}/excluir")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Exclui um mapa")
    public ResponseEntity<Void> excluir(@PathVariable Long codMapa) {
        mapaManutencaoService.excluirMapa(codMapa);
        return ResponseEntity.noContent().build();
    }
}
