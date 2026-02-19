package sgc.subprocesso;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import sgc.analise.AnaliseFacade;
import sgc.analise.dto.AnaliseHistoricoDto;
import sgc.comum.erros.ErroAutenticacao;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.MapaViews;
import sgc.organizacao.OrganizacaoFacade;
import sgc.organizacao.model.Usuario;
import sgc.seguranca.sanitizacao.UtilSanitizacao;
import sgc.comum.dto.ComumDtos.JustificativaRequest;
import sgc.comum.dto.ComumDtos.TextoRequest;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoService;
import sgc.subprocesso.service.SubprocessoWorkflowService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/subprocessos")
@RequiredArgsConstructor
@Tag(name = "Subprocessos", description = "Endpoints para gerenciamento do workflow de subprocessos")
public class SubprocessoCadastroController {

    private final SubprocessoService subprocessoService;
    private final SubprocessoWorkflowService subprocessoWorkflowService;
    private final AnaliseFacade analiseFacade;
    private final OrganizacaoFacade organizacaoFacade;

    @GetMapping("/{codigo}/historico-cadastro")
    @PreAuthorize("isAuthenticated()")
    public List<AnaliseHistoricoDto> obterHistoricoCadastro(@PathVariable Long codigo) {
        return analiseFacade.listarHistoricoCadastro(codigo);
    }

    @PostMapping("/{codigo}/cadastro/disponibilizar")
    @PreAuthorize("hasRole('CHEFE')")
    @Operation(summary = "Disponibiliza o cadastro de atividades para análise")
    public ResponseEntity<MensagemResponse> disponibilizarCadastro(
            @PathVariable("codigo") Long codSubprocesso,
            @AuthenticationPrincipal @Nullable Object principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        List<Atividade> faltando = subprocessoService.obterAtividadesSemConhecimento(codSubprocesso);
        if (!faltando.isEmpty()) {
            var lista = faltando.stream()
                    .map(
                            a -> Map.of(
                                    "codigo",
                                    a.getCodigo(),
                                    "descricao",
                                    a.getDescricao()))
                    .toList();
            throw new ErroValidacao(
                    "Existem atividades sem conhecimentos associados.",
                    Map.of("atividadesSemConhecimento", lista));
        }

        subprocessoWorkflowService.disponibilizarCadastro(codSubprocesso, usuario);
        return ResponseEntity.ok(new MensagemResponse("Cadastro de atividades disponibilizado"));
    }

    @PostMapping("/{codigo}/disponibilizar-revisao")
    @PreAuthorize("hasRole('CHEFE')")
    @Operation(summary = "Disponibiliza a revisão do cadastro de atividades para análise")
    public ResponseEntity<MensagemResponse> disponibilizarRevisao(
            @PathVariable Long codigo, @AuthenticationPrincipal @Nullable Object principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        List<Atividade> faltando = subprocessoService.obterAtividadesSemConhecimento(codigo);
        if (!faltando.isEmpty()) {
            var lista = faltando.stream()
                    .map(a -> Map.of("codigo", a.getCodigo(), "descricao", a.getDescricao()))
                    .toList();

            throw new ErroValidacao(
                    "Existem atividades sem conhecimentos associados.",
                    Map.of("atividadesSemConhecimento", lista));
        }

        subprocessoWorkflowService.disponibilizarRevisao(codigo, usuario);

        return ResponseEntity.ok(new MensagemResponse("Revisão do cadastro de atividades disponibilizada"));
    }

    @JsonView(MapaViews.Publica.class)
    @GetMapping("/{codigo}/cadastro")
    @PreAuthorize("isAuthenticated()")
    public Subprocesso obterCadastro(@PathVariable Long codigo) {
        return subprocessoService.buscarSubprocesso(codigo);
    }

    @PostMapping("/{codigo}/devolver-cadastro")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(summary = "Devolve o cadastro de atividades para o responsável")
    public void devolverCadastro(
            @PathVariable Long codigo,
            @Valid @RequestBody JustificativaRequest request,
            @AuthenticationPrincipal @Nullable Object principal) {
        
        Usuario usuario = obterUsuarioAutenticado(principal);
        String sanitizedObservacoes = Optional.of(UtilSanitizacao.sanitizar(request.justificativa()))
                .orElse("");

        subprocessoWorkflowService.devolverCadastro(codigo, usuario, sanitizedObservacoes);
    }

