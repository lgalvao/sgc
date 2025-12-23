package sgc.painel.internal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sgc.alerta.api.AlertaDto;
import sgc.painel.PainelService;
import sgc.processo.dto.ProcessoResumoDto;
import sgc.sgrh.internal.model.Perfil;

@RestController
@RequestMapping("/api/painel")
@RequiredArgsConstructor
@Tag(name = "Painel", description = "Endpoints para o painel de controle (dashboard)")
public class PainelController {
    private final PainelService painelService;

    /**
     * Lista os processos a serem exibidos no painel do usuário.
     *
     * <p>A visibilidade dos processos é determinada pelo perfil do usuário e, opcionalmente, pela
     * unidade selecionada.
     *
     * @param perfil   O perfil do usuário (e.g., 'ADMIN', 'GESTOR'), que define as regras de acesso.
     * @param unidade  O código da unidade para filtrar os processos (opcional).
     * @param pageable As informações de paginação.
     * @return Um {@link ResponseEntity} contendo uma página {@link Page} de {@link
     * ProcessoResumoDto}.
     */
    @GetMapping("/processos")
    @Operation(summary = "Lista processos para o painel com base no perfil e unidade")
    public ResponseEntity<Page<ProcessoResumoDto>> listarProcessos(
            @RequestParam(name = "perfil") Perfil perfil,
            @RequestParam(name = "unidade", required = false) Long unidade,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProcessoResumoDto> page = painelService.listarProcessos(perfil, unidade, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Lista os alertas a serem exibidos no painel.
     *
     * <p>Os alertas podem ser filtrados pelo título de eleitor do usuário ou pelo código da
     * unidade. Se nenhum filtro for fornecido, todos os alertas são retornados (comportamento
     * destinado a administradores ou testes).
     *
     * @param usuarioTitulo Título de eleitor do usuário para filtrar os alertas (opcional).
     * @param unidade       código da unidade para filtrar os alertas (opcional).
     * @param pageable      As informações de paginação.
     * @return Um {@link ResponseEntity} contendo uma página {@link Page} de {@link AlertaDto}.
     */
    @GetMapping("/alertas")
    @Operation(summary = "Lista alertas para o painel com base no usuário e unidade")
    public ResponseEntity<Page<AlertaDto>> listarAlertas(
            @RequestParam(name = "usuarioTitulo", required = false) String usuarioTitulo,
            @RequestParam(name = "unidade", required = false) Long unidade,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<AlertaDto> page = painelService.listarAlertas(usuarioTitulo, unidade, pageable);
        return ResponseEntity.ok(page);
    }
}
