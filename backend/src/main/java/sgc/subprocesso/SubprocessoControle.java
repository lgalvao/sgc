package sgc.subprocesso;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import sgc.comum.dtos.RespostaDto;
import sgc.comum.erros.ErroDominioAccessoNegado;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.modelo.Usuario;
import sgc.mapa.ImpactoMapaService;
import sgc.mapa.MapaService;
import sgc.mapa.dto.ImpactoMapaDto;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.modelo.Mapa;
import sgc.processo.modelo.Processo;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.modelo.ErroSubprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;

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
public class SubprocessoControle {
    private final SubprocessoRepo subprocessoRepo;
    private final SubprocessoService subprocessoService;
    private final MapaService mapaService;
    private final ImpactoMapaService impactoMapaService;
    private final SubprocessoMapper subprocessoMapper;

    @GetMapping
    public List<SubprocessoDto> listar() {
        return subprocessoRepo.findAll()
                .stream()
                .map(subprocessoMapper::toDTO)
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
    public ResponseEntity<?> obterPorId(@PathVariable Long id,
                                              @RequestParam(required = false) String perfil,
                                              @RequestParam(required = false) Long unidadeUsuario) {
        try {
            SubprocessoDetalheDto detail = subprocessoService.obterDetalhes(id, perfil, unidadeUsuario);
            return ResponseEntity.ok(detail);
        } catch (ErroEntidadeNaoEncontrada e) {
            return ResponseEntity.notFound().build();
        } catch (ErroDominioAccessoNegado e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro interno");
        }
    }

    /**
     * CDU-13 Item 7 - Visualizar histórico de análise de cadastro
     * GET /api/subprocessos/{id}/historico-cadastro
     */
    @GetMapping("/{id}/historico-cadastro")
    public ResponseEntity<?> obterHistoricoCadastro(@PathVariable Long id) {
        try {
            var historico = subprocessoService.getHistoricoAnaliseCadastro(id);
            return ResponseEntity.ok(historico);
        } catch (ErroDominioNaoEncontrado e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erro interno"));
        }
    }

    @PostMapping("/{id}/disponibilizar")
    public ResponseEntity<RespostaDto> disponibilizarCadastro(
        @PathVariable("id") Long subprocessoId,
        @AuthenticationPrincipal Usuario usuario
    ) {
        subprocessoService.disponibilizarCadastro(subprocessoId, usuario);
        return ResponseEntity.ok(new RespostaDto("Cadastro de atividades disponibilizado"));
    }

    /**
     * POST /api/subprocessos/{id}/disponibilizar-revisao
     */
    @PostMapping("/{id}/disponibilizar-revisao")
    public ResponseEntity<?> disponibilizarRevisao(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        try {
            var faltando = subprocessoService.obterAtividadesSemConhecimento(id);
            if (faltando != null && !faltando.isEmpty()) {
                var lista = faltando.stream()
                        .map(a -> Map.of("id", a.getCodigo(), "descricao", a.getDescricao()))
                        .toList();
                return ResponseEntity.badRequest().body(Map.of("atividadesSemConhecimento", lista));
            }

            subprocessoService.disponibilizarRevisao(id, usuario);
            return ResponseEntity.ok(new RespostaDto("Revisão do cadastro de atividades disponibilizada"));
        } catch (ErroEntidadeNaoEncontrada e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (ErroDominioAccessoNegado e) {
            return ResponseEntity.status(403).body(new RespostaDto(e.getMessage()));
        }
        catch (Exception e) {
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
    public ResponseEntity<SubprocessoDto> criar(@Valid @RequestBody SubprocessoDto subprocessoDto) {
        var entity = subprocessoMapper.toEntity(subprocessoDto);
        var salvo = subprocessoRepo.save(entity);

        URI uri = URI.create("/api/subprocessos/%d".formatted(salvo.getCodigo()));
        return ResponseEntity.created(uri).body(subprocessoMapper.toDTO(salvo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubprocessoDto> atualizar(@PathVariable Long id, @Valid @RequestBody SubprocessoDto subprocessoDto) {
        return subprocessoRepo.findById(id)
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
                    subprocesso.setSituacao(subprocessoDto.getSituacao());
                    var atualizado = subprocessoRepo.save(subprocesso);
                    return ResponseEntity.ok(subprocessoMapper.toDTO(atualizado));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * POST /api/subprocessos/{id}/devolver-cadastro
     * CDU-13 item 9 - Devolver cadastro para ajustes
     */
    @PostMapping("/{id}/devolver-cadastro")
    public ResponseEntity<?> devolverCadastro(
            @PathVariable Long id,
            @Valid @RequestBody DevolverCadastroReq request,
            @AuthenticationPrincipal Usuario usuario) {
        try {
            SubprocessoDto resultado = subprocessoService.devolverCadastro(
                id,
                request.motivo(),
                request.observacoes(),
                usuario.getTitulo()
            );
            return ResponseEntity.ok(resultado);
        } catch (ErroEntidadeNaoEncontrada e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erro interno"));
        }
    }

    /**
     * POST /api/subprocessos/{id}/aceitar-cadastro
     * CDU-13 item 10 - Aceitar cadastro (GESTOR)
     */
    @PostMapping("/{id}/aceitar-cadastro")
    public ResponseEntity<?> aceitarCadastro(
            @PathVariable Long id,
            @Valid @RequestBody AceitarCadastroReq request,
            @AuthenticationPrincipal Usuario usuario) {
        try {
            SubprocessoDto resultado = subprocessoService.aceitarCadastro(
                id,
                request.observacoes(),
                usuario.getTitulo()
            );
            return ResponseEntity.ok(resultado);
        } catch (ErroEntidadeNaoEncontrada e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erro interno"));
        }
    }

    /**
     * POST /api/subprocessos/{id}/homologar-cadastro
     * CDU-13 item 11 - Homologar cadastro (ADMIN)
     */
    @PostMapping("/{id}/homologar-cadastro")
    public ResponseEntity<?> homologarCadastro(
            @PathVariable Long id,
            @Valid @RequestBody HomologarCadastroReq request,
            @AuthenticationPrincipal Usuario usuario) {
        try {
            SubprocessoDto resultado = subprocessoService.homologarCadastro(
                id,
                request.observacoes(),
                usuario.getTitulo()
            );
            return ResponseEntity.ok(resultado);
        } catch (ErroEntidadeNaoEncontrada e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erro interno"));
        }
    }

    /**
     * POST /api/subprocessos/{id}/devolver-revisao-cadastro
     * CDU-14 item 10 - Devolver revisão de cadastro para ajustes
     */
    @PostMapping("/{id}/devolver-revisao-cadastro")
    public ResponseEntity<?> devolverRevisaoCadastro(
            @PathVariable Long id,
            @Valid @RequestBody DevolverCadastroReq request) {
        try {
            SubprocessoDto resultado = subprocessoService.devolverRevisaoCadastro(
                id,
                request.motivo(),
                request.observacoes(),
                "USUARIO_ATUAL" // TODO: extrair do token JWT
            );
            return ResponseEntity.ok(resultado);
        } catch (ErroEntidadeNaoEncontrada e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erro interno"));
        }
    }

    /**
     * POST /api/subprocessos/{id}/aceitar-revisao-cadastro
     * CDU-14 item 11 - Aceitar revisão de cadastro (GESTOR)
     */
    @PostMapping("/{id}/aceitar-revisao-cadastro")
    public ResponseEntity<?> aceitarRevisaoCadastro(
            @PathVariable Long id,
            @Valid @RequestBody AceitarCadastroReq request) {
        try {
            SubprocessoDto resultado = subprocessoService.aceitarRevisaoCadastro(
                id,
                request.observacoes(),
                "USUARIO_ATUAL" // TODO: extrair do token JWT
            );
            return ResponseEntity.ok(resultado);
        } catch (ErroEntidadeNaoEncontrada e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erro interno"));
        }
    }

    /**
     * POST /api/subprocessos/{id}/homologar-revisao-cadastro
     * CDU-14 item 12 - Homologar revisão de cadastro (ADMIN)
     */
    @PostMapping("/{id}/homologar-revisao-cadastro")
    public ResponseEntity<?> homologarRevisaoCadastro(
            @PathVariable Long id,
            @Valid @RequestBody HomologarCadastroReq request) {
        try {
            SubprocessoDto resultado = subprocessoService.homologarRevisaoCadastro(
                id,
                request.observacoes(),
                "USUARIO_ATUAL" // TODO: extrair do token JWT
            );
            return ResponseEntity.ok(resultado);
        } catch (ErroEntidadeNaoEncontrada e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erro interno"));
        }
    }

    /**
     * CDU-12 - Verificar impactos no mapa de competências
     * GET /api/subprocessos/{id}/impactos-mapa
     */
    @GetMapping("/{id}/impactos-mapa")
    public ResponseEntity<?> verificarImpactos(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        try {
            ImpactoMapaDto impactos = impactoMapaService.verificarImpactos(id, usuario);
            return ResponseEntity.ok(impactos);
        } catch (ErroEntidadeNaoEncontrada e) {
            return ResponseEntity.notFound().build();
        } catch (ErroDominioAccessoNegado e) {
            return ResponseEntity.status(403).body(new RespostaDto(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erro interno"));
        }
    }
    
    /**
     * CDU-15 - Obter mapa de competências de um subprocesso.
     * GET /api/subprocessos/{id}/mapa
     */
    @GetMapping("/{id}/mapa")
    public ResponseEntity<?> obterMapa(@PathVariable Long id) {
        try {
            MapaCompletoDto mapa = mapaService.obterMapaSubprocesso(id);
            return ResponseEntity.ok(mapa);
        } catch (ErroEntidadeNaoEncontrada e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erro interno"));
        }
    }
    
    /**
     * CDU-15 - Criar/Editar mapa de competências de um subprocesso.
     * PUT /api/subprocessos/{id}/mapa
     */
    @PutMapping("/{id}/mapa")
    @Transactional
    public ResponseEntity<?> salvarMapa(
        @PathVariable Long id,
        @RequestBody @Valid SalvarMapaRequest request
    ) {
        try {
            String usuarioTitulo = "USUARIO_ATUAL";
            MapaCompletoDto mapa = mapaService.salvarMapaSubprocesso(id, request, usuarioTitulo);
            return ResponseEntity.ok(mapa);
        } catch (ErroEntidadeNaoEncontrada e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erro interno"));
        }
    }
    
    /**
     * CDU-17 - Disponibilizar mapa de competências para validação
     * POST /api/subprocessos/{id}/disponibilizar-mapa
     */
    @PostMapping("/{id}/disponibilizar-mapa")
    @Transactional
    public ResponseEntity<?> disponibilizarMapa(
            @PathVariable Long id,
            @RequestBody @Valid DisponibilizarMapaReq request) {
        try {
            String usuarioTitulo = "USUARIO_ATUAL";
            
            SubprocessoDto subprocesso = subprocessoService.disponibilizarMapa(
                id,
                request.observacoes(),
                request.dataLimiteEtapa2(),
                usuarioTitulo
            );
            return ResponseEntity.ok(subprocesso);
        } catch (ErroEntidadeNaoEncontrada e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erro interno"));
        }
    }
    
    /**
     * CDU-19 item 8 - Apresentar sugestões ao mapa
     * POST /api/subprocessos/{id}/apresentar-sugestoes
     */
    @PostMapping("/{id}/apresentar-sugestoes")
    @Transactional
    public ResponseEntity<?> apresentarSugestoes(
            @PathVariable Long id,
            @RequestBody @Valid ApresentarSugestoesReq request) {
        try {
            String usuarioTitulo = "USUARIO_ATUAL";
            
            SubprocessoDto subprocesso = subprocessoService.apresentarSugestoes(
                id, 
                request.sugestoes(),
                usuarioTitulo
            );
            return ResponseEntity.ok(subprocesso);
        } catch (ErroEntidadeNaoEncontrada e) {
            return ResponseEntity.notFound().build();
        } catch (ErroSubprocesso | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erro interno"));
        }
    }
    
    /**
     * CDU-19 item 9 - Validar mapa (sem sugestões)
     * POST /api/subprocessos/{id}/validar-mapa
     */
    @PostMapping("/{id}/validar-mapa")
    @Transactional
    public ResponseEntity<?> validarMapa(@PathVariable Long id) {
        try {
            String usuarioTitulo = "USUARIO_ATUAL";

            SubprocessoDto subprocesso = subprocessoService.validarMapa(id, usuarioTitulo);
            return ResponseEntity.ok(subprocesso);
        } catch (ErroEntidadeNaoEncontrada e) {
            return ResponseEntity.notFound().build();
        } catch (ErroSubprocesso | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erro interno"));
        }
    }

    /**
     * CDU-20 item 5 - Visualizar sugestões apresentadas
     * GET /api/subprocessos/{id}/sugestoes
     */
    @GetMapping("/{id}/sugestoes")
    public ResponseEntity<SugestoesDto> obterSugestoes(@PathVariable Long id) {
        SugestoesDto sugestoes = subprocessoService.obterSugestoes(id);
        return ResponseEntity.ok(sugestoes);
    }

    /**
     * CDU-20 item 6 - Visualizar histórico de análise
     * GET /api/subprocessos/{id}/historico-validacao
     */
    @GetMapping("/{id}/historico-validacao")
    public ResponseEntity<List<AnaliseValidacaoDto>> obterHistoricoValidacao(@PathVariable Long id) {
        List<AnaliseValidacaoDto> historico = subprocessoService.obterHistoricoValidacao(id);
        return ResponseEntity.ok(historico);
    }

    /**
     * CDU-20 item 7 - Devolver validação (GESTOR)
     * POST /api/subprocessos/{id}/devolver-validacao
     */
    @PostMapping("/{id}/devolver-validacao")
    @Transactional
    public ResponseEntity<SubprocessoDto> devolverValidacao(
        @PathVariable Long id,
        @RequestBody @Valid DevolverValidacaoReq request
    ) {
        SubprocessoDto subprocesso = subprocessoService.devolverValidacao(
            id,
            request.justificativa(),
            "USUARIO_ATUAL" // TODO: extrair do token JWT
        );
        return ResponseEntity.ok(subprocesso);
    }

    /**
     * CDU-20 item 8 - Aceitar validação (GESTOR)
     * POST /api/subprocessos/{id}/aceitar-validacao
     */
    @PostMapping("/{id}/aceitar-validacao")
    @Transactional
    public ResponseEntity<SubprocessoDto> aceitarValidacao(@PathVariable Long id) {
        SubprocessoDto subprocesso = subprocessoService.aceitarValidacao(id, "USUARIO_ATUAL");
        return ResponseEntity.ok(subprocesso);
    }

    /**
     * CDU-20 item 9 - Homologar validação (ADMIN)
     * POST /api/subprocessos/{id}/homologar-validacao
     */
    @PostMapping("/{id}/homologar-validacao")
    @Transactional
    public ResponseEntity<SubprocessoDto> homologarValidacao(@PathVariable Long id) {
        SubprocessoDto subprocesso = subprocessoService.homologarValidacao(id, "USUARIO_ATUAL");
        return ResponseEntity.ok(subprocesso);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        return subprocessoRepo.findById(id)
                .map(_ -> {
                    subprocessoRepo.deleteById(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * CDU-16 item 4 - Visualizar mapa para ajuste
     * GET /api/subprocessos/{id}/mapa-ajuste
     */
    @GetMapping("/{id}/mapa-ajuste")
    public ResponseEntity<MapaAjusteDto> obterMapaParaAjuste(@PathVariable Long id) {
        MapaAjusteDto mapa = subprocessoService.obterMapaParaAjuste(id);
        return ResponseEntity.ok(mapa);
    }

    /**
     * CDU-16 item 5 - Salvar ajustes no mapa
     * PUT /api/subprocessos/{id}/mapa-ajuste
     */
    @PutMapping("/{id}/mapa-ajuste")
    @Transactional
    public ResponseEntity<SubprocessoDto> salvarAjustesMapa(
        @PathVariable Long id,
        @RequestBody @Valid SalvarAjustesReq request,
        @AuthenticationPrincipal String usuarioTitulo
    ) {
        SubprocessoDto subprocesso = subprocessoService.salvarAjustesMapa(
            id,
            request.competencias(),
            usuarioTitulo
        );
        return ResponseEntity.ok(subprocesso);
    }

    /**
     * CDU-16 item 6 - Submeter mapa ajustado
     * POST /api/subprocessos/{id}/submeter-mapa-ajustado
     */
    @PostMapping("/{id}/submeter-mapa-ajustado")
    @Transactional
    public ResponseEntity<SubprocessoDto> submeterMapaAjustado(
        @PathVariable Long id,
        @AuthenticationPrincipal String usuarioTitulo
    ) {
        SubprocessoDto subprocesso = subprocessoService.submeterMapaAjustado(id, usuarioTitulo);
        return ResponseEntity.ok(subprocesso);
    }

    /**
     * CDU-08 - Importar atividades de outro subprocesso
     * POST /api/subprocessos/{id}/importar-atividades
     */
    @PostMapping("/{id}/importar-atividades")
    @Transactional
    public ResponseEntity<?> importarAtividades(
        @PathVariable Long id,
        @RequestBody @Valid ImportarAtividadesRequest request
    ) {
        try {
            subprocessoService.importarAtividades(id, request.subprocessoOrigemId());
            return ResponseEntity.ok(Map.of("message", "Atividades importadas com sucesso."));
        } catch (ErroEntidadeNaoEncontrada e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erro interno ao importar atividades."));
        }
    }
}