package sgc.comum;

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
import sgc.alerta.dto.AlertaDto;
import sgc.processo.dto.ProcessoResumoDto;

@RestController
@RequestMapping("/api/painel")
@RequiredArgsConstructor
@Tag(name = "Painel", description = "Endpoints para o painel de controle (dashboard)")
public class PainelControle {
    private final PainelService painelService;

    /**
     * GET /api/painel/processos?perfil={perfil}&unidade={id}&page=&size=
     * perfil é obrigatório e determina regras de visibilidade.
     */
    @GetMapping("/processos")
    @Operation(summary = "Lista processos para o painel com base no perfil e unidade")
    public ResponseEntity<Page<ProcessoResumoDto>> listarProcessos(
            @RequestParam(name = "perfil") String perfil,
            @RequestParam(name = "unidade", required = false) Long unidade,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<ProcessoResumoDto> page = painelService.listarProcessos(perfil, unidade, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * GET /api/painel/alertas?usuarioTitulo={titulo}&unidade={id}&page=&size=
     * Se usuário/unidade não forem informados, retorna todos (uso administrativo/testes).
     */
    @GetMapping("/alertas")
    @Operation(summary = "Lista alertas para o painel com base no usuário e unidade")
    public ResponseEntity<Page<AlertaDto>> listarAlertas(
            @RequestParam(name = "usuarioTitulo", required = false) String usuarioTitulo,
            @RequestParam(name = "unidade", required = false) Long unidade,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<AlertaDto> page = painelService.listarAlertas(usuarioTitulo, unidade, pageable);
        return ResponseEntity.ok(page);
    }
}