package sgc.competencia;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sgc.atividade.Atividade;
import sgc.atividade.AtividadeRepository;
import sgc.competencia.CompetenciaAtividade.Id;

import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para gerenciar a associação N-N entre Competência e Atividade.
 * Endpoints para vincular e desvincular competências e atividades.
 */
@RestController
@RequestMapping("/api/competencia-atividades")
@RequiredArgsConstructor
public class CompetenciaAtividadeController {
    private final CompetenciaAtividadeRepository competenciaAtividadeRepository;
    private final AtividadeRepository atividadeRepository;
    private final CompetenciaRepository competenciaRepository;

    @GetMapping
    public List<CompetenciaAtividade> listarVinculos() {
        return competenciaAtividadeRepository.findAll();
    }

    @GetMapping("/por-atividade/{atividadeId}")
    public ResponseEntity<List<CompetenciaAtividade>> listarPorAtividade(@PathVariable Long atividadeId) {
        List<CompetenciaAtividade> lista = competenciaAtividadeRepository.findAll()
                .stream()
                .filter(ca -> ca.getId() != null && atividadeId.equals(ca.getId().getAtividadeCodigo()))
                .toList();
        return ResponseEntity.ok(lista);
    }

    @PostMapping
    public ResponseEntity<?> vincular(@Valid @RequestBody VinculoRequest req) {
        Long atividadeId = req.getAtividadeCodigo();
        Long competenciaId = req.getCompetenciaCodigo();

        Optional<Atividade> maybeAtividade = atividadeRepository.findById(atividadeId);
        if (maybeAtividade.isEmpty()) {
            return ResponseEntity.badRequest().body("Atividade não encontrada: %d".formatted(atividadeId));
        }

        Optional<Competencia> maybeCompetencia = competenciaRepository.findById(competenciaId);
        if (maybeCompetencia.isEmpty()) {
            return ResponseEntity.badRequest().body("Competência não encontrada: %d".formatted(competenciaId));
        }

        Id id = new Id(atividadeId, competenciaId);
        if (competenciaAtividadeRepository.existsById(id)) {
            return ResponseEntity.status(409).body("Vínculo já existe.");
        }

        CompetenciaAtividade vinculo = new CompetenciaAtividade();
        vinculo.setId(id);
        vinculo.setAtividade(maybeAtividade.get());
        vinculo.setCompetencia(maybeCompetencia.get());

        CompetenciaAtividade salvo = competenciaAtividadeRepository.save(vinculo);
        URI uri = URI.create("/api/competencia-atividades?atividade=%d&competencia=%d".formatted(atividadeId, competenciaId));
        return ResponseEntity.created(uri).body(salvo);
    }

    @DeleteMapping
    public ResponseEntity<?> desvincular(@RequestParam Long atividadeCodigo, @RequestParam Long competenciaCodigo) {
        Id id = new Id(atividadeCodigo, competenciaCodigo);
        if (!competenciaAtividadeRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        competenciaAtividadeRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // DTO simples para requisições de vinculação
    @Setter
    @Getter
    public static class VinculoRequest {
        private Long atividadeCodigo;
        private Long competenciaCodigo;

    }
}