package sgc.conhecimento;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sgc.conhecimento.dto.ConhecimentoDto;
import sgc.conhecimento.dto.ConhecimentoMapper;
import sgc.conhecimento.modelo.ConhecimentoRepo;

import java.net.URI;
import java.util.List;

/**
 * Controlador REST para gerenciar Conhecimentos usando DTOs.
 * Evita expor entidades JPA diretamente nas APIs.
 */
@RestController
@RequestMapping("/api/conhecimentos")
@RequiredArgsConstructor
public class ConhecimentoControle {
    private final ConhecimentoRepo conhecimentoRepo;
    private final ConhecimentoMapper conhecimentoMapper;

    @GetMapping
    public List<ConhecimentoDto> listarConhecimentos() {
        return conhecimentoRepo.findAll()
                .stream()
                .map(conhecimentoMapper::toDTO)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConhecimentoDto> obterConhecimento(@PathVariable Long id) {
        return conhecimentoRepo.findById(id)
                .map(conhecimentoMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ConhecimentoDto> criarConhecimento(@Valid @RequestBody ConhecimentoDto conhecimentoDto) {
        var entity = conhecimentoMapper.toEntity(conhecimentoDto);
        var salvo = conhecimentoRepo.save(entity);
        URI uri = URI.create("/api/conhecimentos/%d".formatted(salvo.getCodigo()));
        return ResponseEntity.created(uri).body(conhecimentoMapper.toDTO(salvo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConhecimentoDto> atualizarConhecimento(@PathVariable Long id,
                                                                 @Valid @RequestBody ConhecimentoDto conhecimentoDto) {
        return conhecimentoRepo.findById(id)
                .map(existing -> {
                    var entityToUpdate = conhecimentoMapper.toEntity(conhecimentoDto);
                    existing.setDescricao(entityToUpdate.getDescricao());
                    existing.setAtividade(entityToUpdate.getAtividade());

                    var atualizado = conhecimentoRepo.save(existing);
                    return ResponseEntity.ok(conhecimentoMapper.toDTO(atualizado));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirConhecimento(@PathVariable Long id) {
        return conhecimentoRepo.findById(id)
                .map(x -> {
                    conhecimentoRepo.deleteById(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}