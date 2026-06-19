package sgc.processo.painel;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sgc.alerta.AlertaDtoMapper;
import sgc.alerta.dto.AlertaDto;
import sgc.alerta.model.Alerta;
import sgc.organizacao.ContextoUsuarioAutenticado;
import sgc.organizacao.UsuarioAplicacaoService;
import sgc.processo.dto.ProcessoResumoDto;
import sgc.processo.painel.dto.PainelBootstrapDto;

import java.util.List;

@RestController
@RequestMapping("/api/painel")
@RequiredArgsConstructor
@Tag(name = "Painel", description = "Endpoints para o painel de controle (dashboard)")
@PreAuthorize("isAuthenticated()")
public class PainelController {
    private final PainelService painelService;
    private final UsuarioAplicacaoService usuarioAplicacaoService;
    private final AlertaDtoMapper alertaDtoMapper;

    @GetMapping("/bootstrap")
    @Operation(summary = "Obtém todos os dados iniciais do painel (processos e alertas) em uma única chamada")
    public ResponseEntity<PainelBootstrapDto> obterBootstrap() {
        ContextoUsuarioAutenticado contextoUsuario = usuarioAplicacaoService.contextoAutenticado();
        return ResponseEntity.ok(painelService.obterBootstrap(contextoUsuario));
    }

    /**
     * Lista os processos a serem exibidos no painel do usuário.
     */
    @GetMapping("/processos")
    @Operation(summary = "Lista processos para o painel com base no contexto do Token JWT")
    public ResponseEntity<Page<ProcessoResumoDto>> listarProcessos(@PageableDefault(size = 20) Pageable pageable) {
        ContextoUsuarioAutenticado contextoUsuario = usuarioAplicacaoService.contextoAutenticado();
        Page<ProcessoResumoDto> page = painelService.listarProcessos(contextoUsuario, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Lista os alertas a serem exibidos no painel.
     * A visibilidade segue a regra do CDU-02 (pessoal ou unidade ativa, sem recursividade).
     */
    @GetMapping("/alertas")
    @Operation(summary = "Lista alertas para o painel com base no contexto do Token JWT")
    public ResponseEntity<Page<AlertaDto>> listarAlertas(@PageableDefault(size = 20) Pageable pageable) {
        ContextoUsuarioAutenticado contextoUsuario = usuarioAplicacaoService.contextoAutenticado();
        Page<Alerta> page = painelService.listarAlertas(contextoUsuario, pageable);
        return ResponseEntity.ok(page.map(alertaDtoMapper::paraAlertaDto));
    }

    /**
     * Marca alertas visualizados como lidos.
     * Chamado pelo frontend de forma assíncrona após exibir a lista de alertas.
     */
    @PostMapping("/alertas/marcar-lidos")
    @Operation(summary = "Marca alertas como lidos para o usuário autenticado")
    public ResponseEntity<Void> marcarAlertasLidos(@RequestBody List<Long> codigos) {
        ContextoUsuarioAutenticado contextoUsuario = usuarioAplicacaoService.contextoAutenticado();
        painelService.marcarAlertasLidos(contextoUsuario, codigos);
        return ResponseEntity.noContent().build();
    }
}
