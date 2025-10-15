package sgc.atividade;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import sgc.atividade.dto.AtividadeDto;
import sgc.comum.erros.ErroDominioAccessoNegado;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.conhecimento.dto.ConhecimentoDto;

import java.net.URI;
import java.util.List;

/**
 * Controlador REST para gerenciar Atividades e seus Conhecimentos associados.
 */
@RestController
@RequestMapping("/api/atividades")
@RequiredArgsConstructor
public class AtividadeControle {
    private final AtividadeService atividadeService;

    @GetMapping
    public List<AtividadeDto> listar() {
        return atividadeService.listar();
    }

    @GetMapping("/{idAtividade}")
    public ResponseEntity<AtividadeDto> obterPorId(@PathVariable Long idAtividade) {
        return ResponseEntity.ok(atividadeService.obterPorId(idAtividade));
    }

    @PostMapping
    public ResponseEntity<AtividadeDto> criar(@Valid @RequestBody AtividadeDto atividadeDto, @AuthenticationPrincipal UserDetails userDetails) {
        var salvo = atividadeService.criar(atividadeDto, userDetails.getUsername());
        URI uri = URI.create("/api/atividades/%d".formatted(salvo.codigo()));
        return ResponseEntity.created(uri).body(salvo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AtividadeDto> atualizar(@PathVariable Long id, @Valid @RequestBody AtividadeDto atividadeDto) {
        return ResponseEntity.ok(atividadeService.atualizar(id, atividadeDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        atividadeService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{atividadeId}/conhecimentos")
    public ResponseEntity<List<ConhecimentoDto>> listarConhecimentos(@PathVariable Long atividadeId) {
        return ResponseEntity.ok(atividadeService.listarConhecimentos(atividadeId));
    }

    @PostMapping("/{atividadeId}/conhecimentos")
    public ResponseEntity<ConhecimentoDto> criarConhecimento(@PathVariable Long atividadeId, @Valid @RequestBody ConhecimentoDto conhecimentoDto) {
        var salvo = atividadeService.criarConhecimento(atividadeId, conhecimentoDto);
        URI uri = URI.create("/api/atividades/%d/conhecimentos/%d".formatted(atividadeId, salvo.codigo()));
        return ResponseEntity.created(uri).body(salvo);
    }

    @PutMapping("/{atividadeId}/conhecimentos/{conhecimentoId}")
    public ResponseEntity<ConhecimentoDto> atualizarConhecimento(@PathVariable Long atividadeId, @PathVariable Long conhecimentoId, @Valid @RequestBody ConhecimentoDto conhecimentoDto) {
        return ResponseEntity.ok(atividadeService.atualizarConhecimento(atividadeId, conhecimentoId, conhecimentoDto));
    }

    @DeleteMapping("/{atividadeId}/conhecimentos/{conhecimentoId}")
    public ResponseEntity<Void> excluirConhecimento(@PathVariable Long atividadeId, @PathVariable Long conhecimentoId) {
        atividadeService.excluirConhecimento(atividadeId, conhecimentoId);
        return ResponseEntity.noContent().build();
    }
}