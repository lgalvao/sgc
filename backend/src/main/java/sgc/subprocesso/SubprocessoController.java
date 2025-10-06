package sgc.subprocesso;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sgc.comum.erros.ErroDominioAccessoNegado;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.mapa.Mapa;
import sgc.processo.Processo;
import sgc.unidade.Unidade;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gerenciar Subprocessos usando DTOs.
 * Evita expor entidades JPA diretamente nas APIs.
 */
@RestController
@RequestMapping("/api/subprocessos")
@RequiredArgsConstructor
public class SubprocessoController {
    private final SubprocessoRepository subprocessoRepository;
    private final SubprocessoService subprocessoService;

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
            SubprocessoDetalheDTO detail = subprocessoService.obterDetalhes(id, perfil, unidadeUsuario);
            return ResponseEntity.ok(detail);
        } catch (ErroDominioNaoEncontrado e) {
            return ResponseEntity.notFound().build();
        } catch (ErroDominioAccessoNegado e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro interno");
        }
    }

    /**
     * POST /api/subprocessos/{id}/disponibilizar-cadastro
     */
    @PostMapping("/{id}/disponibilizar-cadastro")
    public ResponseEntity<?> disponibilizarCadastro(@PathVariable Long id) {
        try {
            // validar atividades sem conhecimento
            var faltando = subprocessoService.obterAtividadesSemConhecimento(id);
            if (faltando != null && !faltando.isEmpty()) {
                var lista = faltando.stream()
                        .map(a -> Map.of("id", a.getCodigo(), "descricao", a.getDescricao()))
                        .toList();
                return ResponseEntity.badRequest().body(Map.of("atividadesSemConhecimento", lista));
            }

            subprocessoService.disponibilizarCadastroAcao(id);
            return ResponseEntity.ok("Cadastro de atividades disponibilizado");
        } catch (ErroDominioNaoEncontrado e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro interno");
        }
    }

    /**
     * POST /api/subprocessos/{id}/disponibilizar-revisao
     */
    @PostMapping("/{id}/disponibilizar-revisao")
    public ResponseEntity<?> disponibilizarRevisao(@PathVariable Long id) {
        try {
            var faltando = subprocessoService.obterAtividadesSemConhecimento(id);
            if (faltando != null && !faltando.isEmpty()) {
                var lista = faltando.stream()
                        .map(a -> Map.of("id", a.getCodigo(), "descricao", a.getDescricao()))
                        .toList();
                return ResponseEntity.badRequest().body(Map.of("atividadesSemConhecimento", lista));
            }

            subprocessoService.disponibilizarRevisaoAcao(id);
            return ResponseEntity.ok("Revisão do cadastro de atividades disponibilizada");
        } catch (ErroDominioNaoEncontrado e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro interno");
        }
    }

    /**
     * GET /api/subprocessos/{id}/cadastro
     * Retorna agregação das atividades e conhecimentos vinculados ao mapa/subprocesso.
     */
    @GetMapping("/{id}/cadastro")
    public ResponseEntity<?> obterCadastro(@PathVariable Long id) {
        try {
            var payload = subprocessoService.obterCadastro(id);
            return ResponseEntity.ok(payload);
        } catch (ErroDominioNaoEncontrado e) {
            return ResponseEntity.notFound().build();
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
                .map(subprocesso -> {
                    if (subprocessoDto.getProcessoCodigo() != null) {
                        Processo p = new Processo();
                        p.setCodigo(subprocessoDto.getProcessoCodigo());
                        subprocesso.setProcesso(p);
                    } else {
                        subprocesso.setProcesso(null);
                    }

                    if (subprocessoDto.getUnidadeCodigo() != null) {
                        Unidade u = new Unidade();
                        u.setCodigo(subprocessoDto.getUnidadeCodigo());
                        subprocesso.setUnidade(u);
                    } else {
                        subprocesso.setUnidade(null);
                    }

                    if (subprocessoDto.getMapaCodigo() != null) {
                        Mapa m = new Mapa();
                        m.setCodigo(subprocessoDto.getMapaCodigo());
                        subprocesso.setMapa(m);
                    } else {
                        subprocesso.setMapa(null);
                    }

                    subprocesso.setDataLimiteEtapa1(subprocessoDto.getDataLimiteEtapa1());
                    subprocesso.setDataFimEtapa1(subprocessoDto.getDataFimEtapa1());
                    subprocesso.setDataLimiteEtapa2(subprocessoDto.getDataLimiteEtapa2());
                    subprocesso.setDataFimEtapa2(subprocessoDto.getDataFimEtapa2());
                    subprocesso.setSituacaoId(subprocessoDto.getSituacaoId());
                    var atualizado = subprocessoRepository.save(subprocesso);
                    return ResponseEntity.ok(SubprocessoMapper.toDTO(atualizado));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirSubprocesso(@PathVariable Long id) {
        return subprocessoRepository.findById(id)
                .map(_ -> {
                    subprocessoRepository.deleteById(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}