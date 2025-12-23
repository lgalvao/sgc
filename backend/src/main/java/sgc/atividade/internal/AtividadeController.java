package sgc.atividade.internal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import sgc.atividade.AtividadeService;
import sgc.atividade.api.AtividadeDto;
import sgc.atividade.api.ConhecimentoDto;
import sgc.atividade.internal.model.Atividade;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.subprocesso.api.AtividadeOperacaoResponse;
import sgc.subprocesso.api.AtividadeVisualizacaoDto;
import sgc.subprocesso.api.SubprocessoSituacaoDto;
import sgc.subprocesso.internal.model.Subprocesso;
import sgc.subprocesso.internal.service.SubprocessoService;

import java.net.URI;
import java.util.List;

/**
 * Controlador REST para gerenciar Atividades e seus Conhecimentos associados.
 */
@RestController
@RequestMapping("/api/atividades")
@RequiredArgsConstructor
@Tag(name = "Atividades", description = "Gerenciamento de atividades e seus conhecimentos")
public class AtividadeController {
    private final AtividadeService atividadeService;
    private final SubprocessoService subprocessoService;

    /**
     * Retorna uma lista com todas as atividades cadastradas no sistema.
     *
     * @return Uma lista de {@link AtividadeDto}.
     */
    @GetMapping
    @Operation(summary = "Lista todas as atividades")
    public List<AtividadeDto> listar() {
        return atividadeService.listar();
    }

    /**
     * Busca e retorna uma atividade específica pelo seu código.
     *
     * @param codAtividade O código da atividade a ser buscada.
     * @return Um {@link ResponseEntity} contendo a {@link AtividadeDto} correspondente ou um status
     * 404 Not Found se a atividade não for encontrada.
     */
    @GetMapping("/{codAtividade}")
    @Operation(summary = "Obtém uma atividade pelo código")
    public ResponseEntity<AtividadeDto> obterPorId(@PathVariable Long codAtividade) {
        return ResponseEntity.ok(atividadeService.obterPorCodigo(codAtividade));
    }

    /**
     * Cria uma nova atividade no sistema.
     *
     * <p>O método recebe os dados da atividade, sanitiza a descrição para remover HTML e a
     * persiste, associando-a ao usuário autenticado. Retorna também o status atualizado do
     * subprocesso para evitar chamadas adicionais ao backend.
     *
     * @param atividadeDto O DTO contendo os dados da atividade a ser criada.
     * @return Um {@link ResponseEntity} com status 201 Created e {@link AtividadeOperacaoResponse}
     * contendo a atividade criada e o status do subprocesso.
     */
    @PostMapping
    @Operation(summary = "Cria uma atividade")
    public ResponseEntity<AtividadeOperacaoResponse> criar(@Valid @RequestBody AtividadeDto atividadeDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String tituloUsuario = authentication != null ? authentication.getName() : null;
        if (tituloUsuario == null || tituloUsuario.isBlank()) {
            throw new ErroAccessoNegado("Usuário não autenticado.");
        }
        var salvo = atividadeService.criar(atividadeDto, tituloUsuario);

        // Buscar subprocesso e situação
        AtividadeOperacaoResponse response = criarRespostaOperacaoPorMapaCodigo(atividadeDto.getMapaCodigo(), salvo.getCodigo(), true);

        URI uri = URI.create("/api/atividades/%d".formatted(salvo.getCodigo()));
        return ResponseEntity.created(uri).body(response);
    }

    /**
     * Atualiza os dados de uma atividade existente.
     *
     * @param atividadeDto O DTO com os novos dados da atividade. A descrição será sanitizada.
     * @return Um {@link ResponseEntity} com {@link AtividadeOperacaoResponse} contendo
     * a atividade atualizada e o status do subprocesso.
     */
    @PostMapping("/{codigo}/atualizar")
    @Operation(summary = "Atualiza atividade existente")
    public ResponseEntity<AtividadeOperacaoResponse> atualizar(@PathVariable Long codigo, @RequestBody @Valid AtividadeDto atividadeDto) {
        atividadeService.atualizar(codigo, atividadeDto);
        AtividadeOperacaoResponse response = criarRespostaOperacaoPorAtividade(codigo);
        return ResponseEntity.ok(response);
    }

    /**
     * Exclui uma atividade do sistema.
     *
     * <p>Se a atividade não for encontrada, o serviço lançará uma exceção que resultará em uma
     * resposta 404 Not Found. Retorna o status atualizado do subprocesso.
     *
     * @param codAtividade O código da atividade a ser excluída.
     * @return Um {@link ResponseEntity} com {@link AtividadeOperacaoResponse} contendo
     * o status atualizado do subprocesso (atividade será null).
     */
    @PostMapping("/{codAtividade}/excluir")
    @Operation(summary = "Exclui uma atividade")
    public ResponseEntity<AtividadeOperacaoResponse> excluir(@PathVariable Long codAtividade) {
        Atividade atividade = atividadeService.obterEntidadePorCodigo(codAtividade);
        Long codMapa = atividade.getMapa().getCodigo();

        atividadeService.excluir(codAtividade);

        // Buscar status atualizado após exclusão
        AtividadeOperacaoResponse response = criarRespostaOperacaoPorMapaCodigo(codMapa, codAtividade, false);

        return ResponseEntity.ok(response);
    }

    /**
     * Lista todos os conhecimentos associados a uma atividade específica.
     *
     * @param codAtividade O código da atividade pai.
     * @return Um {@link ResponseEntity} com status 200 OK e a lista de {@link ConhecimentoDto}.
     */
    @GetMapping("/{codAtividade}/conhecimentos")
    @Operation(summary = "Lista todos os conhecimentos de uma atividade")
    public ResponseEntity<List<ConhecimentoDto>> listarConhecimentos(
            @PathVariable Long codAtividade) {
        return ResponseEntity.ok(atividadeService.listarConhecimentos(codAtividade));
    }

