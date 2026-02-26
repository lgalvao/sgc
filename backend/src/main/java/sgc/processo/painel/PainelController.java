package sgc.processo.painel;


import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.*;
import lombok.*;
import org.springframework.data.domain.*;
import org.springframework.data.web.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.*;
import org.springframework.web.bind.annotation.*;
import sgc.alerta.model.*;
import sgc.organizacao.model.*;
import sgc.processo.dto.*;

@RestController
@RequestMapping("/api/painel")
@RequiredArgsConstructor
@Tag(name = "Painel", description = "Endpoints para o painel de controle (dashboard)")
public class PainelController {
    private final PainelFacade painelFacade;

    /**
     * Lista os processos a serem exibidos no painel do usuário.
     *
     * <p>A visibilidade dos processos é determinada pelo perfil do usuário e pela
     * unidade selecionada.
     */
    @GetMapping("/processos")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lista processos para o painel com base no perfil e unidade")
    public ResponseEntity<Page<ProcessoResumoDto>> listarProcessos(
            @RequestParam(name = "perfil") Perfil perfil,
            @RequestParam(name = "unidade") Long unidade,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<ProcessoResumoDto> page = painelFacade.listarProcessos(perfil, unidade, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Lista os alertas a serem exibidos no painel.
     *
     * <p>Os alertas podem ser filtrados pelo título de eleitor do usuário ou pelo código da
     * unidade.
     */
    @GetMapping("/alertas")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lista alertas para o painel com base no usuário e unidade")
    public ResponseEntity<Page<Alerta>> listarAlertas(
            @RequestParam(name = "usuarioTitulo", required = false) String usuarioTitulo,
            @RequestParam(name = "unidade") Long unidade,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<Alerta> page = painelFacade.listarAlertas(usuarioTitulo, unidade, pageable);
        return ResponseEntity.ok(page);
    }
}
