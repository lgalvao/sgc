package sgc.atividade;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sgc.mapa.Mapa;

import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para gerenciar Atividades usando DTOs.
 * Evita expor entidades JPA diretamente nas APIs.
 */
@RestController
@RequestMapping("/api/atividades")
@RequiredArgsConstructor
public class AtividadeController {
    private final AtividadeRepository atividadeRepository;
    private final AtividadeMapper atividadeMapper;

    @GetMapping
    public List<AtividadeDTO> listarAtividades() {
        return atividadeRepository.findAll()
                .stream()
                .map(atividadeMapper::toDTO)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AtividadeDTO> obterAtividade(@PathVariable Long id) {
        return atividadeRepository.findById(id)
                .map(atividadeMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AtividadeDTO> criarAtividade(@Valid @RequestBody AtividadeDTO atividadeDto) {
        var entity = atividadeMapper.toEntity(atividadeDto);
        var salvo = atividadeRepository.save(entity);
        URI uri = URI.create("/api/atividades/%d".formatted(salvo.getCodigo()));
        return ResponseEntity.created(uri).body(atividadeMapper.toDTO(salvo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AtividadeDTO> atualizarAtividade(@PathVariable Long id,
                                                           @Valid @RequestBody AtividadeDTO atividadeDto) {
        return atividadeRepository.findById(id)
                .map(existing -> {
                    // Mapeia os campos do DTO para a entidade existente
                    var entityToUpdate = atividadeMapper.toEntity(atividadeDto);
                    existing.setDescricao(entityToUpdate.getDescricao());
                    existing.setMapa(entityToUpdate.getMapa());
                    
                    var atualizado = atividadeRepository.save(existing);
                    return ResponseEntity.ok(atividadeMapper.toDTO(atualizado));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirAtividade(@PathVariable Long id) {
        return atividadeRepository.findById(id)
                .map(existing -> {
                    atividadeRepository.deleteById(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}