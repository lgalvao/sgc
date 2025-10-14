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

    // Endpoints para Atividades

    @GetMapping
    public List<AtividadeDto> listar() {
        return atividadeService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AtividadeDto> obterPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(atividadeService.obterPorId(id));
        } catch (ErroDominioNaoEncontrado e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<AtividadeDto> criar(@Valid @RequestBody AtividadeDto atividadeDto, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            var salvo = atividadeService.criar(atividadeDto, userDetails.getUsername());
            URI uri = URI.create("/api/atividades/%d".formatted(salvo.codigo()));
            return ResponseEntity.created(uri).body(salvo);
        } catch (ErroDominioNaoEncontrado e) {
            return ResponseEntity.badRequest().build();
        } catch (ErroDominioAccessoNegado e) {
            return ResponseEntity.status(403).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.unprocessableEntity().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<AtividadeDto> atualizar(@PathVariable Long id, @Valid @RequestBody AtividadeDto atividadeDto) {
        try {
            return ResponseEntity.ok(atividadeService.atualizar(id, atividadeDto));
        } catch (ErroDominioNaoEncontrado e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        try {
            atividadeService.excluir(id);
            return ResponseEntity.noContent().build();
        } catch (ErroDominioNaoEncontrado e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Endpoints para Conhecimentos aninhados em Atividades

    @GetMapping("/{atividadeId}/conhecimentos")
    public ResponseEntity<List<ConhecimentoDto>> listarConhecimentos(@PathVariable Long atividadeId) {
        try {
            return ResponseEntity.ok(atividadeService.listarConhecimentos(atividadeId));
        } catch (ErroDominioNaoEncontrado e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{atividadeId}/conhecimentos")
    public ResponseEntity<ConhecimentoDto> criarConhecimento(@PathVariable Long atividadeId, @Valid @RequestBody ConhecimentoDto conhecimentoDto) {
        try {
            var salvo = atividadeService.criarConhecimento(atividadeId, conhecimentoDto);
            URI uri = URI.create("/api/atividades/%d/conhecimentos/%d".formatted(atividadeId, salvo.codigo()));
            return ResponseEntity.created(uri).body(salvo);
        } catch (ErroDominioNaoEncontrado e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{atividadeId}/conhecimentos/{conhecimentoId}")
    public ResponseEntity<ConhecimentoDto> atualizarConhecimento(@PathVariable Long atividadeId, @PathVariable Long conhecimentoId, @Valid @RequestBody ConhecimentoDto conhecimentoDto) {
        try {
            return ResponseEntity.ok(atividadeService.atualizarConhecimento(atividadeId, conhecimentoId, conhecimentoDto));
        } catch (ErroDominioNaoEncontrado e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{atividadeId}/conhecimentos/{conhecimentoId}")
    public ResponseEntity<Void> excluirConhecimento(@PathVariable Long atividadeId, @PathVariable Long conhecimentoId) {
        try {
            atividadeService.excluirConhecimento(atividadeId, conhecimentoId);
            return ResponseEntity.noContent().build();
        } catch (ErroDominioNaoEncontrado e) {
            return ResponseEntity.notFound().build();
        }
    }
}