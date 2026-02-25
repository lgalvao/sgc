package sgc.mapa;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sgc.mapa.dto.*;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Conhecimento;
import sgc.subprocesso.dto.AtividadeOperacaoResponse;

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
    private final AtividadeFacade atividadeFacade;

    /**
     * Busca e retorna uma atividade específica pelo seu código.
     *
     * @param codAtividade O código da atividade a ser buscada.
     * @return Um {@link ResponseEntity} contendo a {@link Atividade}
     * correspondente ou um status
     * 404 Not Found se a atividade não for encontrada.
     */
    @GetMapping("/{codAtividade}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtém uma atividade pelo código")
    public ResponseEntity<Atividade> obterPorId(@PathVariable Long codAtividade) {
        return ResponseEntity.ok(atividadeFacade.obterAtividadePorId(codAtividade));
    }

    /**
     * Cria uma nova atividade no sistema.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'CHEFE')")
    @Operation(summary = "Cria uma atividade")
    public ResponseEntity<AtividadeOperacaoResponse> criar(@Valid @RequestBody CriarAtividadeRequest request) {
        AtividadeOperacaoResponse resp = atividadeFacade.criarAtividade(request);
        URI uri = URI.create("/api/atividades/%d".formatted(resp.atividade().codigo()));
        return ResponseEntity.created(uri).body(resp);
    }

    /**
     * Atualiza os dados de uma atividade existente.
     */
    @PostMapping("/{codigo}/atualizar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'CHEFE')")
    @Operation(summary = "Atualiza atividade existente")
    public ResponseEntity<AtividadeOperacaoResponse> atualizar(@PathVariable Long codigo,
                                                               @RequestBody @Valid AtualizarAtividadeRequest request) {
        AtividadeOperacaoResponse response = atividadeFacade.atualizarAtividade(codigo, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Exclui uma atividade do sistema.
     */
    @PostMapping("/{codAtividade}/excluir")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'CHEFE')")
    @Operation(summary = "Exclui uma atividade")
    public ResponseEntity<AtividadeOperacaoResponse> excluir(@PathVariable Long codAtividade) {
        AtividadeOperacaoResponse response = atividadeFacade.excluirAtividade(codAtividade);
        return ResponseEntity.ok(response);
    }

    /**
     * Lista todos os conhecimentos associados a uma atividade específica.
     *
     * @param codAtividade O código da atividade pai.
     * @return Um {@link ResponseEntity} com status 200 OK e a lista de
     * {@link Conhecimento}.
     */
    @GetMapping("/{codAtividade}/conhecimentos")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lista todos os conhecimentos de uma atividade")
    public ResponseEntity<List<Conhecimento>> listarConhecimentos(@PathVariable Long codAtividade) {
        return ResponseEntity.ok(atividadeFacade.listarConhecimentosPorAtividade(codAtividade));
    }

    /**
     * Adiciona um novo conhecimento a uma atividade existente.
     */
    @PostMapping("/{codAtividade}/conhecimentos")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'CHEFE')")
    @Operation(summary = "Cria um conhecimento para uma atividade")
    public ResponseEntity<AtividadeOperacaoResponse> criarConhecimento(
            @PathVariable Long codAtividade,
            @Valid @RequestBody CriarConhecimentoRequest request) {

        ResultadoOperacaoConhecimento resultado = atividadeFacade.criarConhecimento(codAtividade, request);
        URI uri = URI.create("/api/atividades/%d/conhecimentos/%d".formatted(codAtividade, resultado.novoConhecimentoId()));
        return ResponseEntity.created(uri).body(resultado.response());
    }

    /**
     * Atualiza um conhecimento existente dentro de uma atividade.
     */
    @PostMapping("/{codAtividade}/conhecimentos/{codConhecimento}/atualizar")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'CHEFE')")
    @Operation(summary = "Atualiza um conhecimento existente em uma atividade")
    public ResponseEntity<AtividadeOperacaoResponse> atualizarConhecimento(
            @PathVariable Long codAtividade,
            @PathVariable Long codConhecimento,
            @Valid @RequestBody AtualizarConhecimentoRequest request) {

        AtividadeOperacaoResponse response = atividadeFacade.atualizarConhecimento(
                codAtividade, codConhecimento, request
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Exclui um conhecimento de uma atividade.
     */
    @PostMapping("/{codAtividade}/conhecimentos/{codConhecimento}/excluir")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'CHEFE')")
    @Operation(summary = "Exclui um conhecimento de uma atividade")
    public ResponseEntity<AtividadeOperacaoResponse> excluirConhecimento(
            @PathVariable Long codAtividade,
            @PathVariable Long codConhecimento) {

        AtividadeOperacaoResponse response = atividadeFacade.excluirConhecimento(codAtividade, codConhecimento);
        return ResponseEntity.ok(response);
    }
}
