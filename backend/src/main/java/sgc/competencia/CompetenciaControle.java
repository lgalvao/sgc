package sgc.competencia;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sgc.competencia.dto.CompetenciaDto;
import sgc.competencia.modelo.CompetenciaAtividade;
import sgc.comum.erros.ErroDominioNaoEncontrado;

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
        try {
            return ResponseEntity.ok(competenciaService.obterCompetencia(id));
        } catch (ErroDominioNaoEncontrado e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<CompetenciaDto> criarCompetencia(@Valid @RequestBody CompetenciaDto competenciaDto) {
        var salvo = competenciaService.criarCompetencia(competenciaDto);
        URI uri = URI.create("/api/competencias/%d".formatted(salvo.codigo()));
        return ResponseEntity.created(uri).body(salvo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompetenciaDto> atualizarCompetencia(@PathVariable Long id, @Valid @RequestBody CompetenciaDto competenciaDto) {
        try {
            return ResponseEntity.ok(competenciaService.atualizarCompetencia(id, competenciaDto));
        } catch (ErroDominioNaoEncontrado e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirCompetencia(@PathVariable Long id) {
        try {
            competenciaService.excluirCompetencia(id);
            return ResponseEntity.noContent().build();
        } catch (ErroDominioNaoEncontrado e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{idCompetencia}/atividades")
    public ResponseEntity<List<CompetenciaAtividade>> listarAtividadesVinculadas(@PathVariable Long idCompetencia) {
        try {
            return ResponseEntity.ok(competenciaService.listarAtividadesVinculadas(idCompetencia));
        } catch (ErroDominioNaoEncontrado e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{idCompetencia}/atividades")
    public ResponseEntity<?> vincularAtividade(@PathVariable Long idCompetencia, @Valid @RequestBody CompetenciaControle.VinculoAtividadeReq requisicao) {
        try {
            var salvo = competenciaService.vincularAtividade(idCompetencia, requisicao.getIdAtividade());
            URI uri = URI.create("/api/competencias/%d/atividades".formatted(idCompetencia));
            return ResponseEntity.created(uri).body(salvo);
        } catch (ErroDominioNaoEncontrado e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }

    @DeleteMapping("/{idCompetencia}/atividades/{idAtividade}")
    public ResponseEntity<?> desvincularAtividade(@PathVariable Long idCompetencia, @PathVariable Long idAtividade) {
        try {
            competenciaService.desvincularAtividade(idCompetencia, idAtividade);
            return ResponseEntity.noContent().build();
        } catch (ErroDominioNaoEncontrado e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Setter
    @Getter
    public static class VinculoAtividadeReq {
        private Long idAtividade;
    }
}