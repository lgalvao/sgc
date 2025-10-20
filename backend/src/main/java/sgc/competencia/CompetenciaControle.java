package sgc.competencia;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sgc.competencia.dto.CompetenciaDto;
import sgc.competencia.modelo.CompetenciaAtividade;

import java.net.URI;
import java.util.List;

/**
 * Controlador REST para gerenciar Competências e seus relacionamentos com Atividades.
 */
@RestController
@RequestMapping("/api/competencias")
@RequiredArgsConstructor
@Tag(name = "Competências", description = "Endpoints para gerenciamento de competências e suas associações com atividades")
public class CompetenciaControle {
    private final CompetenciaService competenciaService;

    /**
     * Retorna uma lista com todas as competências cadastradas.
     *
     * @return Uma lista de {@link CompetenciaDto}.
     */
    @GetMapping
    @Operation(summary = "Lista todas as competências")
    public List<CompetenciaDto> listarCompetencias() {
        return competenciaService.listarCompetencias();
    }

    /**
     * Busca e retorna uma competência específica pelo seu ID.
     *
     * @param id O ID da competência a ser buscada.
     * @return Um {@link ResponseEntity} contendo a {@link CompetenciaDto} correspondente.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtém uma competência pelo ID")
    public ResponseEntity<CompetenciaDto> obterCompetencia(@PathVariable Long id) {
        return ResponseEntity.ok(competenciaService.obterCompetencia(id));
    }

    /**
     * Cria uma nova competência.
     *
     * @param competenciaDto O DTO com os dados da competência a ser criada.
     * @return Um {@link ResponseEntity} com status 201 Created, o URI da nova
     *         competência e o {@link CompetenciaDto} criado no corpo da resposta.
     */
    @PostMapping
    @Operation(summary = "Cria uma nova competência")
    public ResponseEntity<CompetenciaDto> criarCompetencia(@Valid @RequestBody CompetenciaDto competenciaDto) {
        var salvo = competenciaService.criarCompetencia(competenciaDto);
        URI uri = URI.create("/api/competencias/%d".formatted(salvo.getCodigo()));
        return ResponseEntity.created(uri).body(salvo.sanitize());
    }

    /**
     * Atualiza uma competência existente.
     *
     * @param id             O ID da competência a ser atualizada.
     * @param competenciaDto O DTO com os novos dados da competência.
     * @return Um {@link ResponseEntity} com status 200 OK e a {@link CompetenciaDto} atualizada.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma competência existente")
    public ResponseEntity<CompetenciaDto> atualizarCompetencia(@PathVariable Long id, @Valid @RequestBody CompetenciaDto competenciaDto) {
        return ResponseEntity.ok(competenciaService.atualizarCompetencia(id, competenciaDto));
    }

    /**
     * Exclui uma competência.
     *
     * @param id O ID da competência a ser excluída.
     * @return Um {@link ResponseEntity} com status 204 No Content.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma competência")
    public ResponseEntity<Void> excluirCompetencia(@PathVariable Long id) {
        competenciaService.excluirCompetencia(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lista todas as atividades que estão vinculadas a uma competência específica.
     *
     * @param idCompetencia O ID da competência.
     * @return Um {@link ResponseEntity} com a lista de vínculos {@link CompetenciaAtividade}.
     */
    @GetMapping("/{idCompetencia}/atividades")
    @Operation(summary = "Lista todas as atividades vinculadas a uma competência")
    public ResponseEntity<List<CompetenciaAtividade>> listarAtividadesVinculadas(@PathVariable Long idCompetencia) {
        return ResponseEntity.ok(competenciaService.listarAtividadesVinculadas(idCompetencia));
    }

    /**
     * Cria um novo vínculo entre uma competência e uma atividade.
     *
     * @param idCompetencia O ID da competência.
     * @param requisicao    O corpo da requisição contendo o ID da atividade a ser vinculada.
     * @return Um {@link ResponseEntity} com status 201 Created e o
     *         vínculo {@link CompetenciaAtividade} criado no corpo da resposta.
     */
    @PostMapping("/{idCompetencia}/atividades")
    @Operation(summary = "Vincula uma atividade a uma competência")
    public ResponseEntity<?> vincularAtividade(@PathVariable Long idCompetencia, @Valid @RequestBody CompetenciaControle.VinculoAtividadeReq requisicao) {
        var salvo = competenciaService.vincularAtividade(idCompetencia, requisicao.getIdAtividade());
        URI uri = URI.create("/api/competencias/%d/atividades/%d".formatted(idCompetencia, requisicao.getIdAtividade()));
        return ResponseEntity.created(uri).body(salvo);
    }

    /**
     * Remove o vínculo entre uma competência e uma atividade.
     *
     * @param idCompetencia O ID da competência.
     * @param idAtividade   O ID da atividade.
     * @return Um {@link ResponseEntity} com status 204 No Content.
     */
    @DeleteMapping("/{idCompetencia}/atividades/{idAtividade}")
    @Operation(summary = "Desvincula uma atividade de uma competência")
    public ResponseEntity<?> desvincularAtividade(@PathVariable Long idCompetencia, @PathVariable Long idAtividade) {
        competenciaService.desvincularAtividade(idCompetencia, idAtividade);
        return ResponseEntity.noContent().build();
    }

    @Setter
    @Getter
    public static class VinculoAtividadeReq {
        private Long idAtividade;
    }
}