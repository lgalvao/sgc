package sgc.atividade;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

/**
 * Controlador REST para gerenciar Atividades usando DTOs.
 * Evita expor entidades JPA diretamente nas APIs.
 */
@RestController
@RequestMapping("/api/atividades")
@RequiredArgsConstructor
public class AtividadeControlador {
    private final RepositorioAtividade repositorioAtividade;
    private final AtividadeMapper atividadeMapper;

    @GetMapping
    public List<AtividadeDTO> listar() {
        return repositorioAtividade.findAll()
                .stream()
                .map(atividadeMapper::toDTO)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AtividadeDTO> obterPorId(@PathVariable Long id) {
        return repositorioAtividade.findById(id)
                .map(atividadeMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AtividadeDTO> criar(@Valid @RequestBody AtividadeDTO atividadeDto) {
        var entidade = atividadeMapper.toEntity(atividadeDto);
        var salvo = repositorioAtividade.save(entidade);
        URI uri = URI.create("/api/atividades/%d".formatted(salvo.getCodigo()));
        return ResponseEntity.created(uri).body(atividadeMapper.toDTO(salvo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AtividadeDTO> atualizar(@PathVariable Long id,
            @Valid @RequestBody AtividadeDTO atividadeDto) {
        return repositorioAtividade.findById(id)
                .map(existente -> {
                    // Mapeia os campos do DTO para a entidade existente
                    var entidadeParaAtualizar = atividadeMapper.toEntity(atividadeDto);
                    existente.setDescricao(entidadeParaAtualizar.getDescricao());
                    existente.setMapa(entidadeParaAtualizar.getMapa());

                    var atualizado = repositorioAtividade.save(existente);
                    return ResponseEntity.ok(atividadeMapper.toDTO(atualizado));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        return repositorioAtividade.findById(id)
                .map(_ -> {
                    repositorioAtividade.deleteById(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}