package sgc.competencia;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.competencia.dto.CompetenciaDto;
import sgc.competencia.dto.CompetenciaMapper;
import sgc.competencia.modelo.Competencia;
import sgc.competencia.modelo.CompetenciaAtividade;
import sgc.competencia.modelo.CompetenciaAtividade.Id;
import sgc.competencia.modelo.CompetenciaAtividadeRepo;
import sgc.competencia.modelo.CompetenciaRepo;
import sgc.mapa.modelo.Mapa;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controlador REST para gerenciar Competências e seus relacionamentos com Atividades.
 */
@RestController
@RequestMapping("/api/competencias")
@RequiredArgsConstructor
public class CompetenciaControle {
    private final CompetenciaRepo competenciaRepo;
    private final CompetenciaMapper competenciaMapper;
    private final AtividadeRepo atividadeRepo;
    private final CompetenciaAtividadeRepo competenciaAtividadeRepo;

    @GetMapping
    public List<CompetenciaDto> listarCompetencias() {
        return competenciaRepo.findAll()
                .stream()
                .map(competenciaMapper::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompetenciaDto> obterCompetencia(@PathVariable Long id) {
        return competenciaRepo.findById(id)
                .map(competenciaMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
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
                    if (competenciaDto.mapaCodigo() != null) {
                        Mapa m = new Mapa();
                        m.setCodigo(competenciaDto.mapaCodigo());
                        existing.setMapa(m);
                    } else {
                        existing.setMapa(null);
                    }
                    existing.setDescricao(competenciaDto.descricao());
                    var atualizado = competenciaRepo.save(existing);
                    return ResponseEntity.ok(competenciaMapper.toDTO(atualizado));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirCompetencia(@PathVariable Long id) {
        return competenciaRepo.findById(id)
                .map(x -> {
                    competenciaRepo.deleteById(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{idCompetencia}/atividades")
    public ResponseEntity<List<CompetenciaAtividade>> listarAtividadesVinculadas(@PathVariable Long idCompetencia) {
        if (!competenciaRepo.existsById(idCompetencia)) {
            return ResponseEntity.notFound().build();
        }
        List<CompetenciaAtividade> lista = competenciaAtividadeRepo.findAll()
                .stream()
                .filter(ca -> ca.getId() != null && idCompetencia.equals(ca.getId().getCompetenciaCodigo()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @PostMapping("/{idCompetencia}/atividades")
    public ResponseEntity<?> vincularAtividade(@PathVariable Long idCompetencia, @Valid @RequestBody CompetenciaControle.VinculoAtividadeReq requisicao) {
        Long idAtividade = requisicao.getIdAtividade();

        Optional<Competencia> maybeCompetencia = competenciaRepo.findById(idCompetencia);
        if (maybeCompetencia.isEmpty()) {
            return ResponseEntity.badRequest().body("Competência não encontrada: %d".formatted(idCompetencia));
        }

        Optional<Atividade> maybeAtividade = atividadeRepo.findById(idAtividade);
        if (maybeAtividade.isEmpty()) {
            return ResponseEntity.badRequest().body("Atividade não encontrada: %d".formatted(idAtividade));
        }

        Id id = new Id(idAtividade, idCompetencia);
        if (competenciaAtividadeRepo.existsById(id)) {
            return ResponseEntity.status(409).body("Vínculo já existe.");
        }

        CompetenciaAtividade vinculo = new CompetenciaAtividade();
        vinculo.setId(id);
        vinculo.setAtividade(maybeAtividade.get());
        vinculo.setCompetencia(maybeCompetencia.get());

        CompetenciaAtividade salvo = competenciaAtividadeRepo.save(vinculo);
        URI uri = URI.create("/api/competencias/%d/atividades".formatted(idCompetencia));
        return ResponseEntity.created(uri).body(salvo);
    }

    @DeleteMapping("/{idCompetencia}/atividades/{idAtividade}")
    public ResponseEntity<?> desvincularAtividade(@PathVariable Long idCompetencia, @PathVariable Long idAtividade) {
        Id id = new Id(idAtividade, idCompetencia);
        if (!competenciaAtividadeRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        competenciaAtividadeRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Setter
    @Getter
    public static class VinculoAtividadeReq {
        private Long idAtividade;
    }
}