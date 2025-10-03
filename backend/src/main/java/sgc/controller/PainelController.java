package sgc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sgc.dto.AlertDTO;
import sgc.dto.ProcessoSummaryDTO;
import sgc.service.PainelService;

@RestController
@RequestMapping("/api/painel")
@RequiredArgsConstructor
public class PainelController {
    private final PainelService painelService;

    /**
     * GET /api/painel/processos?perfil={perfil}&unidade={id}&page=&size=
     * perfil é obrigatório e determina regras de visibilidade.
     */
    @GetMapping("/processos")
    public ResponseEntity<Page<ProcessoSummaryDTO>> listarProcessos(
            @RequestParam(name = "perfil") String perfil,
            @RequestParam(name = "unidade", required = false) Long unidade,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<ProcessoSummaryDTO> page = painelService.listarProcessos(perfil, unidade, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * GET /api/painel/alertas?usuarioTitulo={titulo}&unidade={id}&page=&size=
     * Se usuário/unidade não forem informados, retorna todos (uso administrativo/testes).
     */
    @GetMapping("/alertas")
    public ResponseEntity<Page<AlertDTO>> listarAlertas(
            @RequestParam(name = "usuarioTitulo", required = false) String usuarioTitulo,
            @RequestParam(name = "unidade", required = false) Long unidade,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<AlertDTO> page = painelService.listarAlertas(usuarioTitulo, unidade, pageable);
        return ResponseEntity.ok(page);
    }
}