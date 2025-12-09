package sgc.atividade;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import sgc.atividade.dto.AtividadeDto;
import sgc.atividade.dto.ConhecimentoDto;
import sgc.atividade.model.Atividade;
import sgc.atividade.model.AtividadeRepo;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.subprocesso.dto.AtividadeOperacaoResponse;
import sgc.subprocesso.dto.AtividadeVisualizacaoDto;
import sgc.subprocesso.dto.SubprocessoStatusDto;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.service.SubprocessoService;

/** Controlador REST para gerenciar Atividades e seus Conhecimentos associados. */
@RestController
@RequestMapping("/api/atividades")
@RequiredArgsConstructor
@Tag(
        name = "Atividades",
        description = "Endpoints para gerenciamento de atividades e seus conhecimentos associados")
public class AtividadeController {
    private final AtividadeService atividadeService;
    private final SubprocessoService subprocessoService;
    private final SubprocessoRepo subprocessoRepo;
    private final AtividadeRepo atividadeRepo;

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
     *     404 Not Found se a atividade não for encontrada.
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
     *     contendo a atividade criada e o status do subprocesso.
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
        
        // Buscar subprocesso e status
        Long codSubprocesso = obterCodigoSubprocessoPorMapa(atividadeDto.getMapaCodigo());
        SubprocessoStatusDto status = subprocessoService.obterStatus(codSubprocesso);
        AtividadeVisualizacaoDto atividadeVis = subprocessoService.listarAtividadesPorSubprocesso(codSubprocesso)
                .stream()
                .filter(a -> a.getCodigo().equals(salvo.getCodigo()))
                .findFirst()
                .orElse(null);
        
        AtividadeOperacaoResponse response = AtividadeOperacaoResponse.builder()
                .atividade(atividadeVis)
                .subprocesso(status)
                .build();
        