    /**
     * Adiciona um novo conhecimento a uma atividade existente.
     *
     * @param codAtividade    O código da atividade à qual o conhecimento será associado.
     * @param conhecimentoDto O DTO com os dados do conhecimento a ser criado.
     * @return Um {@link ResponseEntity} com status 201 Created e {@link AtividadeOperacaoResponse}
     * contendo a atividade atualizada com o novo conhecimento e o situação do subprocesso.
     */
    @PostMapping("/{codAtividade}/conhecimentos")
    @Operation(summary = "Cria um conhecimento para uma atividade")
    public ResponseEntity<AtividadeOperacaoResponse> criarConhecimento(
            @PathVariable Long codAtividade,
            @Valid @RequestBody ConhecimentoDto conhecimentoDto) {
        var salvo = atividadeService.criarConhecimento(codAtividade, conhecimentoDto);

        AtividadeOperacaoResponse response = criarRespostaOperacaoPorAtividade(codAtividade);
        URI uri = URI.create("/api/atividades/%d/conhecimentos/%d".formatted(codAtividade, salvo.getCodigo()));
        return ResponseEntity.created(uri).body(response);
    }

    /**
     * Atualiza um conhecimento existente dentro de uma atividade.
     *
     * @param codAtividade    O código da atividade pai.
     * @param codConhecimento O código do conhecimento a ser atualizado.
     * @param conhecimentoDto O DTO com os novos dados do conhecimento.
     * @return Um {@link ResponseEntity} com {@link AtividadeOperacaoResponse} contendo
     * a atividade atualizada e o status do subprocesso.
     */
    @PostMapping("/{codAtividade}/conhecimentos/{codConhecimento}/atualizar")
    @Operation(summary = "Atualiza um conhecimento existente em uma atividade")
    public ResponseEntity<AtividadeOperacaoResponse> atualizarConhecimento(
            @PathVariable Long codAtividade,
            @PathVariable Long codConhecimento,
            @Valid @RequestBody ConhecimentoDto conhecimentoDto) {

        atividadeService.atualizarConhecimento(codAtividade, codConhecimento, conhecimentoDto);
        AtividadeOperacaoResponse response = criarRespostaOperacaoPorAtividade(codAtividade);

        return ResponseEntity.ok(response);
    }

    /**
     * Exclui um conhecimento de uma atividade.
     *
     * @param codAtividade    O código da atividade pai.
     * @param codConhecimento O código do conhecimento a ser excluído.
     * @return Um {@link ResponseEntity} com {@link AtividadeOperacaoResponse} contendo
     * a atividade atualizada e o status do subprocesso.
     */
    @PostMapping("/{codAtividade}/conhecimentos/{codConhecimento}/excluir")
    @Operation(summary = "Exclui um conhecimento de uma atividade")
    public ResponseEntity<AtividadeOperacaoResponse> excluirConhecimento(@PathVariable Long codAtividade, @PathVariable Long codConhecimento) {
        atividadeService.excluirConhecimento(codAtividade, codConhecimento);

        AtividadeOperacaoResponse response = criarRespostaOperacaoPorAtividade(codAtividade);
        return ResponseEntity.ok(response);
    }

    /**
     * Método helper para obter o código do subprocesso a partir do código do mapa.
     *
     * @param codMapa O código do mapa.
     * @return O código do subprocesso associado ao mapa.
     * @throws ErroEntidadeNaoEncontrada se o subprocesso não for encontrado.
     */
    private Long obterCodigoSubprocessoPorMapa(Long codMapa) {
        Subprocesso subprocesso = subprocessoService.obterEntidadePorCodigoMapa(codMapa);
        return subprocesso.getCodigo();
    }

    // Helper: obtain subprocesso code from an atividade id (reads the Atividade entity once)
    private Long obterCodigoSubprocessoPorAtividade(Long codigoAtividade) {
        Atividade atividade = atividadeService.obterEntidadePorCodigo(codigoAtividade);
        return obterCodigoSubprocessoPorMapa(atividade.getMapa().getCodigo());
    }

    private AtividadeOperacaoResponse criarRespostaOperacaoPorAtividade(Long codigoAtividade) {
        Long codSubprocesso = obterCodigoSubprocessoPorAtividade(codigoAtividade);
        return criarRespostaOperacao(codSubprocesso, codigoAtividade, true);
    }

    private AtividadeOperacaoResponse criarRespostaOperacaoPorMapaCodigo(Long mapaCodigo, Long codigoAtividade, boolean incluirAtividade) {
        Long codSubprocesso = obterCodigoSubprocessoPorMapa(mapaCodigo);
        return criarRespostaOperacao(codSubprocesso, codigoAtividade, incluirAtividade);
    }

    private AtividadeOperacaoResponse criarRespostaOperacao(Long codSubprocesso, Long codigoAtividade, boolean incluirAtividade) {
        SubprocessoSituacaoDto status = subprocessoService.obterSituacao(codSubprocesso);
        AtividadeVisualizacaoDto atividadeVis = null;
        if (incluirAtividade) {
            atividadeVis = subprocessoService.listarAtividadesSubprocesso(codSubprocesso)
                    .stream()
                    .filter(a -> a.getCodigo().equals(codigoAtividade))
                    .findFirst()
                    .orElse(null);
        }
        return AtividadeOperacaoResponse.builder()
                .atividade(atividadeVis)
                .subprocesso(status)
                .build();
    }
}
