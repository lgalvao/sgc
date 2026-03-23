package sgc.alerta;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.*;
import lombok.*;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sgc.alerta.model.*;
import sgc.comum.model.ComumViews;
import com.fasterxml.jackson.annotation.JsonView;
import sgc.organizacao.model.*;

import java.util.*;

@RestController
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
@Tag(name = "Alertas", description = "Endpoints para gerenciamento de alertas")
public class AlertaController {
    private final AlertaFacade alertaFacade;

    @GetMapping
    @JsonView(ComumViews.Publica.class)
    @Operation(summary = "Lista todos os alertas do usuário autenticado usando o contexto do JWT")
    public ResponseEntity<List<Alerta>> listarAlertas(@AuthenticationPrincipal Usuario usuario) {
        List<Alerta> alertas = alertaFacade.alertasPorUsuario(
                usuario.getTituloEleitoral(), 
                usuario.getUnidadeAtivaCodigo(),
                usuario.getPerfilAtivo().name()
        );

        return ResponseEntity.ok(alertas);
    }

    @GetMapping("/nao-lidos")
    @JsonView(ComumViews.Publica.class)
    @Operation(summary = "Lista alertas não lidos do usuário autenticado usando o contexto do JWT")
    public ResponseEntity<List<Alerta>> listarNaoLidos(@AuthenticationPrincipal Usuario usuario) {
        List<Alerta> alertas = alertaFacade.listarNaoLidos(
                usuario.getTituloEleitoral(), 
                usuario.getUnidadeAtivaCodigo(),
                usuario.getPerfilAtivo().name()
        );

        return ResponseEntity.ok(alertas);
    }

    @PostMapping("/marcar-como-lidos")
    @Operation(summary = "Marca múltiplos alertas como lidos")
    public ResponseEntity<Map<String, String>> marcarComoLidos(
            @RequestBody List<Long> codigos,
            @AuthenticationPrincipal Usuario usuario) {
        
        alertaFacade.marcarComoLidos(usuario.getTituloEleitoral(), codigos);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Alertas marcados como lidos.");
        return ResponseEntity.ok(response);
    }
}
