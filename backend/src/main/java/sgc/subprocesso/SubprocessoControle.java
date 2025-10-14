package sgc.subprocesso;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import sgc.analise.modelo.TipoAnalise;
import sgc.atividade.modelo.Atividade;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.ImpactoMapaService;
import sgc.mapa.MapaService;
import sgc.mapa.dto.ImpactoMapaDto;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.dto.visualizacao.MapaVisualizacaoDto;
import sgc.sgrh.Usuario;
import sgc.subprocesso.dto.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subprocessos")
@RequiredArgsConstructor
public class SubprocessoControle {
    private final SubprocessoService subprocessoService;
    private final MapaService mapaService;
    private final ImpactoMapaService impactoMapaService;
    private final sgc.analise.AnaliseService analiseService;
    private final sgc.analise.dto.AnaliseMapper analiseMapper;

    @GetMapping
    public List<SubprocessoDto> listar() {
        return subprocessoService.listar();
    }

    @GetMapping("/{id}")
    public SubprocessoDetalheDto obterPorId(@PathVariable Long id,
                                              @RequestParam(required = false) String perfil,
                                              @RequestParam(required = false) Long unidadeUsuario) {
        return subprocessoService.obterDetalhes(id, perfil, unidadeUsuario);
    }

    @GetMapping("/{id}/historico-cadastro")
    public List<sgc.analise.dto.AnaliseHistoricoDto> obterHistoricoCadastro(@PathVariable Long id) {
        return analiseService.listarPorSubprocesso(id, TipoAnalise.CADASTRO)
            .stream()
            .map(analiseMapper::toAnaliseHistoricoDto)
            .toList();
    }

    @PostMapping("/{id}/disponibilizar")
    public ResponseEntity<RespostaDto> disponibilizarCadastro(
        @PathVariable("id") Long subprocessoId,
        @AuthenticationPrincipal Usuario usuario
    ) {
        subprocessoService.disponibilizarCadastro(subprocessoId, usuario);
        return ResponseEntity.ok(new RespostaDto("Cadastro de atividades disponibilizado"));
    }

    @PostMapping("/{id}/disponibilizar-revisao")
    public ResponseEntity<RespostaDto> disponibilizarRevisao(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        List<Atividade> faltando = subprocessoService.obterAtividadesSemConhecimento(id);
        if (faltando != null && !faltando.isEmpty()) {
            var lista = faltando.stream()
                    .map(a -> Map.of("id", a.getCodigo(), "descricao", a.getDescricao()))
                    .toList();
            throw new ErroValidacao("Existem atividades sem conhecimentos associados.", Map.of("atividadesSemConhecimento", lista));
        }

        subprocessoService.disponibilizarRevisao(id, usuario);
        return ResponseEntity.ok(new RespostaDto("Revisão do cadastro de atividades disponibilizada"));
    }

    @GetMapping("/{id}/cadastro")
    public SubprocessoCadastroDto obterCadastro(@PathVariable Long id) {
        return subprocessoService.obterCadastro(id);
    }

