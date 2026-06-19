package sgc.mapa;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sgc.comum.erros.ErroInconsistenciaInterna;
import sgc.mapa.dto.*;
import sgc.subprocesso.dto.AtividadeOperacaoResponse;

import java.net.URI;
import java.util.List;

/**
 * Gerencia atividades e seus Conhecimentos associados.
 */
@RestController
@RequestMapping("/api/atividades")
@RequiredArgsConstructor
@Tag(name = "Atividades", description = "Gerenciamento de atividades e seus conhecimentos")
@PreAuthorize("isAuthenticated()")
public class AtividadeController {
    private final AtividadeService atividadeService;

    /**
     * Busca e retorna uma atividade específica pelo seu código.
     */
    @GetMapping("/{codAtividade}")
    @PreAuthorize("hasPermission(#codAtividade, 'Atividade', 'VISUALIZAR_SUBPROCESSO')")
    @Operation(summary = "Obtém uma atividade pelo código")
    public ResponseEntity<AtividadeDto> obterPorCodigo(@PathVariable Long codAtividade) {
        return ResponseEntity.ok(atividadeService.obterAtividadePorCodigo(codAtividade));
    }

    /**
     * Lista todos os conhecimentos associados a uma atividade específica.
     */
    @GetMapping("/{codAtividade}/conhecimentos")
    @PreAuthorize("hasPermission(#codAtividade, 'Atividade', 'VISUALIZAR_SUBPROCESSO')")
    @Operation(summary = "Lista todos os conhecimentos de uma atividade")
    public ResponseEntity<List<ConhecimentoResumoDto>> listarConhecimentos(@PathVariable Long codAtividade) {
        return ResponseEntity.ok(atividadeService.listarConhecimentosPorAtividade(codAtividade));
    }

    /**
     * Cria uma nova atividade no sistema.
     */
    @PostMapping
    @PreAuthorize("hasRole('CHEFE')")
    @Operation(summary = "Cria uma atividade")
    public ResponseEntity<AtividadeOperacaoResponse> criar(@Valid @RequestBody CriarAtividadeRequest request) {
        AtividadeOperacaoResponse resp = atividadeService.criarAtividade(request);
        AtividadeDto atividadeCriada = resp.atividade();
        if (atividadeCriada == null) {
            throw new ErroInconsistenciaInterna("Resposta de criação de atividade sem código gerado");
        }
        URI uri = URI.create("/api/atividades/%d".formatted(atividadeCriada.codigo()));
        return ResponseEntity.created(uri).body(resp);
    }

    /**
     * Atualiza os dados de uma atividade existente.
     */
    @PostMapping("/{codigo}/atualizar")
    @PreAuthorize("hasRole('CHEFE')")
    @Operation(summary = "Atualiza atividade existente")
    public ResponseEntity<AtividadeOperacaoResponse> atualizar(@PathVariable Long codigo,
                                                               @RequestBody @Valid AtualizarAtividadeRequest request) {
        AtividadeOperacaoResponse response = atividadeService.atualizarAtividade(codigo, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Exclui uma atividade do sistema.
     */
    @PostMapping("/{codAtividade}/excluir")
    @PreAuthorize("hasRole('CHEFE')")
    @Operation(summary = "Exclui uma atividade")
    public ResponseEntity<AtividadeOperacaoResponse> excluir(@PathVariable Long codAtividade) {
        AtividadeOperacaoResponse response = atividadeService.excluirAtividade(codAtividade);
        return ResponseEntity.ok(response);
    }

    /**
     * Adiciona um conhecimento a uma atividade existente.
     */
    @PostMapping("/{codAtividade}/conhecimentos")
    @PreAuthorize("hasRole('CHEFE')")
    @Operation(summary = "Cria um conhecimento para uma atividade")
    public ResponseEntity<AtividadeOperacaoResponse> criarConhecimento(
            @PathVariable Long codAtividade,
            @Valid @RequestBody CriarConhecimentoRequest request) {

        ResultadoOperacaoConhecimento resultado = atividadeService.criarConhecimento(codAtividade, request);
        URI uri = URI.create("/api/atividades/%d/conhecimentos/%d".formatted(codAtividade, resultado.novoConhecimentoCodigo()));
        return ResponseEntity.created(uri).body(resultado.response());
    }

    /**
     * Atualiza um conhecimento existente dentro de uma atividade.
     */
    @PostMapping("/{codAtividade}/conhecimentos/{codConhecimento}/atualizar")
    @PreAuthorize("hasRole('CHEFE')")
    @Operation(summary = "Atualiza um conhecimento existente em uma atividade")
    public ResponseEntity<AtividadeOperacaoResponse> atualizarConhecimento(
            @PathVariable Long codAtividade,
            @PathVariable Long codConhecimento,
            @Valid @RequestBody AtualizarConhecimentoRequest request) {

        AtividadeOperacaoResponse response = atividadeService.atualizarConhecimento(
                codAtividade, codConhecimento, request
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Exclui um conhecimento de uma atividade.
     */
    @PostMapping("/{codAtividade}/conhecimentos/{codConhecimento}/excluir")
    @PreAuthorize("hasRole('CHEFE')")
    @Operation(summary = "Exclui um conhecimento de uma atividade")
    public ResponseEntity<AtividadeOperacaoResponse> excluirConhecimento(
            @PathVariable Long codAtividade,
            @PathVariable Long codConhecimento) {

        AtividadeOperacaoResponse response = atividadeService.excluirConhecimento(codAtividade, codConhecimento);
        return ResponseEntity.ok(response);
    }
}
