package sgc.atividade;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import sgc.atividade.dto.AtividadeDto;
import io.swagger.v3.oas.annotations.Operation;
import sgc.conhecimento.dto.ConhecimentoDto;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.List;

/**
 * Controlador REST para gerenciar Atividades e seus Conhecimentos associados.
 */
@RestController
@RequestMapping("/api/atividades")
@RequiredArgsConstructor
@Tag(name = "Atividades", description = "Endpoints para gerenciamento de atividades e seus conhecimentos associados")
public class AtividadeControle {
    private final AtividadeService atividadeService;

    @GetMapping
    @Operation(summary = "Lista todas as atividades")
    public List<AtividadeDto> listar() {
        return atividadeService.listar();
    }

    @GetMapping("/{idAtividade}")
    @Operation(summary = "Obtém uma atividade pelo ID")
    public ResponseEntity<AtividadeDto> obterPorId(@PathVariable Long idAtividade) {
        return ResponseEntity.ok(atividadeService.obterPorId(idAtividade));
    }

    @PostMapping
    @Operation(summary = "Cria uma nova atividade")
    public ResponseEntity<AtividadeDto> criar(@Valid @RequestBody AtividadeDto atividadeDto, @AuthenticationPrincipal UserDetails userDetails) {
        var sanitizedAtividadeDto = atividadeDto.sanitize();
        var salvo = atividadeService.criar(sanitizedAtividadeDto, userDetails.getUsername());
        URI uri = URI.create("/api/atividades/%d".formatted(salvo.codigo()));
        return ResponseEntity.created(uri).body(salvo.sanitize());
    }


    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma atividade existente")
    public ResponseEntity<AtividadeDto> atualizar(@PathVariable Long id, @Valid @RequestBody AtividadeDto atividadeDto) {
        var sanitizedAtividadeDto = atividadeDto.sanitize();
        return ResponseEntity.ok(atividadeService.atualizar(id, sanitizedAtividadeDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma atividade")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        atividadeService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{atividadeId}/conhecimentos")
    @Operation(summary = "Lista todos os conhecimentos de uma atividade")
    public ResponseEntity<List<ConhecimentoDto>> listarConhecimentos(@PathVariable Long atividadeId) {
        return ResponseEntity.ok(atividadeService.listarConhecimentos(atividadeId));
    }

    @PostMapping("/{atividadeId}/conhecimentos")
    @Operation(summary = "Cria um novo conhecimento para uma atividade")
    public ResponseEntity<ConhecimentoDto> criarConhecimento(@PathVariable Long atividadeId, @Valid @RequestBody ConhecimentoDto conhecimentoDto) {
        var sanitizedConhecimentoDto = conhecimentoDto.sanitize();
        var salvo = atividadeService.criarConhecimento(atividadeId, sanitizedConhecimentoDto);
        URI uri = URI.create("/api/atividades/%d/conhecimentos/%d".formatted(atividadeId, salvo.codigo()));
        return ResponseEntity.created(uri).body(salvo.sanitize());
    }

    @PutMapping("/{atividadeId}/conhecimentos/{conhecimentoId}")
    @Operation(summary = "Atualiza um conhecimento existente em uma atividade")
    public ResponseEntity<ConhecimentoDto> atualizarConhecimento(@PathVariable Long atividadeId, @PathVariable Long conhecimentoId, @Valid @RequestBody ConhecimentoDto conhecimentoDto) {
        var sanitizedConhecimentoDto = conhecimentoDto.sanitize();
        return ResponseEntity.ok(atividadeService.atualizarConhecimento(atividadeId, conhecimentoId, sanitizedConhecimentoDto));
    }

    @DeleteMapping("/{atividadeId}/conhecimentos/{conhecimentoId}")
    @Operation(summary = "Exclui um conhecimento de uma atividade")
    public ResponseEntity<Void> excluirConhecimento(@PathVariable Long atividadeId, @PathVariable Long conhecimentoId) {
        atividadeService.excluirConhecimento(atividadeId, conhecimentoId);
        return ResponseEntity.noContent().build();
    }
}