    @PostMapping("/{codigo}/aceitar-cadastro")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(summary = "Aceita o cadastro de atividades")
    public void aceitarCadastro(
            @PathVariable Long codigo,
            @Valid @RequestBody TextoRequest request,
            @AuthenticationPrincipal @Nullable Object principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        String sanitizedObservacoes = Optional.of(UtilSanitizacao.sanitizar(request.texto()))
                .orElse("");

        subprocessoWorkflowService.aceitarCadastro(codigo, usuario, sanitizedObservacoes);
    }

    @PostMapping("/{codigo}/homologar-cadastro")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Homologa o cadastro de atividades")
    public void homologarCadastro(
            @PathVariable Long codigo,
            @Valid @RequestBody TextoRequest request,
            @AuthenticationPrincipal @Nullable Object principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        String sanitizedObservacoes = Optional.of(UtilSanitizacao.sanitizar(request.texto()))
                .orElse("");

        subprocessoWorkflowService.homologarCadastro(codigo, usuario, sanitizedObservacoes);
    }

    @PostMapping("/{codigo}/devolver-revisao-cadastro")
    @Operation(summary = "Devolve a revisão do cadastro de atividades para o responsável")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    public void devolverRevisaoCadastro(
            @PathVariable Long codigo,
            @Valid @RequestBody JustificativaRequest request,
            @AuthenticationPrincipal @Nullable Object principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        String sanitizedObservacoes = Optional.of(UtilSanitizacao.sanitizar(request.justificativa()))
                .orElse("");

        subprocessoWorkflowService.devolverRevisaoCadastro(codigo, usuario, sanitizedObservacoes);
    }

    @PostMapping("/{codigo}/aceitar-revisao-cadastro")
    @Operation(summary = "Aceita a revisão do cadastro de atividades")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    public void aceitarRevisaoCadastro(
            @PathVariable Long codigo,
            @Valid @RequestBody TextoRequest request,
            @AuthenticationPrincipal @Nullable Object principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        String sanitizedObservacoes = Optional.of(UtilSanitizacao.sanitizar(request.texto()))
                .orElse("");

        subprocessoWorkflowService.aceitarRevisaoCadastro(codigo, usuario, sanitizedObservacoes);
    }

    @PostMapping("/{codigo}/homologar-revisao-cadastro")
    @Operation(summary = "Homologa a revisão do cadastro de atividades")
    @PreAuthorize("hasRole('ADMIN')")
    public void homologarRevisaoCadastro(
            @PathVariable Long codigo,
            @Valid @RequestBody TextoRequest request,
            @AuthenticationPrincipal @Nullable Object principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        String sanitizedObservacoes = Optional.of(UtilSanitizacao.sanitizar(request.texto()))
                .orElse("");

        subprocessoWorkflowService.homologarRevisaoCadastro(codigo, usuario, sanitizedObservacoes);
    }

    @PostMapping("/{codigo}/importar-atividades")
    @PreAuthorize("hasRole('CHEFE')")
    @Transactional
    @Operation(summary = "Importa atividades de outro subprocesso")
    public Map<String, String> importarAtividades(
            @PathVariable Long codigo, @RequestBody @Valid ImportarAtividadesRequest request) {
        subprocessoService.importarAtividades(codigo, request.codSubprocessoOrigem());
        return Map.of("message", "Atividades importadas.");
    }

    @PostMapping("/{codigo}/aceitar-cadastro-bloco")
    @PreAuthorize("hasAnyRole('GESTOR', 'ADMIN')")
    @Operation(summary = "Aceita cadastros em bloco")
    public void aceitarCadastroEmBloco(@PathVariable Long codigo,
            @RequestBody @Valid ProcessarEmBlocoRequest request,
            @AuthenticationPrincipal @Nullable Object principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        subprocessoWorkflowService.aceitarCadastroEmBloco(request.subprocessos(), usuario);
    }

    @PostMapping("/{codigo}/homologar-cadastro-bloco")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Homologa cadastros em bloco")
    public void homologarCadastroEmBloco(@PathVariable Long codigo,
            @RequestBody @Valid ProcessarEmBlocoRequest request,
            @AuthenticationPrincipal @Nullable Object principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        subprocessoWorkflowService.homologarCadastroEmBloco(request.subprocessos(), usuario);
    }

    private Usuario obterUsuarioAutenticado(@Nullable Object principal) {
        if (principal instanceof Usuario usuario) {
            return usuario;
        }
        String titulo = organizacaoFacade.extrairTituloUsuario(principal);
        if (titulo == null) {
            throw new ErroAutenticacao("Usuário não identificado");
        }
        return organizacaoFacade.buscarPorLogin(titulo);
    }
}
