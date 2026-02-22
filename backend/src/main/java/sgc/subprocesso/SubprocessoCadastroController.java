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
import sgc.comum.dto.ComumDtos.JustificativaRequest;
import sgc.comum.dto.ComumDtos.TextoOpcionalRequest;
import sgc.comum.erros.ErroAutenticacao;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.MapaViews;
import sgc.organizacao.OrganizacaoFacade;
import sgc.organizacao.model.Usuario;
import sgc.seguranca.sanitizacao.UtilSanitizacao;
import sgc.subprocesso.dto.ContextoEdicaoResponse;
import sgc.subprocesso.dto.ImportarAtividadesRequest;
import sgc.subprocesso.dto.MensagemResponse;
import sgc.subprocesso.dto.ProcessarEmBlocoRequest;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoViews;
import sgc.subprocesso.service.SubprocessoFacade;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/subprocessos")
@RequiredArgsConstructor
@Tag(name = "Subprocessos", description = "Endpoints para gerenciamento do workflow de subprocessos")
public class SubprocessoCadastroController {

    private final SubprocessoFacade subprocessoFacade;
    private final AnaliseFacade analiseFacade;
    private final OrganizacaoFacade organizacaoFacade;

    @GetMapping("/{codigo}/historico-cadastro")
    @PreAuthorize("@subprocessoSecurity.canView(#codigo)")
    public List<AnaliseHistoricoDto> obterHistoricoCadastro(@PathVariable Long codigo) {
        return analiseFacade.listarHistoricoCadastro(codigo);
    }

    @GetMapping("/{codigo}/contexto-edicao")
    @PreAuthorize("@subprocessoSecurity.canView(#codigo)")
    @JsonView(SubprocessoViews.Publica.class)
    public ResponseEntity<ContextoEdicaoResponse> obterContextoEdicao(@PathVariable Long codigo) {
        return ResponseEntity.ok(subprocessoFacade.obterContextoEdicao(codigo));
    }

    @PostMapping("/{codigo}/cadastro/disponibilizar")
    @PreAuthorize("@subprocessoSecurity.canExecute(#codSubprocesso, 'DISPONIBILIZAR_CADASTRO')")
    @Operation(summary = "Disponibiliza o cadastro de atividades para análise")
    public ResponseEntity<MensagemResponse> disponibilizarCadastro(
            @PathVariable("codigo") Long codSubprocesso,
            @AuthenticationPrincipal @Nullable Object principal) {

        Usuario usuario = obterUsuarioAutenticado(principal);
        List<Atividade> faltando = subprocessoFacade.obterAtividadesSemConhecimento(codSubprocesso);
        if (!faltando.isEmpty()) {
            var lista = faltando.stream()
                    .map(a -> Map.of("codigo", a.getCodigo(), "descricao", a.getDescricao()))
                    .toList();

            throw new ErroValidacao(
                    "Existem atividades sem conhecimentos associados.",
                    Map.of("atividadesSemConhecimento", lista));
        }

        subprocessoFacade.disponibilizarCadastro(codSubprocesso, usuario);
        return ResponseEntity.ok(new MensagemResponse("Cadastro de atividades disponibilizado"));
    }

    @PostMapping("/{codigo}/disponibilizar-revisao")
    @PreAuthorize("@subprocessoSecurity.canExecute(#codigo, 'DISPONIBILIZAR_REVISAO_CADASTRO')")
    @Operation(summary = "Disponibiliza a revisão do cadastro de atividades para análise")
    public ResponseEntity<MensagemResponse> disponibilizarRevisao(
            @PathVariable Long codigo, @AuthenticationPrincipal @Nullable Object principal) {

        Usuario usuario = obterUsuarioAutenticado(principal);
        List<Atividade> faltando = subprocessoFacade.obterAtividadesSemConhecimento(codigo);
        if (!faltando.isEmpty()) {
            var lista = faltando.stream()
                    .map(a -> Map.of("codigo", a.getCodigo(), "descricao", a.getDescricao()))
                    .toList();

            throw new ErroValidacao(
                    "Existem atividades sem conhecimentos associados.",
                    Map.of("atividadesSemConhecimento", lista));
        }

        subprocessoFacade.disponibilizarRevisao(codigo, usuario);

        return ResponseEntity.ok(new MensagemResponse("Revisão do cadastro de atividades disponibilizada"));
    }

    @JsonView(MapaViews.Publica.class)
    @GetMapping("/{codigo}/cadastro")
    @PreAuthorize("@subprocessoSecurity.canView(#codigo)")
    public Subprocesso obterCadastro(@PathVariable Long codigo) {
        return subprocessoFacade.buscarSubprocesso(codigo);
    }

    @PostMapping("/{codigo}/devolver-cadastro")
    @PreAuthorize("@subprocessoSecurity.canExecute(#codigo, 'DEVOLVER_CADASTRO')")
    @Operation(summary = "Devolve o cadastro de atividades para o responsável")
    public void devolverCadastro(
            @PathVariable Long codigo,
            @Valid @RequestBody JustificativaRequest request,
            @AuthenticationPrincipal @Nullable Object principal) {
        
        Usuario usuario = obterUsuarioAutenticado(principal);
        String sanitizedObservacoes = Optional.of(UtilSanitizacao.sanitizar(request.justificativa()))
                .orElse("");

        subprocessoFacade.devolverCadastro(codigo, sanitizedObservacoes, usuario);
    }

