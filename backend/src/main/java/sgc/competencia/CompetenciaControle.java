package sgc.competencia;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sgc.competencia.dto.CompetenciaDto;
import sgc.competencia.dto.CompetenciaMapper;
import sgc.competencia.modelo.Competencia;
import sgc.competencia.modelo.CompetenciaRepo;
import sgc.mapa.modelo.Mapa;

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
public class CompetenciaControle {
    private final CompetenciaRepo competenciaRepo;
    private final CompetenciaMapper competenciaMapper;

    @GetMapping
    public List<CompetenciaDto> listarCompetencias() {
        return competenciaRepo.findAll()
                .stream()
                .map(competenciaMapper::toDTO)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompetenciaDto> obterCompetencia(@PathVariable Long id) {

        Optional<Competencia> c = competenciaRepo.findById(id);
        return c.map(competenciaMapper::toDTO).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CompetenciaDto> criarCompetencia(@Valid @RequestBody CompetenciaDto competenciaDto) {
        var entity = competenciaMapper.toEntity(competenciaDto);
        var salvo = competenciaRepo.save(entity);
        URI uri = URI.create("/api/competencias/%d".formatted(salvo.getCodigo()));
        return ResponseEntity.created(uri).body(competenciaMapper.toDTO(salvo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompetenciaDto> atualizarCompetencia(@PathVariable Long id, @Valid @RequestBody CompetenciaDto competenciaDto) {
        return competenciaRepo.findById(id)
                .map(existing -> {
                    if (competenciaDto.getMapaCodigo() != null) {
                        Mapa m = new Mapa();
                        m.setCodigo(competenciaDto.getMapaCodigo());
                        existing.setMapa(m);
                    } else {
                        existing.setMapa(null);
                    }
                    existing.setDescricao(competenciaDto.getDescricao());
                    var atualizado = competenciaRepo.save(existing);
                    return ResponseEntity.ok(competenciaMapper.toDTO(atualizado));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirCompetencia(@PathVariable Long id) {
        return competenciaRepo.findById(id)
                .map(_ -> {
                    competenciaRepo.deleteById(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
