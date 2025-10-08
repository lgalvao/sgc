package sgc.competencia;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.competencia.modelo.Competencia;
import sgc.competencia.modelo.CompetenciaAtividade;
import sgc.competencia.modelo.CompetenciaAtividade.Id;
import sgc.competencia.modelo.CompetenciaAtividadeRepo;
import sgc.competencia.modelo.CompetenciaRepo;

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
public class CompetenciaAtividadeControle {
    private final CompetenciaAtividadeRepo repositorioCompetenciaAtividade;
    private final AtividadeRepo atividadeRepo;
    private final CompetenciaRepo repositorioCompetencia;

    @GetMapping
    public List<CompetenciaAtividade> listar() {
        return repositorioCompetenciaAtividade.findAll();
    }

    @GetMapping("/por-atividade/{idAtividade}")
    public ResponseEntity<List<CompetenciaAtividade>> listarPorAtividade(@PathVariable Long idAtividade) {
        List<CompetenciaAtividade> lista = repositorioCompetenciaAtividade.findAll()
                .stream()
                .filter(ca -> ca.getId() != null && idAtividade.equals(ca.getId().getAtividadeCodigo()))
                .toList();
        return ResponseEntity.ok(lista);
    }

    @PostMapping
    public ResponseEntity<?> vincular(@Valid @RequestBody RequisicaoVinculo requisicao) {
        Long idAtividade = requisicao.getIdAtividade();
        Long idCompetencia = requisicao.getIdCompetencia();

        Optional<Atividade> maybeAtividade = atividadeRepo.findById(idAtividade);
        if (maybeAtividade.isEmpty()) {
            return ResponseEntity.badRequest().body("Atividade não encontrada: %d".formatted(idAtividade));
        }

        Optional<Competencia> maybeCompetencia = repositorioCompetencia.findById(idCompetencia);
        if (maybeCompetencia.isEmpty()) {
            return ResponseEntity.badRequest().body("Competência não encontrada: %d".formatted(idCompetencia));
        }

        Id id = new Id(idAtividade, idCompetencia);
        if (repositorioCompetenciaAtividade.existsById(id)) {
            return ResponseEntity.status(409).body("Vínculo já existe.");
        }

        CompetenciaAtividade vinculo = new CompetenciaAtividade();
        vinculo.setId(id);
        vinculo.setAtividade(maybeAtividade.get());
        vinculo.setCompetencia(maybeCompetencia.get());

        CompetenciaAtividade salvo = repositorioCompetenciaAtividade.save(vinculo);
        URI uri = URI.create("/api/competencia-atividades?atividade=%d&competencia=%d".formatted(idAtividade, idCompetencia));
        return ResponseEntity.created(uri).body(salvo);
    }

    @DeleteMapping
    public ResponseEntity<?> desvincular(@RequestParam Long idAtividade, @RequestParam Long idCompetencia) {
        Id id = new Id(idAtividade, idCompetencia);
        if (!repositorioCompetenciaAtividade.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repositorioCompetenciaAtividade.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Setter
    @Getter
    public static class RequisicaoVinculo {
        private Long idAtividade;
        private Long idCompetencia;
    }
}