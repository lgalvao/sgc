package sgc.mapa;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
public class MapaController {
    private final MapaRepository mapaRepository;

    @GetMapping
    public List<MapaDTO> listarMapas() {
        return mapaRepository.findAll()
                .stream()
                .map(MapaMapper::toDTO)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MapaDTO> obterMapa(@PathVariable Long id) {
        Optional<Mapa> m = mapaRepository.findById(id);
        return m.map(MapaMapper::toDTO).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<MapaDTO> criarMapa(@Valid @RequestBody MapaDTO mapaDto) {
        var entity = MapaMapper.toEntity(mapaDto);
        var salvo = mapaRepository.save(entity);
        URI uri = URI.create("/api/mapas/%d".formatted(salvo.getCodigo()));
        return ResponseEntity.created(uri).body(MapaMapper.toDTO(salvo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MapaDTO> atualizarMapa(@PathVariable Long id, @Valid @RequestBody MapaDTO mapaDto) {
        return mapaRepository.findById(id)
                .map(existing -> {
                    existing.setDataHoraDisponibilizado(mapaDto.getDataHoraDisponibilizado());
                    existing.setObservacoesDisponibilizacao(mapaDto.getObservacoesDisponibilizacao());
                    existing.setSugestoesApresentadas(mapaDto.getSugestoesApresentadas());
                    existing.setDataHoraHomologado(mapaDto.getDataHoraHomologado());
                    var atualizado = mapaRepository.save(existing);
                    return ResponseEntity.ok(MapaMapper.toDTO(atualizado));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirMapa(@PathVariable Long id) {
        return mapaRepository.findById(id)
                .map(existing -> {
                    mapaRepository.deleteById(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}