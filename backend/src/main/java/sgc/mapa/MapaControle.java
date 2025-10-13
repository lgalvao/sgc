package sgc.mapa;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import sgc.comum.modelo.Usuario;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.MapaDto;
import sgc.mapa.dto.MapaMapper;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;

import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para gerenciar Mapas usando DTOs.
 * Evita expor entidades JPA diretamente nas APIs.
 */
@RestController
@RequestMapping("/api/mapas")
@RequiredArgsConstructor
public class MapaControle {
    private final MapaRepo repositorioMapa;
    private final MapaService mapaService;
    private final MapaMapper mapaMapper;

    @GetMapping
    public List<MapaDto> listar() {
        return repositorioMapa.findAll()
                .stream()
                .map(mapaMapper::toDTO)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MapaDto> obterPorId(@PathVariable Long id) {
        Optional<Mapa> m = repositorioMapa.findById(id);
        return m.map(mapaMapper::toDTO).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<MapaDto> criar(@Valid @RequestBody MapaDto mapaDto) {
        var entidade = mapaMapper.toEntity(mapaDto);
        var salvo = repositorioMapa.save(entidade);
        URI uri = URI.create("/api/mapas/%d".formatted(salvo.getCodigo()));
        return ResponseEntity.created(uri).body(mapaMapper.toDTO(salvo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MapaDto> atualizar(@PathVariable Long id, @Valid @RequestBody MapaDto mapaDto) {
        return repositorioMapa.findById(id)
                .map(existente -> {
                    existente.setDataHoraDisponibilizado(mapaDto.getDataHoraDisponibilizado());
                    existente.setObservacoesDisponibilizacao(mapaDto.getObservacoesDisponibilizacao());
                    existente.setSugestoesApresentadas(mapaDto.getSugestoesApresentadas());
                    existente.setDataHoraHomologado(mapaDto.getDataHoraHomologado());
                    var atualizado = repositorioMapa.save(existente);
                    return ResponseEntity.ok(mapaMapper.toDTO(atualizado));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        return repositorioMapa.findById(id)
                .map(x -> {
                    repositorioMapa.deleteById(id);
                    return ResponseEntity.noContent().<Void>build();
                }).orElseGet(() -> ResponseEntity.notFound().build());
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
            MapaCompletoDto mapa = mapaService.salvarMapaCompleto(id, request, usuario.getTitulo());
            return ResponseEntity.ok(mapa);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}