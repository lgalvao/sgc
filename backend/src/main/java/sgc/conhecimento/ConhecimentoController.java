package sgc.conhecimento;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Controlador REST para gerenciar Conhecimentos usando DTOs.
 * Evita expor entidades JPA diretamente nas APIs.
 */
@RestController
@RequestMapping("/api/conhecimentos")
@RequiredArgsConstructor
public class ConhecimentoController {
    private final ConhecimentoRepository conhecimentoRepository;
    private final ConhecimentoMapper conhecimentoMapper;

    @GetMapping
    public List<ConhecimentoDTO> listarConhecimentos() {
        return conhecimentoRepository.findAll()
                .stream()
                .map(conhecimentoMapper::toDTO)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConhecimentoDTO> obterConhecimento(@PathVariable Long id) {
        return conhecimentoRepository.findById(id)
                .map(conhecimentoMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ConhecimentoDTO> criarConhecimento(@Valid @RequestBody ConhecimentoDTO conhecimentoDto) {
        var entity = conhecimentoMapper.toEntity(conhecimentoDto);
        var salvo = conhecimentoRepository.save(entity);
        URI uri = URI.create("/api/conhecimentos/%d".formatted(salvo.getCodigo()));
        return ResponseEntity.created(uri).body(conhecimentoMapper.toDTO(salvo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConhecimentoDTO> atualizarConhecimento(@PathVariable Long id,
            @Valid @RequestBody ConhecimentoDTO conhecimentoDto) {
        return conhecimentoRepository.findById(id)
                .map(existing -> {
                    var entityToUpdate = conhecimentoMapper.toEntity(conhecimentoDto);
                    existing.setDescricao(entityToUpdate.getDescricao());
                    existing.setAtividade(entityToUpdate.getAtividade());

                    var atualizado = conhecimentoRepository.save(existing);
                    return ResponseEntity.ok(conhecimentoMapper.toDTO(atualizado));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirConhecimento(@PathVariable Long id) {
        return conhecimentoRepository.findById(id)
                .map(_ -> {
                    conhecimentoRepository.deleteById(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}