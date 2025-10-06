package sgc.conhecimento;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sgc.atividade.Atividade;

import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para gerenciar Conhecimentos usando DTOs.
 * Evita expor entidades JPA diretamente nas APIs.
 */
@RestController
@RequestMapping("/api/conhecimentos")
@RequiredArgsConstructor
public class ConhecimentoController {
    private final ConhecimentoRepository conhecimentoRepository;

    @GetMapping
    public List<ConhecimentoDTO> listarConhecimentos() {
        return conhecimentoRepository.findAll()
                .stream()
                .map(ConhecimentoMapper::toDTO)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConhecimentoDTO> obterConhecimento(@PathVariable Long id) {
        Optional<Conhecimento> c = conhecimentoRepository.findById(id);
        return c.map(ConhecimentoMapper::toDTO).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ConhecimentoDTO> criarConhecimento(@Valid @RequestBody ConhecimentoDTO conhecimentoDto) {
        var entity = ConhecimentoMapper.toEntity(conhecimentoDto);
        var salvo = conhecimentoRepository.save(entity);
        URI uri = URI.create("/api/conhecimentos/%d".formatted(salvo.getCodigo()));
        return ResponseEntity.created(uri).body(ConhecimentoMapper.toDTO(salvo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConhecimentoDTO> atualizarConhecimento(@PathVariable Long id, @Valid @RequestBody ConhecimentoDTO conhecimentoDto) {
        return conhecimentoRepository.findById(id)
                .map(existing -> {
                    if (conhecimentoDto.getAtividadeCodigo() != null) {
                        Atividade a = new Atividade();
                        a.setCodigo(conhecimentoDto.getAtividadeCodigo());
                        existing.setAtividade(a);
                    } else {
                        existing.setAtividade(null);
                    }
                    existing.setDescricao(conhecimentoDto.getDescricao());
                    var atualizado = conhecimentoRepository.save(existing);
                    return ResponseEntity.ok(ConhecimentoMapper.toDTO(atualizado));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirConhecimento(@PathVariable Long id) {
        return conhecimentoRepository.findById(id)
                .map(existing -> {
                    conhecimentoRepository.deleteById(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}