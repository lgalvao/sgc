package sgc.processo.painel;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.*;
import lombok.*;
import org.springframework.data.domain.*;
import org.springframework.data.web.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.*;
import org.springframework.security.core.annotation.*;
import org.springframework.web.bind.annotation.*;
import sgc.alerta.model.*;
import sgc.organizacao.model.*;
import sgc.processo.dto.*;

@RestController
@RequestMapping("/api/painel")
@RequiredArgsConstructor
@Tag(name = "Painel", description = "Endpoints para o painel de controle (dashboard)")
@PreAuthorize("isAuthenticated()")
public class PainelController {
    private final PainelFacade painelFacade;

    /**
     * Lista os processos a serem exibidos no painel do usuário.
     */
    @GetMapping("/processos")
    @Operation(summary = "Lista processos para o painel com base no contexto do Token JWT")
    public ResponseEntity<Page<ProcessoResumoDto>> listarProcessos(
            @AuthenticationPrincipal Usuario usuario,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<ProcessoResumoDto> page = painelFacade.listarProcessos(
                usuario.getPerfilAtivo(), 
                usuario.getUnidadeAtivaCodigo(), 
                pageable
        );
        return ResponseEntity.ok(page);
    }

    /**
     * Lista os alertas a serem exibidos no painel.
     * A visibilidade segue a regra do CDU-02 (pessoal ou unidade ativa, sem recursividade).
     */
    @GetMapping("/alertas")
    @Operation(summary = "Lista alertas para o painel com base no contexto do Token JWT")
    public ResponseEntity<Page<Alerta>> listarAlertas(
            @AuthenticationPrincipal Usuario usuario,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<Alerta> page = painelFacade.listarAlertas(
                usuario.getTituloEleitoral(), 
                usuario.getUnidadeAtivaCodigo(), 
                usuario.getPerfilAtivo().name(), 
                pageable
        );
        return ResponseEntity.ok(page);
    }
}
