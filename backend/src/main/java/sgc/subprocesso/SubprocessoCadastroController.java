package sgc.subprocesso;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import sgc.analise.dto.AnaliseHistoricoDto;
import sgc.analise.mapper.AnaliseMapper;
import sgc.analise.model.TipoAnalise;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.model.Atividade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Usuario;
import sgc.seguranca.sanitizacao.UtilSanitizacao;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.service.SubprocessoFacade;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subprocessos")
@RequiredArgsConstructor
@Tag(name = "Subprocessos", description = "Endpoints para gerenciamento do workflow de subprocessos")
public class SubprocessoCadastroController {

    private final SubprocessoFacade subprocessoFacade;
    private final sgc.analise.AnaliseFacade analiseFacade;
    private final AnaliseMapper analiseMapper;
    private final UsuarioFacade usuarioService;

    /**
     * Obtém o histórico de análises da fase de cadastro de um subprocesso.
     *
     * @param codigo O código do subprocesso.
     * @return Uma {@link List} de {@link AnaliseHistoricoDto} com o histórico.
     */
    @GetMapping("/{codigo}/historico-cadastro")
    @PreAuthorize("isAuthenticated()")
    public List<AnaliseHistoricoDto> obterHistoricoCadastro(@PathVariable Long codigo) {
        return analiseFacade.listarPorSubprocesso(codigo, TipoAnalise.CADASTRO).stream()
                .map(analiseMapper::toAnaliseHistoricoDto)
                .toList();
    }

    /**
     * Disponibiliza o cadastro de atividades de um subprocesso para a próxima etapa
     * de análise.
     *
     * <p>
     * Ação restrita a usuários com perfil 'CHEFE' (CDU-08).
     *
     * @param codSubprocesso O código do subprocesso.
     * @return Um {@link ResponseEntity} com uma mensagem de sucesso.
     */
    @PostMapping("/{codigo}/cadastro/disponibilizar")
    @PreAuthorize("hasRole('CHEFE')")
    @Operation(summary = "Disponibiliza o cadastro de atividades para análise")
    public ResponseEntity<MensagemResponse> disponibilizarCadastro(
            @PathVariable("codigo") Long codSubprocesso,
            @AuthenticationPrincipal Object principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        List<Atividade> faltando = subprocessoFacade.obterAtividadesSemConhecimento(codSubprocesso);
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

        subprocessoFacade.disponibilizarCadastro(codSubprocesso, usuario);
        return ResponseEntity.ok(new MensagemResponse("Cadastro de atividades disponibilizado"));
    }

