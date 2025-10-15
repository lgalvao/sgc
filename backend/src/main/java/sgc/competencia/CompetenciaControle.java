package sgc.competencia;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sgc.competencia.dto.CompetenciaDto;
import sgc.competencia.modelo.CompetenciaAtividade;

import java.net.URI;
import java.util.List;

/**
 * Controlador REST para gerenciar Competências e seus relacionamentos com Atividades.
 */
@RestController
@RequestMapping("/api/competencias")
@RequiredArgsConstructor
public class CompetenciaControle {
    private final CompetenciaService competenciaService;

    @GetMapping
    public List<CompetenciaDto> listarCompetencias() {
        return competenciaService.listarCompetencias();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompetenciaDto> obterCompetencia(@PathVariable Long id) {
        return ResponseEntity.ok(competenciaService.obterCompetencia(id));
    }

    @PostMapping
    public ResponseEntity<CompetenciaDto> criarCompetencia(@Valid @RequestBody CompetenciaDto competenciaDto) {
        var salvo = competenciaService.criarCompetencia(competenciaDto);
        URI uri = URI.create("/api/competencias/%d".formatted(salvo.getCodigo()));
        return ResponseEntity.created(uri).body(salvo.sanitize());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompetenciaDto> atualizarCompetencia(@PathVariable Long id, @Valid @RequestBody CompetenciaDto competenciaDto) {
        return ResponseEntity.ok(competenciaService.atualizarCompetencia(id, competenciaDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirCompetencia(@PathVariable Long id) {
        competenciaService.excluirCompetencia(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{idCompetencia}/atividades")
    public ResponseEntity<List<CompetenciaAtividade>> listarAtividadesVinculadas(@PathVariable Long idCompetencia) {
        return ResponseEntity.ok(competenciaService.listarAtividadesVinculadas(idCompetencia));
    }

    @PostMapping("/{idCompetencia}/atividades")
    public ResponseEntity<?> vincularAtividade(@PathVariable Long idCompetencia, @Valid @RequestBody CompetenciaControle.VinculoAtividadeReq requisicao) {
        var salvo = competenciaService.vincularAtividade(idCompetencia, requisicao.getIdAtividade());
        URI uri = URI.create("/api/competencias/%d/atividades/%d".formatted(idCompetencia, requisicao.getIdAtividade()));
        return ResponseEntity.created(uri).body(salvo);
    }

    @DeleteMapping("/{idCompetencia}/atividades/{idAtividade}")
    public ResponseEntity<?> desvincularAtividade(@PathVariable Long idCompetencia, @PathVariable Long idAtividade) {
        competenciaService.desvincularAtividade(idCompetencia, idAtividade);
        return ResponseEntity.noContent().build();
    }

    @Setter
    @Getter
    public static class VinculoAtividadeReq {
        private Long idAtividade;
    }
}