package sgc.mapa;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.MapaDto;
import sgc.mapa.dto.MapaMapper;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.sgrh.Usuario;

import java.net.URI;
import java.util.List;

/**
 * Controlador REST para gerenciar Mapas usando DTOs.
 * Evita expor entidades JPA diretamente nas APIs.
 */
@RestController
@RequestMapping("/api/mapas")
@RequiredArgsConstructor
public class MapaControle {
    private final MapaService mapaService;
    private final MapaCrudService mapaCrudService;
    private final MapaMapper mapaMapper;

    @GetMapping
    public List<MapaDto> listar() {
        return mapaCrudService.listar()
                .stream()
                .map(mapaMapper::toDTO)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MapaDto> obterPorId(@PathVariable Long id) {
        try {
            var mapa = mapaCrudService.obterPorId(id);
            return ResponseEntity.ok(mapaMapper.toDTO(mapa));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<MapaDto> criar(@Valid @RequestBody MapaDto mapaDto) {
        var entidade = mapaMapper.toEntity(mapaDto);
        var salvo = mapaCrudService.criar(entidade);
        URI uri = URI.create("/api/mapas/%d".formatted(salvo.getCodigo()));
        return ResponseEntity.created(uri).body(mapaMapper.toDTO(salvo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MapaDto> atualizar(@PathVariable Long id, @Valid @RequestBody MapaDto mapaDto) {
        try {
            var entidade = mapaMapper.toEntity(mapaDto);
            var atualizado = mapaCrudService.atualizar(id, entidade);
            return ResponseEntity.ok(mapaMapper.toDTO(atualizado));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        try {
            mapaCrudService.excluir(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * CDU-15 - Obter mapa completo com competências e atividades aninhadas.
     * GET /api/mapas/{id}/completo
     */
    @GetMapping("/{id}/completo")
    public ResponseEntity<MapaCompletoDto> obterCompleto(@PathVariable Long id) {
        try {
            MapaCompletoDto mapa = mapaService.obterMapaCompleto(id);
            return ResponseEntity.ok(mapa);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * CDU-15 - Salvar mapa completo (criar/editar competências + vínculos).
     * PUT /api/mapas/{id}/completo
     */
    @PutMapping("/{id}/completo")
    @Transactional
    public ResponseEntity<MapaCompletoDto> salvarCompleto(
            @PathVariable Long id,
            @RequestBody @Valid SalvarMapaRequest request,
            @AuthenticationPrincipal Usuario usuario
    ) {
        try {
            MapaCompletoDto mapa = mapaService.salvarMapaCompleto(id, request, usuario.getTituloEleitoral());
            return ResponseEntity.ok(mapa);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}