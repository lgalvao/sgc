package sgc.competencia;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sgc.mapa.Mapa;

import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para gerenciar Competências usando DTOs.
 * Evita expor entidades JPA diretamente nas APIs.
 */
@RestController
@RequestMapping("/api/competencias")
@RequiredArgsConstructor
public class CompetenciaController {
    private final CompetenciaRepository competenciaRepository;
    private final CompetenciaMapper competenciaMapper;

    @GetMapping
    public List<CompetenciaDTO> listarCompetencias() {
        return competenciaRepository.findAll()
                .stream()
                .map(competenciaMapper::toDTO)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompetenciaDTO> obterCompetencia(@PathVariable Long id) {

        Optional<sgc.competencia.Competencia> c = competenciaRepository.findById(id);
        return c.map(competenciaMapper::toDTO).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CompetenciaDTO> criarCompetencia(@Valid @RequestBody CompetenciaDTO competenciaDto) {
        var entity = competenciaMapper.toEntity(competenciaDto);
        var salvo = competenciaRepository.save(entity);
        URI uri = URI.create("/api/competencias/%d".formatted(salvo.getCodigo()));
        return ResponseEntity.created(uri).body(competenciaMapper.toDTO(salvo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompetenciaDTO> atualizarCompetencia(@PathVariable Long id, @Valid @RequestBody CompetenciaDTO competenciaDto) {
        return competenciaRepository.findById(id)
                .map(existing -> {
                    if (competenciaDto.getMapaCodigo() != null) {
                        Mapa m = new Mapa();
                        m.setCodigo(competenciaDto.getMapaCodigo());
                        existing.setMapa(m);
                    } else {
                        existing.setMapa(null);
                    }
                    existing.setDescricao(competenciaDto.getDescricao());
                    var atualizado = competenciaRepository.save(existing);
                    return ResponseEntity.ok(competenciaMapper.toDTO(atualizado));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirCompetencia(@PathVariable Long id) {
        return competenciaRepository.findById(id)
                .map(_ -> {
                    competenciaRepository.deleteById(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