    /**
     * Disponibiliza a revisão do cadastro de atividades para a próxima etapa de
     * análise.
     *
     * <p>
     * Ação restrita a usuários com perfil 'CHEFE' (CDU-12).
     *
     * <p>
     * Antes de disponibilizar, o método valida se todas as atividades do
     * subprocesso possuem
     * pelo menos um conhecimento associado.
     *
     * @param codigo O código do subprocesso.
     * @return Um {@link ResponseEntity} com uma mensagem de sucesso.
     * @throws ErroValidacao se existirem atividades sem conhecimentos.
     */
    @PostMapping("/{codigo}/disponibilizar-revisao")
    @PreAuthorize("hasRole('CHEFE')")
    @Operation(summary = "Disponibiliza a revisão do cadastro de atividades para análise")
    public ResponseEntity<MensagemResponse> disponibilizarRevisao(
            @PathVariable Long codigo, @AuthenticationPrincipal Object principal) {
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

    /**
     * Obtém os dados de cadastro de um subprocesso.
     *
     * @param codigo O código do subprocesso.
     * @return Um {@link SubprocessoCadastroDto} com os dados do cadastro.
     */
    @GetMapping("/{codigo}/cadastro")
    @PreAuthorize("isAuthenticated()")
    public SubprocessoCadastroDto obterCadastro(@PathVariable Long codigo) {
        return subprocessoFacade.obterCadastro(codigo);
    }

    /**
     * Devolve o cadastro de um subprocesso para o responsável pela unidade para que
     * sejam feitos
     * ajustes.
     *
     * <p>
     * Ação restrita a usuários com perfil 'ADMIN' ou 'GESTOR' (CDU-13).
     *
     * @param codigo  O código do subprocesso.
     * @param request O DTO contendo o motivo e as observações da devolução.
     */
    @PostMapping("/{codigo}/devolver-cadastro")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(summary = "Devolve o cadastro de atividades para o responsável")
    public void devolverCadastro(
            @PathVariable Long codigo,
            @Valid @RequestBody DevolverCadastroRequest request,
            @AuthenticationPrincipal Object principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        var sanitizedObservacoes = UtilSanitizacao.sanitizar(request.getObservacoes());

        subprocessoFacade.devolverCadastro(codigo, sanitizedObservacoes, usuario);
    }

    /**
     * Aceita o cadastro de um subprocesso, movendo-o para a próxima etapa do fluxo
     * de trabalho.
     *
     * <p>
     * Ação restrita a usuários com perfil 'ADMIN' ou 'GESTOR' (CDU-13).
     *
     * @param codigo  O código do subprocesso.
     * @param request O DTO contendo as observações da aceitação.
     */
    @PostMapping("/{codigo}/aceitar-cadastro")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(summary = "Aceita o cadastro de atividades")
    public void aceitarCadastro(
            @PathVariable Long codigo,
            @Valid @RequestBody AceitarCadastroRequest request,
            @AuthenticationPrincipal Object principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        var sanitizedObservacoes = UtilSanitizacao.sanitizar(request.getObservacoes());

        subprocessoFacade.aceitarCadastro(codigo, sanitizedObservacoes, usuario);
    }

    /**
     * Homologa o cadastro de um subprocesso.
     *
     * <p>
     * Ação restrita a usuários com perfil 'ADMIN' (CDU-13).
     *
     * @param codigo  O código do subprocesso.
     * @param request O DTO contendo as observações da homologação.
     */
    @PostMapping("/{codigo}/homologar-cadastro")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Homologa o cadastro de atividades")
    public void homologarCadastro(
            @PathVariable Long codigo,
            @Valid @RequestBody HomologarCadastroRequest request,
            @AuthenticationPrincipal Object principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        var sanitizedObservacoes = UtilSanitizacao.sanitizar(request.getObservacoes());

        subprocessoFacade.homologarCadastro(codigo, sanitizedObservacoes, usuario);
    }

    /**
     * Devolve a revisão de um cadastro de subprocesso para o responsável pela
     * unidade para que
     * sejam feitos ajustes.
     *
     * @param codigo  O código do subprocesso.
     * @param request O DTO contendo o motivo e as observações da devolução.
     */
    @PostMapping("/{codigo}/devolver-revisao-cadastro")
    @Operation(summary = "Devolve a revisão do cadastro de atividades para o responsável")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    public void devolverRevisaoCadastro(
            @PathVariable Long codigo,
            @Valid @RequestBody DevolverCadastroRequest request,
            @AuthenticationPrincipal Object principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        var sanitizedObservacoes = UtilSanitizacao.sanitizar(request.getObservacoes());

        subprocessoFacade.devolverRevisaoCadastro(codigo, sanitizedObservacoes, usuario);
    }

    /**
     * Aceita a revisão do cadastro de um subprocesso.
     *
     * @param codigo  O código do subprocesso.
     * @param request O DTO contendo as observações da aceitação.
     */
    @PostMapping("/{codigo}/aceitar-revisao-cadastro")
    @Operation(summary = "Aceita a revisão do cadastro de atividades")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    public void aceitarRevisaoCadastro(
            @PathVariable Long codigo,
            @Valid @RequestBody AceitarCadastroRequest request,
            @AuthenticationPrincipal Object principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        var sanitizedObservacoes = UtilSanitizacao.sanitizar(request.getObservacoes());

        subprocessoFacade.aceitarRevisaoCadastro(codigo, sanitizedObservacoes, usuario);
    }

    /**
     * Homologa a revisão do cadastro de um subprocesso.
     *
     * <p>
     * Esta ação é restrita a usuários com o perfil 'ADMIN'.
     *
     * @param codigo  O código do subprocesso.
     * @param request O DTO contendo as observações da homologação.
     */
    @PostMapping("/{codigo}/homologar-revisao-cadastro")
    @Operation(summary = "Homologa a revisão do cadastro de atividades")
    @PreAuthorize("hasRole('ADMIN')")
    public void homologarRevisaoCadastro(
            @PathVariable Long codigo,
            @Valid @RequestBody HomologarCadastroRequest request,
            @AuthenticationPrincipal Object principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        var sanitizedObservacoes = UtilSanitizacao.sanitizar(request.getObservacoes());

        subprocessoFacade.homologarRevisaoCadastro(codigo, sanitizedObservacoes, usuario);
    }

    /**
     * Importa atividades de um subprocesso de origem para o subprocesso de destino.
     *
     * <p>
     * Ação restrita a usuários com perfil 'CHEFE'.
     *
     * @param codigo  O código do subprocesso de destino.
     * @param request O DTO contendo o código do subprocesso de origem.
     * @return Um {@link Map} com uma mensagem de sucesso.
     */
    @PostMapping("/{codigo}/importar-atividades")
    @PreAuthorize("hasRole('CHEFE')")
    @Transactional
    @Operation(summary = "Importa atividades de outro subprocesso")
    public Map<String, String> importarAtividades(
            @PathVariable Long codigo, @RequestBody @Valid ImportarAtividadesRequest request) {
        subprocessoFacade.importarAtividades(codigo, request.getCodSubprocessoOrigem());
        return Map.of("message", "Atividades importadas.");
    }

    /**
     * Aceita o cadastro de atividades de múltiplas unidades em bloco.
     * (CDU-22)
     */
    @PostMapping("/{codigo}/aceitar-cadastro-bloco")
    @PreAuthorize("hasAnyRole('GESTOR', 'ADMIN')")
    @Operation(summary = "Aceita cadastros em bloco")
    public void aceitarCadastroEmBloco(@PathVariable Long codigo,
            @RequestBody @Valid ProcessarEmBlocoRequest request,
            @AuthenticationPrincipal Object principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        subprocessoFacade.aceitarCadastroEmBloco(request.getUnidadeCodigos(), codigo, usuario);
    }

    /**
     * Homologa o cadastro de atividades de múltiplas unidades em bloco.
     * (CDU-23)
     */
    @PostMapping("/{codigo}/homologar-cadastro-bloco")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Homologa cadastros em bloco")
    public void homologarCadastroEmBloco(@PathVariable Long codigo,
            @RequestBody @Valid ProcessarEmBlocoRequest request,
            @AuthenticationPrincipal Object principal) {
        Usuario usuario = obterUsuarioAutenticado(principal);
        subprocessoFacade.homologarCadastroEmBloco(request.getUnidadeCodigos(), codigo, usuario);
    }

    private Usuario obterUsuarioAutenticado(Object principal) {
        String titulo = usuarioService.extractTituloUsuario(principal);
        return usuarioService.buscarPorLogin(titulo);
    }
}
