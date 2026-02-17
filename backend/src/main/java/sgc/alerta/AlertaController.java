package sgc.alerta;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import sgc.alerta.model.Alerta;
import sgc.comum.model.ComumViews;
import sgc.organizacao.model.Usuario;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
@Tag(name = "Alertas", description = "Gerenciamento de alertas para usuários")
public class AlertaController {
    private final AlertaFacade alertaFacade;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @JsonView(ComumViews.Publica.class)
    @Operation(summary = "Lista todos os alertas do usuário autenticado")
    public ResponseEntity<List<Alerta>> listarAlertas(@AuthenticationPrincipal Object principal) {
        String usuarioTitulo = extractTituloUsuario(principal);
        List<Alerta> alertas = alertaFacade.listarAlertasPorUsuario(usuarioTitulo);

        return ResponseEntity.ok(alertas);
    }

    @GetMapping("/nao-lidos")
    @PreAuthorize("isAuthenticated()")
    @JsonView(ComumViews.Publica.class)
    @Operation(summary = "Lista alertas não lidos do usuário autenticado")
    public ResponseEntity<List<Alerta>> listarNaoLidos(@AuthenticationPrincipal Object principal) {
        String usuarioTitulo = extractTituloUsuario(principal);
        List<Alerta> alertas = alertaFacade.listarAlertasNaoLidos(usuarioTitulo);

        return ResponseEntity.ok(alertas);
    }

    @PostMapping("/marcar-como-lidos")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Marca múltiplos alertas como lidos")
    public ResponseEntity<Map<String, String>> marcarComoLidos(
            @RequestBody List<Long> codigos,
            @AuthenticationPrincipal Object principal) {

        String usuarioTitulo = extractTituloUsuario(principal);
        alertaFacade.marcarComoLidos(usuarioTitulo, codigos);
        return ResponseEntity.ok(Map.of("message", "Alertas marcados como lidos."));
    }

    private String extractTituloUsuario(Object principal) {
        return switch (principal) {
            case String string -> string;
            case Usuario usuario -> usuario.getTituloEleitoral();
            case UserDetails userDetails -> userDetails.getUsername();
            default -> principal.toString();
        };

    }
}
