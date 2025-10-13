package sgc.atividade;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import sgc.atividade.dto.AtividadeDto;
import sgc.atividade.dto.AtividadeMapper;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.conhecimento.modelo.ConhecimentoRepo;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.conhecimento.dto.ConhecimentoDto;
import sgc.conhecimento.dto.ConhecimentoMapper;
import sgc.sgrh.UsuarioRepo;
import sgc.subprocesso.modelo.SubprocessoRepo;

import java.net.URI;
import java.util.List;

/**
 * Controlador REST para gerenciar Atividades e seus Conhecimentos associados.
 */
@RestController
@RequestMapping("/api/atividades")
@RequiredArgsConstructor
public class AtividadeControle {
    private final AtividadeRepo atividadeRepo;
    private final AtividadeMapper atividadeMapper;
    private final ConhecimentoRepo conhecimentoRepo;
    private final ConhecimentoMapper conhecimentoMapper;
    private final SubprocessoRepo subprocessoRepo;
    private final UsuarioRepo usuarioRepo;

    // Endpoints para Atividades

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
        var subprocesso = subprocessoRepo.findByMapaCodigo(atividadeDto.mapaCodigo())
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado para o mapa com código " + atividadeDto.mapaCodigo()));

        var usuario = usuarioRepo.findByTitulo(userDetails.getUsername())
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Usuário não encontrado: " + userDetails.getUsername()));

        if (!usuario.equals(subprocesso.getUnidade().getTitular())) {
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
    public ResponseEntity<AtividadeDto> atualizar(@PathVariable Long id, @Valid @RequestBody AtividadeDto atividadeDto) {
        return atividadeRepo.findById(id)
            .map(existente -> {
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

    // Endpoints para Conhecimentos aninhados em Atividades

    @GetMapping("/{atividadeId}/conhecimentos")
    public List<ConhecimentoDto> listarConhecimentos(@PathVariable Long atividadeId) {
        return conhecimentoRepo.findByAtividadeCodigo(atividadeId)
            .stream()
            .map(conhecimentoMapper::toDTO)
            .toList();
    }

    @PostMapping("/{atividadeId}/conhecimentos")
    public ResponseEntity<ConhecimentoDto> criarConhecimento(@PathVariable Long atividadeId, @Valid @RequestBody ConhecimentoDto conhecimentoDto) {
        return atividadeRepo.findById(atividadeId)
            .map(atividade -> {
                var conhecimento = conhecimentoMapper.toEntity(conhecimentoDto);
                conhecimento.setAtividade(atividade); // Garante a associação correta
                var salvo = conhecimentoRepo.save(conhecimento);
                URI uri = URI.create("/api/atividades/%d/conhecimentos/%d".formatted(atividadeId, salvo.getCodigo()));
                return ResponseEntity.created(uri).body(conhecimentoMapper.toDTO(salvo));
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{atividadeId}/conhecimentos/{conhecimentoId}")
    public ResponseEntity<ConhecimentoDto> atualizarConhecimento(@PathVariable Long atividadeId, @PathVariable Long conhecimentoId, @Valid @RequestBody ConhecimentoDto conhecimentoDto) {
        return conhecimentoRepo.findById(conhecimentoId)
            .filter(conhecimento -> conhecimento.getAtividade().getCodigo().equals(atividadeId))
            .map(existente -> {
                var paraAtualizar = conhecimentoMapper.toEntity(conhecimentoDto);
                existente.setDescricao(paraAtualizar.getDescricao());
                var atualizado = conhecimentoRepo.save(existente);
                return ResponseEntity.ok(conhecimentoMapper.toDTO(atualizado));
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{atividadeId}/conhecimentos/{conhecimentoId}")
    public ResponseEntity<Void> excluirConhecimento(@PathVariable Long atividadeId, @PathVariable Long conhecimentoId) {
        return conhecimentoRepo.findById(conhecimentoId)
            .filter(conhecimento -> conhecimento.getAtividade().getCodigo().equals(atividadeId))
            .map(conhecimento -> {
                conhecimentoRepo.delete(conhecimento);
                return ResponseEntity.noContent().<Void>build();
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}