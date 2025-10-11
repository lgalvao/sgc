package sgc.subprocesso;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import sgc.comum.dtos.RespostaDto;
import sgc.comum.erros.ErroValidacao;
import sgc.comum.modelo.Usuario;
import sgc.mapa.ImpactoMapaService;
import sgc.mapa.MapaService;
import sgc.mapa.dto.ImpactoMapaDto;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.modelo.Mapa;
import sgc.processo.modelo.Processo;
import sgc.subprocesso.dto.*;
import sgc.analise.dto.AnaliseCadastroDto;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;
import sgc.atividade.modelo.Atividade;

import java.net.URI;
import java.util.List;
import java.util.Map;

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

    @GetMapping("/{id}")
    public SubprocessoDetalheDto obterPorId(@PathVariable Long id,
                                              @RequestParam(required = false) String perfil,
                                              @RequestParam(required = false) Long unidadeUsuario) {
        return subprocessoService.obterDetalhes(id, perfil, unidadeUsuario);
    }

    @GetMapping("/{id}/historico-cadastro")
    public List<AnaliseCadastroDto> obterHistoricoCadastro(@PathVariable Long id) {
        return subprocessoService.getHistoricoAnaliseCadastro(id);
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

    @PostMapping("/{id}/devolver-cadastro")
    public SubprocessoDto devolverCadastro(
            @PathVariable Long id,
            @Valid @RequestBody DevolverCadastroReq request,
            @AuthenticationPrincipal Usuario usuario) {
        return subprocessoService.devolverCadastro(
            id,
            request.motivo(),
            request.observacoes(),
            usuario.getTitulo()
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
            usuario.getTitulo()
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
            usuario.getTitulo()
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
            usuario.getTitulo()
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
    public SubprocessoDto disponibilizarMapa(
            @PathVariable Long id,
            @RequestBody @Valid DisponibilizarMapaReq request,
            @AuthenticationPrincipal Usuario usuario) {
        return subprocessoService.disponibilizarMapa(
            id,
            request.observacoes(),
            request.dataLimiteEtapa2(),
            usuario.getTitulo()
        );
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
    public List<AnaliseValidacaoDto> obterHistoricoValidacao(@PathVariable Long id) {
        return subprocessoService.obterHistoricoValidacao(id);
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
            usuario.getTitulo()
        );
    }

    @PostMapping("/{id}/aceitar-validacao")
    @Transactional
    public SubprocessoDto aceitarValidacao(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        return subprocessoService.aceitarValidacao(id, usuario.getTitulo());
    }

    @PostMapping("/{id}/homologar-validacao")
    @Transactional
    public SubprocessoDto homologarValidacao(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        return subprocessoService.homologarValidacao(id, usuario.getTitulo());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        subprocessoRepo.deleteById(id);
        return ResponseEntity.noContent().build();
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
        @AuthenticationPrincipal Usuario usuario
    ) {
        return subprocessoService.submeterMapaAjustado(id, usuario.getTitulo());
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