        URI uri = URI.create("/api/atividades/%d".formatted(salvo.getCodigo()));
        return ResponseEntity.created(uri).body(response);
    }

    /**
     * Atualiza os dados de uma atividade existente.
     *
     * @param atividadeDto O DTO com os novos dados da atividade. A descrição será sanitizada.
     * @return Um {@link ResponseEntity} com {@link AtividadeOperacaoResponse} contendo
     *     a atividade atualizada e o status do subprocesso.
     */
    @PostMapping("/{codigo}/atualizar")
    @Operation(summary = "Atualiza atividade existente")
    public ResponseEntity<AtividadeOperacaoResponse> atualizar(
            @PathVariable Long codigo, @RequestBody @Valid AtividadeDto atividadeDto) {
        var atualizado = atividadeService.atualizar(codigo, atividadeDto);
        
        // Buscar subprocesso e status
        Atividade atividade = atividadeRepo.findById(codigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Atividade", codigo));
        Long codSubprocesso = obterCodigoSubprocessoPorMapa(atividade.getMapa().getCodigo());
        SubprocessoStatusDto status = subprocessoService.obterStatus(codSubprocesso);
        AtividadeVisualizacaoDto atividadeVis = subprocessoService.listarAtividadesPorSubprocesso(codSubprocesso)
                .stream()
                .filter(a -> a.getCodigo().equals(codigo))
                .findFirst()
                .orElse(null);
        
        AtividadeOperacaoResponse response = AtividadeOperacaoResponse.builder()
                .atividade(atividadeVis)
                .subprocesso(status)
                .build();
        
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
     *     o status atualizado do subprocesso (atividade será null).
     */
    @PostMapping("/{codAtividade}/excluir")
    @Operation(summary = "Exclui uma atividade")
    public ResponseEntity<AtividadeOperacaoResponse> excluir(@PathVariable Long codAtividade) {
        // Buscar mapa antes de excluir
        Atividade atividade = atividadeRepo.findById(codAtividade)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Atividade", codAtividade));
        Long codMapa = atividade.getMapa().getCodigo();
        Long codSubprocesso = obterCodigoSubprocessoPorMapa(codMapa);
        
        atividadeService.excluir(codAtividade);
        
        // Buscar status atualizado após exclusão
        SubprocessoStatusDto status = subprocessoService.obterStatus(codSubprocesso);
        
        AtividadeOperacaoResponse response = AtividadeOperacaoResponse.builder()
                .atividade(null) // Atividade foi excluída
                .subprocesso(status)
                .build();
        
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
     * @param codAtividade O código da atividade à qual o conhecimento será associado.
     * @param conhecimentoDto O DTO com os dados do conhecimento a ser criado.
     * @return Um {@link ResponseEntity} com status 201 Created e {@link AtividadeOperacaoResponse}
     *     contendo a atividade atualizada com o novo conhecimento e o status do subprocesso.
     */
    @PostMapping("/{codAtividade}/conhecimentos")
    @Operation(summary = "Cria um conhecimento para uma atividade")
    public ResponseEntity<AtividadeOperacaoResponse> criarConhecimento(
            @PathVariable Long codAtividade, @Valid @RequestBody ConhecimentoDto conhecimentoDto) {
        var salvo = atividadeService.criarConhecimento(codAtividade, conhecimentoDto);
        
        // Buscar subprocesso e status
        Atividade atividade = atividadeRepo.findById(codAtividade)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Atividade", codAtividade));
        Long codSubprocesso = obterCodigoSubprocessoPorMapa(atividade.getMapa().getCodigo());
        SubprocessoStatusDto status = subprocessoService.obterStatus(codSubprocesso);
        AtividadeVisualizacaoDto atividadeVis = subprocessoService.listarAtividadesPorSubprocesso(codSubprocesso)
                .stream()
                .filter(a -> a.getCodigo().equals(codAtividade))
                .findFirst()
                .orElse(null);
        
        AtividadeOperacaoResponse response = AtividadeOperacaoResponse.builder()
                .atividade(atividadeVis)
                .subprocesso(status)
                .build();
        
        URI uri = URI.create("/api/atividades/%d/conhecimentos/%d".formatted(codAtividade, salvo.getCodigo()));
        return ResponseEntity.created(uri).body(response);
    }

    /**
     * Atualiza um conhecimento existente dentro de uma atividade.
     *
     * @param codAtividade O código da atividade pai.
     * @param codConhecimento O código do conhecimento a ser atualizado.
     * @param conhecimentoDto O DTO com os novos dados do conhecimento.
     * @return Um {@link ResponseEntity} com {@link AtividadeOperacaoResponse} contendo
     *     a atividade atualizada e o status do subprocesso.
     */
    @PostMapping("/{codAtividade}/conhecimentos/{codConhecimento}/atualizar")
    @Operation(summary = "Atualiza um conhecimento existente em uma atividade")
    public ResponseEntity<AtividadeOperacaoResponse> atualizarConhecimento(
            @PathVariable Long codAtividade,
            @PathVariable Long codConhecimento,
            @Valid @RequestBody ConhecimentoDto conhecimentoDto) {
        atividadeService.atualizarConhecimento(codAtividade, codConhecimento, conhecimentoDto);
        
        // Buscar subprocesso e status
        Atividade atividade = atividadeRepo.findById(codAtividade)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Atividade", codAtividade));
        Long codSubprocesso = obterCodigoSubprocessoPorMapa(atividade.getMapa().getCodigo());
        SubprocessoStatusDto status = subprocessoService.obterStatus(codSubprocesso);
        AtividadeVisualizacaoDto atividadeVis = subprocessoService.listarAtividadesPorSubprocesso(codSubprocesso)
                .stream()
                .filter(a -> a.getCodigo().equals(codAtividade))
                .findFirst()
                .orElse(null);
        
        AtividadeOperacaoResponse response = AtividadeOperacaoResponse.builder()
                .atividade(atividadeVis)
                .subprocesso(status)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Exclui um conhecimento de uma atividade.
     *
     * @param codAtividade O código da atividade pai.
     * @param codConhecimento O código do conhecimento a ser excluído.
     * @return Um {@link ResponseEntity} com {@link AtividadeOperacaoResponse} contendo
     *     a atividade atualizada e o status do subprocesso.
     */
    @PostMapping("/{codAtividade}/conhecimentos/{codConhecimento}/excluir")
    @Operation(summary = "Exclui um conhecimento de uma atividade")
    public ResponseEntity<AtividadeOperacaoResponse> excluirConhecimento(
            @PathVariable Long codAtividade, @PathVariable Long codConhecimento) {
        atividadeService.excluirConhecimento(codAtividade, codConhecimento);
        
        // Buscar subprocesso e status
        Atividade atividade = atividadeRepo.findById(codAtividade)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Atividade", codAtividade));
        Long codSubprocesso = obterCodigoSubprocessoPorMapa(atividade.getMapa().getCodigo());
        SubprocessoStatusDto status = subprocessoService.obterStatus(codSubprocesso);
        AtividadeVisualizacaoDto atividadeVis = subprocessoService.listarAtividadesPorSubprocesso(codSubprocesso)
                .stream()
                .filter(a -> a.getCodigo().equals(codAtividade))
                .findFirst()
                .orElse(null);
        
        AtividadeOperacaoResponse response = AtividadeOperacaoResponse.builder()
                .atividade(atividadeVis)
                .subprocesso(status)
                .build();
        
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
        Subprocesso subprocesso = subprocessoRepo
                .findByMapaCodigo(codMapa)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(
                        "Subprocesso não encontrado para o mapa com código %d".formatted(codMapa)));
        return subprocesso.getCodigo();
    }

}
