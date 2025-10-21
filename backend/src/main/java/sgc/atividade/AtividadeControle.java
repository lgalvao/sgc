package sgc.atividade;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import sgc.atividade.dto.AtividadeDto;
import sgc.conhecimento.dto.ConhecimentoDto;

import java.net.URI;
import java.util.List;

/**
 * Controlador REST para gerenciar Atividades e seus Conhecimentos associados.
 */
@RestController
@RequestMapping("/api/atividades")
@RequiredArgsConstructor
@Tag(name = "Atividades", description = "Endpoints para gerenciamento de atividades e seus conhecimentos associados")
/**
 * Controlador REST para gerenciar Atividades e seus Conhecimentos associados.
 */
public class AtividadeControle {
    private final AtividadeService atividadeService;

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
     * Busca e retorna uma atividade específica pelo seu ID.
     *
     * @param idAtividade O ID da atividade a ser buscada.
     * @return Um {@link ResponseEntity} contendo a {@link AtividadeDto} correspondente
     *         ou um status 404 Not Found se a atividade não for encontrada.
     */
    @GetMapping("/{idAtividade}")
    @Operation(summary = "Obtém uma atividade pelo ID")
    public ResponseEntity<AtividadeDto> obterPorId(@PathVariable Long idAtividade) {
        return ResponseEntity.ok(atividadeService.obterPorId(idAtividade));
    }

    /**
     * Cria uma nova atividade no sistema.
     * <p>
     * O método recebe os dados da atividade, sanitiza a descrição para remover HTML
     * e a persiste, associando-a ao usuário autenticado.
     *
     * @param atividadeDto O DTO contendo os dados da atividade a ser criada.
     * @param userDetails  Os detalhes do usuário autenticado, injetado pelo Spring Security.
     * @return Um {@link ResponseEntity} com status 201 Created, o URI da nova atividade
     *         no cabeçalho 'Location' e a atividade criada no corpo da resposta.
     */
    @PostMapping
    @Operation(summary = "Cria uma nova atividade")
    public ResponseEntity<AtividadeDto> criar(@Valid @RequestBody AtividadeDto atividadeDto, @AuthenticationPrincipal UserDetails userDetails) {
        var sanitizedAtividadeDto = atividadeDto.sanitize();
        var salvo = atividadeService.criar(sanitizedAtividadeDto, userDetails.getUsername());
        URI uri = URI.create("/api/atividades/%d".formatted(salvo.codigo()));
        return ResponseEntity.created(uri).body(salvo.sanitize());
    }


    /**
     * Atualiza os dados de uma atividade existente.
     *
     * @param codAtividade O ID da atividade a ser atualizada.
     * @param atividadeDto O DTO com os novos dados da atividade. A descrição será sanitizada.
     * @return Um {@link ResponseEntity} com status 200 OK e a {@link AtividadeDto} atualizada.
     */
    @PostMapping("/{codAtividade}/atualizar")
    @Operation(summary = "Atualiza uma atividade existente")
    public ResponseEntity<AtividadeDto> atualizar(@PathVariable Long codAtividade, @Valid @RequestBody AtividadeDto atividadeDto) {
        var sanitizedAtividadeDto = atividadeDto.sanitize();
        return ResponseEntity.ok(atividadeService.atualizar(codAtividade, sanitizedAtividadeDto));
    }

    /**
     * Exclui uma atividade do sistema.
     * <p>
     * Se a atividade não for encontrada, o serviço lançará uma exceção que
     * resultará em uma resposta 404 Not Found.
     *
     * @param codAtividade O ID da atividade a ser excluída.
     * @return Um {@link ResponseEntity} com status 204 No Content.
     */
    @PostMapping("/{codAtividade}/excluir")
    @Operation(summary = "Exclui uma atividade")
    public ResponseEntity<Void> excluir(@PathVariable Long codAtividade) {
        atividadeService.excluir(codAtividade);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lista todos os conhecimentos associados a uma atividade específica.
     *
     * @param atividadeId O ID da atividade pai.
     * @return Um {@link ResponseEntity} com status 200 OK e a lista de {@link ConhecimentoDto}.
     */
    @GetMapping("/{atividadeId}/conhecimentos")
    @Operation(summary = "Lista todos os conhecimentos de uma atividade")
    public ResponseEntity<List<ConhecimentoDto>> listarConhecimentos(@PathVariable Long atividadeId) {
        return ResponseEntity.ok(atividadeService.listarConhecimentos(atividadeId));
    }

    /**
     * Adiciona um novo conhecimento a uma atividade existente.
     *
     * @param atividadeId     O ID da atividade à qual o conhecimento será associado.
     * @param conhecimentoDto O DTO com os dados do conhecimento a ser criado.
     * @return Um {@link ResponseEntity} com status 201 Created, o URI do novo
     *         conhecimento e o {@link ConhecimentoDto} criado no corpo da resposta.
     */
    @PostMapping("/{atividadeId}/conhecimentos")
    @Operation(summary = "Cria um novo conhecimento para uma atividade")
    public ResponseEntity<ConhecimentoDto> criarConhecimento(@PathVariable Long atividadeId, @Valid @RequestBody ConhecimentoDto conhecimentoDto) {
        var sanitizedConhecimentoDto = conhecimentoDto.sanitize();
        var salvo = atividadeService.criarConhecimento(atividadeId, sanitizedConhecimentoDto);
        URI uri = URI.create("/api/atividades/%d/conhecimentos/%d".formatted(atividadeId, salvo.codigo()));
        return ResponseEntity.created(uri).body(salvo.sanitize());
    }

    /**
     * Atualiza um conhecimento existente dentro de uma atividade.
     *
     * @param codAtividade    O ID da atividade pai.
     * @param codConhecimento O ID do conhecimento a ser atualizado.
     * @param conhecimentoDto O DTO com os novos dados do conhecimento.
     * @return Um {@link ResponseEntity} com status 200 OK e o {@link ConhecimentoDto} atualizado.
     */
    @PostMapping("/{codAtividade}/conhecimentos/{codConhecimento}/atualizar")
    @Operation(summary = "Atualiza um conhecimento existente em uma atividade")
    public ResponseEntity<ConhecimentoDto> atualizarConhecimento(@PathVariable Long codAtividade, @PathVariable Long codConhecimento, @Valid @RequestBody ConhecimentoDto conhecimentoDto) {
        var sanitizedConhecimentoDto = conhecimentoDto.sanitize();
        return ResponseEntity.ok(atividadeService.atualizarConhecimento(codAtividade, codConhecimento, sanitizedConhecimentoDto));
    }

    /**
     * Exclui um conhecimento de uma atividade.
     *
     * @param codAtividade    O ID da atividade pai.
     * @param codConhecimento O ID do conhecimento a ser excluído.
     * @return Um {@link ResponseEntity} com status 204 No Content.
     */
    @PostMapping("/{codAtividade}/conhecimentos/{codConhecimento}/excluir")
    @Operation(summary = "Exclui um conhecimento de uma atividade")
    public ResponseEntity<Void> excluirConhecimento(@PathVariable Long codAtividade, @PathVariable Long codConhecimento) {
        atividadeService.excluirConhecimento(codAtividade, codConhecimento);
        return ResponseEntity.noContent().build();
    }
}