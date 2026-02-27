package sgc.alerta;

import com.fasterxml.jackson.annotation.*;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.*;
import lombok.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.*;
import org.springframework.security.core.annotation.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.web.bind.annotation.*;
import sgc.alerta.model.*;
import sgc.comum.model.*;
import sgc.organizacao.model.*;

import java.util.*;

@RestController
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
@Tag(name = "Alertas", description = "Gerenciamento de alertas para usuários")
@PreAuthorize("isAuthenticated()")
public class AlertaController {
    private final AlertaFacade alertaFacade;

    @GetMapping
    @JsonView(ComumViews.Publica.class)
    @Operation(summary = "Lista todos os alertas do usuário autenticado")
    public ResponseEntity<List<Alerta>> listarAlertas(@AuthenticationPrincipal Object principal) {
        String usuarioTitulo = extractTituloUsuario(principal);
        List<Alerta> alertas = alertaFacade.alertasPorUsuario(usuarioTitulo);

        return ResponseEntity.ok(alertas);
    }

    @GetMapping("/nao-lidos")
    @JsonView(ComumViews.Publica.class)
    @Operation(summary = "Lista alertas não lidos do usuário autenticado")
    public ResponseEntity<List<Alerta>> listarNaoLidos(@AuthenticationPrincipal Object principal) {
        String usuarioTitulo = extractTituloUsuario(principal);
        List<Alerta> alertas = alertaFacade.listarNaoLidos(usuarioTitulo);

        return ResponseEntity.ok(alertas);
    }

    @PostMapping("/marcar-como-lidos")
    @Operation(summary = "Marca múltiplos alertas como lidos")
    public ResponseEntity<Map<String, String>> marcarComoLidos(
            @RequestBody List<Long> codigos,
            @AuthenticationPrincipal Object principal) {

        String usuarioTitulo = extractTituloUsuario(principal);
        alertaFacade.marcarComoLidos(usuarioTitulo, codigos);

        return ResponseEntity.ok(Map.of("message", "Alertas marcados como lidos."));
    }

    String extractTituloUsuario(Object principal) {
        return switch (principal) {
            case String string -> string;
            case Usuario usuario -> usuario.getTituloEleitoral();
            case UserDetails userDetails -> userDetails.getUsername();
            default -> principal.toString();
        };
    }
}
