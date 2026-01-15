package sgc.mapa;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sgc.mapa.dto.AtividadeDto;
import sgc.mapa.dto.ConhecimentoDto;
import sgc.mapa.dto.ResultadoOperacaoConhecimento;
import sgc.mapa.service.AtividadeFacade;
import sgc.subprocesso.dto.AtividadeOperacaoResponse;

import java.net.URI;
import java.util.List;

/**
 * Controlador REST para gerenciar Atividades e seus Conhecimentos associados.
 * 
 * <p><b>Padrão Arquitetural:</b> Este controller usa APENAS {@link AtividadeFacade}.
 * Nunca acessa AtividadeService ou ConhecimentoService diretamente, seguindo o padrão Facade
 * para manter a separação de responsabilidades e encapsulamento.
 */
@RestController
@RequestMapping("/api/atividades")
@RequiredArgsConstructor
@Tag(name = "Atividades", description = "Gerenciamento de atividades e seus conhecimentos")
public class AtividadeController {
    private final AtividadeFacade atividadeFacade;

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
        return ResponseEntity.ok(atividadeFacade.obterAtividadePorId(codAtividade));
    }

    /**
     * Cria uma nova atividade no sistema.
     *
     * @param atividadeDto O DTO contendo os dados da atividade a ser criada.
     * @return Um {@link ResponseEntity} com status 201 Created e {@link AtividadeOperacaoResponse}
     * contendo a atividade criada e o status do subprocesso.
     */
    @PostMapping
    @Operation(summary = "Cria uma atividade")
    public ResponseEntity<AtividadeOperacaoResponse> criar(@Valid @RequestBody AtividadeDto atividadeDto) {
        AtividadeOperacaoResponse resp = atividadeFacade.criarAtividade(atividadeDto);

        URI uri = URI.create("/api/atividades/%d".formatted(resp.getAtividade().getCodigo()));
        return ResponseEntity.created(uri).body(resp);
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
        AtividadeOperacaoResponse response = atividadeFacade.atualizarAtividade(codigo, atividadeDto);
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
        AtividadeOperacaoResponse response = atividadeFacade.excluirAtividade(codAtividade);
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
    public ResponseEntity<List<ConhecimentoDto>> listarConhecimentos(@PathVariable Long codAtividade) {
        return ResponseEntity.ok(atividadeFacade.listarConhecimentosPorAtividade(codAtividade));
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

        ResultadoOperacaoConhecimento resultado = atividadeFacade.criarConhecimento(codAtividade, conhecimentoDto);
        URI uri = URI.create("/api/atividades/%d/conhecimentos/%d".formatted(codAtividade, resultado.getNovoConhecimentoId()));
        return ResponseEntity.created(uri).body(resultado.getResponse());
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

        AtividadeOperacaoResponse response = atividadeFacade.atualizarConhecimento(codAtividade, codConhecimento, conhecimentoDto);
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
    public ResponseEntity<AtividadeOperacaoResponse> excluirConhecimento(
            @PathVariable Long codAtividade,
            @PathVariable Long codConhecimento) {

        AtividadeOperacaoResponse response = atividadeFacade.excluirConhecimento(codAtividade, codConhecimento);
        return ResponseEntity.ok(response);
    }
}