    @PostMapping
    public ResponseEntity<SubprocessoDto> criar(@Valid @RequestBody SubprocessoDto subprocessoDto) {
        var salvo = subprocessoService.criar(subprocessoDto);
        URI uri = URI.create("/api/subprocessos/%d".formatted(salvo.getCodigo()));
        return ResponseEntity.created(uri).body(salvo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubprocessoDto> atualizar(@PathVariable Long id, @Valid @RequestBody SubprocessoDto subprocessoDto) {
        try {
            var atualizado = subprocessoService.atualizar(id, subprocessoDto);
            return ResponseEntity.ok(atualizado);
        } catch (ErroDominioNaoEncontrado e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/devolver-cadastro")
    public SubprocessoDto devolverCadastro(
            @PathVariable Long id,
            @Valid @RequestBody DevolverCadastroReq request,
            @AuthenticationPrincipal Usuario usuario) {
        return subprocessoService.devolverCadastro(
            id,
            request.motivo(),
            request.observacoes(),
            usuario
        );
    }

    @PostMapping("/{id}/aceitar-cadastro")
    public SubprocessoDto aceitarCadastro(
            @PathVariable Long id,
            @Valid @RequestBody AceitarCadastroReq request,
            @AuthenticationPrincipal Usuario usuario) {
        return subprocessoService.aceitarCadastro(
            id,
            request.observacoes(),
            usuario.getTitulo()
        );
    }

    @PostMapping("/{id}/homologar-cadastro")
    public SubprocessoDto homologarCadastro(
            @PathVariable Long id,
            @Valid @RequestBody HomologarCadastroReq request,
            @AuthenticationPrincipal Usuario usuario) {
        return subprocessoService.homologarCadastro(
            id,
            request.observacoes(),
            usuario.getTitulo()
        );
    }

    @PostMapping("/{id}/devolver-revisao-cadastro")
    public SubprocessoDto devolverRevisaoCadastro(
            @PathVariable Long id,
            @Valid @RequestBody DevolverCadastroReq request,
            @AuthenticationPrincipal Usuario usuario) {
        return subprocessoService.devolverRevisaoCadastro(
            id,
            request.motivo(),
            request.observacoes(),
            usuario
        );
    }

    @PostMapping("/{id}/aceitar-revisao-cadastro")
    public SubprocessoDto aceitarRevisaoCadastro(
            @PathVariable Long id,
            @Valid @RequestBody AceitarCadastroReq request,
            @AuthenticationPrincipal Usuario usuario) {
        return subprocessoService.aceitarRevisaoCadastro(
            id,
            request.observacoes(),
            usuario
        );
    }

    @PostMapping("/{id}/homologar-revisao-cadastro")
    public SubprocessoDto homologarRevisaoCadastro(
            @PathVariable Long id,
            @Valid @RequestBody HomologarCadastroReq request,
            @AuthenticationPrincipal Usuario usuario) {
        return subprocessoService.homologarRevisaoCadastro(
            id,
            request.observacoes(),
            usuario
        );
    }

    @GetMapping("/{id}/impactos-mapa")
    public ImpactoMapaDto verificarImpactos(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        return impactoMapaService.verificarImpactos(id, usuario);
    }
    
    @GetMapping("/{id}/mapa")
    public MapaCompletoDto obterMapa(@PathVariable Long id) {
        return mapaService.obterMapaSubprocesso(id);
    }

    @GetMapping("/{id}/mapa-visualizacao")
    public MapaVisualizacaoDto obterMapaVisualizacao(@PathVariable("id") Long subprocessoId) {
        return mapaService.obterMapaParaVisualizacao(subprocessoId);
    }
    
    @PutMapping("/{id}/mapa")
    @Transactional
    public MapaCompletoDto salvarMapa(
        @PathVariable Long id,
        @RequestBody @Valid SalvarMapaRequest request,
        @AuthenticationPrincipal Usuario usuario
    ) {
        return mapaService.salvarMapaSubprocesso(id, request, usuario.getTitulo());
    }
    
    @PostMapping("/{id}/disponibilizar-mapa")
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RespostaDto> disponibilizarMapa(
            @PathVariable Long id,
            @RequestBody @Valid DisponibilizarMapaReq request,
            @AuthenticationPrincipal Usuario usuario) {
        subprocessoService.disponibilizarMapa(
            id,
            request.observacoes(),
            request.dataLimiteEtapa2(),
            usuario
        );
        return ResponseEntity.ok(new RespostaDto("Mapa de competências disponibilizado com sucesso."));
    }
    
    @PostMapping("/{id}/apresentar-sugestoes")
    @Transactional
    public SubprocessoDto apresentarSugestoes(
            @PathVariable Long id,
            @RequestBody @Valid ApresentarSugestoesReq request,
            @AuthenticationPrincipal Usuario usuario) {
        return subprocessoService.apresentarSugestoes(
            id,
            request.sugestoes(),
            usuario.getTitulo()
        );
    }
    
    @PostMapping("/{id}/validar-mapa")
    @Transactional
    public SubprocessoDto validarMapa(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        return subprocessoService.validarMapa(id, usuario.getTitulo());
    }

    @GetMapping("/{id}/sugestoes")
    public SugestoesDto obterSugestoes(@PathVariable Long id) {
        return subprocessoService.obterSugestoes(id);
    }

    @GetMapping("/{id}/historico-validacao")
    public List<sgc.analise.dto.AnaliseValidacaoHistoricoDto> obterHistoricoValidacao(@PathVariable Long id) {
        return analiseService.listarPorSubprocesso(id, TipoAnalise.VALIDACAO)
            .stream()
            .map(analiseMapper::toAnaliseValidacaoHistoricoDto)
            .toList();
    }

    @PostMapping("/{id}/devolver-validacao")
    @Transactional
    public SubprocessoDto devolverValidacao(
        @PathVariable Long id,
        @RequestBody @Valid DevolverValidacaoReq request,
        @AuthenticationPrincipal Usuario usuario
    ) {
        return subprocessoService.devolverValidacao(
            id,
            request.justificativa(),
            usuario
        );
    }

    @PostMapping("/{id}/aceitar-validacao")
    @Transactional
    public SubprocessoDto aceitarValidacao(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        return subprocessoService.aceitarValidacao(id, usuario);
    }

    @PostMapping("/{id}/homologar-validacao")
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public SubprocessoDto homologarValidacao(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        return subprocessoService.homologarValidacao(id, usuario.getTitulo());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        try {
            subprocessoService.excluir(id);
            return ResponseEntity.noContent().build();
        } catch (ErroDominioNaoEncontrado e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/mapa-ajuste")
    public MapaAjusteDto obterMapaParaAjuste(@PathVariable Long id) {
        return subprocessoService.obterMapaParaAjuste(id);
    }

    @PutMapping("/{id}/mapa-ajuste")
    @Transactional
    public SubprocessoDto salvarAjustesMapa(
        @PathVariable Long id,
        @RequestBody @Valid SalvarAjustesReq request,
        @AuthenticationPrincipal Usuario usuario
    ) {
        return subprocessoService.salvarAjustesMapa(
            id,
            request.competencias(),
            usuario.getTitulo()
        );
    }

    @PostMapping("/{id}/submeter-mapa-ajustado")
    @Transactional
    public SubprocessoDto submeterMapaAjustado(
        @PathVariable Long id,
        @RequestBody @Valid SubmeterMapaAjustadoReq request,
        @AuthenticationPrincipal Usuario usuario
    ) {
        return subprocessoService.submeterMapaAjustado(id, request, usuario.getTitulo());
    }

    @PostMapping("/{id}/importar-atividades")
    @Transactional
    public Map<String, String> importarAtividades(
        @PathVariable Long id,
        @RequestBody @Valid ImportarAtividadesRequest request
    ) {
        subprocessoService.importarAtividades(id, request.subprocessoOrigemId());
        return Map.of("message", "Atividades importadas com sucesso.");
    }
}