package sgc.atividade;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sgc.atividade.dto.AtividadeDto;
import sgc.atividade.dto.AtividadeMapper;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.conhecimento.modelo.ConhecimentoRepo;
import sgc.subprocesso.modelo.SubprocessoRepo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import sgc.comum.modelo.UsuarioRepo;

import java.net.URI;
import java.util.List;

/**
 * Controlador REST para gerenciar Atividades usando DTOs.
 * Evita expor entidades JPA diretamente nas APIs.
 */
@RestController
@RequestMapping("/api/atividades")
@RequiredArgsConstructor
public class AtividadeControle {
    private final AtividadeRepo atividadeRepo;
    private final AtividadeMapper atividadeMapper;
    private final ConhecimentoRepo conhecimentoRepo;
    private final SubprocessoRepo subprocessoRepo;
    private final UsuarioRepo usuarioRepo;

    @GetMapping
    public List<AtividadeDto> listar() {
        return atividadeRepo.findAll()
                .stream()
                .map(atividadeMapper::toDTO)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AtividadeDto> obterPorId(@PathVariable Long id) {
        return atividadeRepo.findById(id)
                .map(atividadeMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AtividadeDto> criar(@Valid @RequestBody AtividadeDto atividadeDto, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(403).build();
        }

        var subprocessoOptional = subprocessoRepo.findByMapaCodigo(atividadeDto.mapaCodigo());
        if (subprocessoOptional.isEmpty()) {
            return ResponseEntity.unprocessableEntity().build();
        }
        var subprocesso = subprocessoOptional.get();

        var usuarioOptional = usuarioRepo.findByTitulo(userDetails.getUsername());
        if (usuarioOptional.isEmpty() || !usuarioOptional.get().equals(subprocesso.getUnidade().getTitular())) {
            return ResponseEntity.status(403).build();
        }

        if (subprocesso.getSituacao().isFinalizado()) {
            return ResponseEntity.unprocessableEntity().build();
        }

        var entidade = atividadeMapper.toEntity(atividadeDto);
        var salvo = atividadeRepo.save(entidade);
        URI uri = URI.create("/api/atividades/%d".formatted(salvo.getCodigo()));
        return ResponseEntity.created(uri).body(atividadeMapper.toDTO(salvo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AtividadeDto> atualizar(@PathVariable Long id,
                                                  @Valid @RequestBody AtividadeDto atividadeDto) {
        return atividadeRepo.findById(id)
                .map(existente -> {
                    // Mapeia os campos do DTO para a entidade existente
                    var entidadeParaAtualizar = atividadeMapper.toEntity(atividadeDto);
                    existente.setDescricao(entidadeParaAtualizar.getDescricao());
                    existente.setMapa(entidadeParaAtualizar.getMapa());

                    var atualizado = atividadeRepo.save(existente);
                    return ResponseEntity.ok(atividadeMapper.toDTO(atualizado));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        return atividadeRepo.findById(id)
                .map(atividade -> {
                    var conhecimentos = conhecimentoRepo.findByAtividadeCodigo(atividade.getCodigo());
                    conhecimentoRepo.deleteAll(conhecimentos);
                    atividadeRepo.delete(atividade);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}