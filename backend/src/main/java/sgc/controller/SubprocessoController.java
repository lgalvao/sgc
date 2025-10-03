package sgc.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sgc.dto.SubprocessoDTO;
import sgc.mapper.SubprocessoMapper;
import sgc.repository.SubprocessoRepository;

import java.net.URI;
import java.util.List;

/**
 * Controlador REST para gerenciar Subprocessos usando DTOs.
 * Evita expor entidades JPA diretamente nas APIs.
 */
@RestController
@RequestMapping("/api/subprocessos")
@RequiredArgsConstructor
public class SubprocessoController {
    private final SubprocessoRepository subprocessoRepository;
    private final sgc.service.SubprocessoService subprocessoService;
 
    @GetMapping
    public List<SubprocessoDTO> listarSubprocessos() {
        return subprocessoRepository.findAll()
                .stream()
                .map(SubprocessoMapper::toDTO)
                .toList();
    }
 
    /**
     * GET /api/subprocessos/{id}
     * <p>
     * Retorna detalhes do subprocesso conforme CDU-07. Para fins de testes aceitamos
     * parâmetros opcionais ?perfil=...&unidadeUsuario=... que em produção devem ser
     * extraídos do token.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obterSubprocesso(@PathVariable Long id,
                                              @RequestParam(required = false) String perfil,
                                              @RequestParam(required = false) Long unidadeUsuario) {
        try {
            sgc.dto.SubprocessoDetailDTO detail = subprocessoService.getDetails(id, perfil, unidadeUsuario);
            return ResponseEntity.ok(detail);
        } catch (sgc.exception.DomainNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (sgc.exception.DomainAccessDeniedException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro interno");
        }
    }

    @PostMapping
    public ResponseEntity<SubprocessoDTO> criarSubprocesso(@Valid @RequestBody SubprocessoDTO subprocessoDto) {
        var entity = SubprocessoMapper.toEntity(subprocessoDto);
        var salvo = subprocessoRepository.save(entity);

        URI uri = URI.create("/api/subprocessos/%d".formatted(salvo.getCodigo()));
        return ResponseEntity.created(uri).body(SubprocessoMapper.toDTO(salvo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubprocessoDTO> atualizarSubprocesso(@PathVariable Long id, @Valid @RequestBody SubprocessoDTO subprocessoDto) {
        return subprocessoRepository.findById(id)
                .map(existing -> {
                    if (subprocessoDto.getProcessoCodigo() != null) {
                        sgc.model.Processo p = new sgc.model.Processo();
                        p.setCodigo(subprocessoDto.getProcessoCodigo());
                        existing.setProcesso(p);
                    } else {
                        existing.setProcesso(null);
                    }

                    if (subprocessoDto.getUnidadeCodigo() != null) {
                        sgc.model.Unidade u = new sgc.model.Unidade();
                        u.setCodigo(subprocessoDto.getUnidadeCodigo());
                        existing.setUnidade(u);
                    } else {
                        existing.setUnidade(null);
                    }

                    if (subprocessoDto.getMapaCodigo() != null) {
                        sgc.model.Mapa m = new sgc.model.Mapa();
                        m.setCodigo(subprocessoDto.getMapaCodigo());
                        existing.setMapa(m);
                    } else {
                        existing.setMapa(null);
                    }

                    existing.setDataLimiteEtapa1(subprocessoDto.getDataLimiteEtapa1());
                    existing.setDataFimEtapa1(subprocessoDto.getDataFimEtapa1());
                    existing.setDataLimiteEtapa2(subprocessoDto.getDataLimiteEtapa2());
                    existing.setDataFimEtapa2(subprocessoDto.getDataFimEtapa2());
                    existing.setSituacaoId(subprocessoDto.getSituacaoId());
                    var atualizado = subprocessoRepository.save(existing);
                    return ResponseEntity.ok(SubprocessoMapper.toDTO(atualizado));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirSubprocesso(@PathVariable Long id) {
        return subprocessoRepository.findById(id)
                .map(existing -> {
                    subprocessoRepository.deleteById(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}