    @PostMapping("/{codigo}/aceitar-cadastro")
    @PreAuthorize("@subprocessoSecurity.canExecute(#codigo, 'ACEITAR_CADASTRO')")
    @Operation(summary = "Aceita o cadastro de atividades")
    public void aceitarCadastro(
            @PathVariable Long codigo,
            @Valid @RequestBody TextoOpcionalRequest request,
            @AuthenticationPrincipal @Nullable Object principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        String sanitizedObservacoes = Optional.ofNullable(request.texto())
                .map(UtilSanitizacao::sanitizar)
                .orElse("");

        subprocessoFacade.aceitarCadastro(codigo, sanitizedObservacoes, usuario);
    }

    @PostMapping("/{codigo}/homologar-cadastro")
    @PreAuthorize("@subprocessoSecurity.canExecute(#codigo, 'HOMOLOGAR_CADASTRO')")
    @Operation(summary = "Homologa o cadastro de atividades")
    public void homologarCadastro(
            @PathVariable Long codigo,
            @Valid @RequestBody TextoOpcionalRequest request,
            @AuthenticationPrincipal @Nullable Object principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        String sanitizedObservacoes = Optional.ofNullable(request.texto())
                .map(UtilSanitizacao::sanitizar)
                .orElse("");

        subprocessoFacade.homologarCadastro(codigo, sanitizedObservacoes, usuario);
    }

    @PostMapping("/{codigo}/devolver-revisao-cadastro")
    @Operation(summary = "Devolve a revisão do cadastro de atividades para o responsável")
    @PreAuthorize("@subprocessoSecurity.canExecute(#codigo, 'DEVOLVER_REVISAO_CADASTRO')")
    public void devolverRevisaoCadastro(
            @PathVariable Long codigo,
            @Valid @RequestBody JustificativaRequest request,
            @AuthenticationPrincipal @Nullable Object principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        String sanitizedObservacoes = Optional.of(UtilSanitizacao.sanitizar(request.justificativa()))
                .orElse("");

        subprocessoFacade.devolverRevisaoCadastro(codigo, sanitizedObservacoes, usuario);
    }

    @PostMapping("/{codigo}/aceitar-revisao-cadastro")
    @Operation(summary = "Aceita a revisão do cadastro de atividades")
    @PreAuthorize("@subprocessoSecurity.canExecute(#codigo, 'ACEITAR_REVISAO_CADASTRO')")
    public void aceitarRevisaoCadastro(
            @PathVariable Long codigo,
            @Valid @RequestBody TextoOpcionalRequest request,
            @AuthenticationPrincipal @Nullable Object principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        String sanitizedObservacoes = Optional.ofNullable(request.texto())
                .map(UtilSanitizacao::sanitizar)
                .orElse("");

        subprocessoFacade.aceitarRevisaoCadastro(codigo, sanitizedObservacoes, usuario);
    }

    @PostMapping("/{codigo}/homologar-revisao-cadastro")
    @Operation(summary = "Homologa a revisão do cadastro de atividades")
    @PreAuthorize("@subprocessoSecurity.canExecute(#codigo, 'HOMOLOGAR_REVISAO_CADASTRO')")
    public void homologarRevisaoCadastro(
            @PathVariable Long codigo,
            @Valid @RequestBody TextoOpcionalRequest request,
            @AuthenticationPrincipal @Nullable Object principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        String sanitizedObservacoes = Optional.ofNullable(request.texto())
                .map(UtilSanitizacao::sanitizar)
                .orElse("");

        subprocessoFacade.homologarRevisaoCadastro(codigo, sanitizedObservacoes, usuario);
    }

    @PostMapping("/{codigo}/importar-atividades")
    @PreAuthorize("@subprocessoSecurity.canExecute(#codigo, 'EDITAR_CADASTRO')")
    @Transactional
    @Operation(summary = "Importa atividades de outro subprocesso")
    public Map<String, String> importarAtividades(
            @PathVariable Long codigo, @RequestBody @Valid ImportarAtividadesRequest request) {
        subprocessoFacade.importarAtividades(codigo, request.codSubprocessoOrigem());
        return Map.of("message", "Atividades importadas.");
    }

    @PostMapping("/{codigo}/aceitar-cadastro-bloco")
    @PreAuthorize("@subprocessoSecurity.canExecuteBulk(#request.subprocessos, 'ACEITAR_CADASTRO')")
    @Operation(summary = "Aceita cadastros em bloco")
    public void aceitarCadastroEmBloco(@PathVariable Long codigo,
            @RequestBody @Valid ProcessarEmBlocoRequest request,
            @AuthenticationPrincipal @Nullable Object principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        subprocessoFacade.aceitarCadastroEmBloco(request.subprocessos(), usuario);
    }

    @PostMapping("/{codigo}/homologar-cadastro-bloco")
    @PreAuthorize("@subprocessoSecurity.canExecuteBulk(#request.subprocessos, 'HOMOLOGAR_CADASTRO')")
    @Operation(summary = "Homologa cadastros em bloco")
    public void homologarCadastroEmBloco(@PathVariable Long codigo,
            @RequestBody @Valid ProcessarEmBlocoRequest request,
            @AuthenticationPrincipal @Nullable Object principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        subprocessoFacade.homologarCadastroEmBloco(request.subprocessos(), usuario);
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
