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
import sgc.analise.AnaliseFacade;
import sgc.analise.dto.AnaliseValidacaoHistoricoDto;
import sgc.analise.mapper.AnaliseMapper;
import sgc.analise.model.TipoAnalise;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.service.SubprocessoFacade;

import java.util.List;

@RestController
@RequestMapping("/api/subprocessos")
@RequiredArgsConstructor
@Transactional
@Tag(name = "Subprocessos", description = "Gerenciamento do workflow de subprocessos")
public class SubprocessoValidacaoController {
    private final SubprocessoFacade subprocessoFacade;
    private final AnaliseFacade analiseFacade;
    private final AnaliseMapper analiseMapper;

    /**
     * Disponibiliza o mapa de competências de um subprocesso para as unidades
     * envolvidas iniciarem a etapa de validação.
     */
    @PostMapping("/{codigo}/disponibilizar-mapa")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Disponibiliza o mapa de competências para as unidades")
    public ResponseEntity<MensagemResponse> disponibilizarMapa(
            @PathVariable Long codigo,
            @RequestBody @Valid DisponibilizarMapaRequest request,
            @AuthenticationPrincipal Usuario usuario) {

        DisponibilizarMapaRequest serviceRequest = DisponibilizarMapaRequest.builder()
                .dataLimite(request.dataLimite())
                .observacoes(request.observacoes())
                .build();

        subprocessoFacade.disponibilizarMapa(codigo, serviceRequest, usuario);
        return ResponseEntity.ok(new MensagemResponse("Mapa de competências disponibilizado."));
    }

    /**
     * Permite que um usuário apresente sugestões de melhoria para um mapa de competências.
     */
    @PostMapping("/{codigo}/apresentar-sugestoes")
    @PreAuthorize("hasRole('CHEFE')")
    @Operation(summary = "Apresenta sugestões de melhoria para o mapa")
    public void apresentarSugestoes(
            @PathVariable Long codigo,
            @RequestBody @Valid ApresentarSugestoesRequest request,
            @AuthenticationPrincipal Usuario usuario) {

        subprocessoFacade.apresentarSugestoes(codigo, request.sugestoes(), usuario);
    }

    /**
     * Registra a validação de um mapa de competências pelo responsável da unidade.
     */
    @PostMapping("/{codigo}/validar-mapa")
    @PreAuthorize("hasRole('CHEFE')")
    @Operation(summary = "Valida o mapa de competências da unidade")
    public void validarMapa(@PathVariable Long codigo, @AuthenticationPrincipal Usuario usuario) {
        subprocessoFacade.validarMapa(codigo, usuario);
    }

    /**
     * Obtém as sugestões de melhoria que foram apresentadas para o mapa de um
     * subprocesso.
     */
    @GetMapping("/{codigo}/sugestoes")
    @PreAuthorize("isAuthenticated()")
    public SugestoesDto obterSugestoes(@PathVariable Long codigo) {
        return subprocessoFacade.obterSugestoes(codigo);
    }

    /**
     * Obtém o histórico de análises da fase de validação de um subprocesso.
     */
    @GetMapping("/{codigo}/historico-validacao")
    @PreAuthorize("isAuthenticated()")
    public List<AnaliseValidacaoHistoricoDto> obterHistoricoValidacao(@PathVariable Long codigo) {
        return analiseFacade.listarPorSubprocesso(codigo, TipoAnalise.VALIDACAO).stream()
                .map(analiseMapper::toAnaliseValidacaoHistoricoDto)
                .toList();
    }

    /**
     * Devolve a validação de um mapa para a unidade de negócio responsável para que
     * sejam feitos ajustes.
     */
    @PostMapping("/{codigo}/devolver-validacao")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(summary = "Devolve a validação do mapa para a unidade de negócio")
    public void devolverValidacao(@PathVariable Long codigo,
            @RequestBody @Valid DevolverValidacaoRequest request,
            @AuthenticationPrincipal Usuario usuario) {

        subprocessoFacade.devolverValidacao(codigo, request.justificativa(), usuario);
    }

    /**
     * Aceita a validação de um mapa, movendo o subprocesso para a próxima etapa de
     * análise hierárquica.
     */
    @PostMapping("/{codigo}/aceitar-validacao")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR')")
    @Operation(summary = "Aceita a validação do mapa")
    public void aceitarValidacao(@PathVariable Long codigo, @AuthenticationPrincipal Usuario usuario) {
        subprocessoFacade.aceitarValidacao(codigo, usuario);
    }

    /**
     * Homologa a validação de um mapa, finalizando o fluxo de aprovações.
     */
    @PostMapping("/{codigo}/homologar-validacao")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Homologa a validação do mapa")
    public void homologarValidacao(@PathVariable Long codigo, @AuthenticationPrincipal Usuario usuario) {
        subprocessoFacade.homologarValidacao(codigo, usuario);
    }

    /**
     * Submete a versão ajustada de um mapa para uma nova rodada de validação.
     */
    @PostMapping("/{codigo}/submeter-mapa-ajustado")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Submete o mapa ajustado para nova validação")
    public void submeterMapaAjustado(@PathVariable Long codigo, @RequestBody @Valid SubmeterMapaAjustadoRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        subprocessoFacade.submeterMapaAjustado(codigo, request, usuario);
    }

    /**
     * Aceita a validação de mapa de competências de múltiplas unidades em bloco.
     */
    @PostMapping("/{codigo}/aceitar-validacao-bloco")
    @PreAuthorize("hasRole('GESTOR')")
    @Operation(summary = "Aceita validação de mapas em bloco")
    public void aceitarValidacaoEmBloco(@PathVariable Long codigo,
            @RequestBody @Valid ProcessarEmBlocoRequest request,
            @AuthenticationPrincipal Usuario usuario) {
                
        subprocessoFacade.aceitarValidacaoEmBloco(request.subprocessos(), codigo, usuario);
    }

    /**
     * Homologa a validação de mapa de competências de múltiplas unidades em bloco.
     */
    @PostMapping("/{codigo}/homologar-validacao-bloco")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Homologa validação de mapas em bloco")
    public void homologarValidacaoEmBloco(@PathVariable Long codigo,
            @RequestBody @Valid ProcessarEmBlocoRequest request,
            @AuthenticationPrincipal Usuario usuario) {

        subprocessoFacade.homologarValidacaoEmBloco(request.subprocessos(), codigo, usuario);
    }
}
