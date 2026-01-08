package sgc.alerta;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import sgc.alerta.dto.AlertaDto;
import sgc.organizacao.model.Usuario;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
@Tag(name = "Alertas", description = "Gerenciamento de alertas para usuários")
public class AlertaController {
    private final AlertaService alertaService;

    @GetMapping
    @Operation(summary = "Lista todos os alertas do usuário autenticado")
    public ResponseEntity<List<AlertaDto>> listarAlertas(@AuthenticationPrincipal Object principal) {
        String usuarioTitulo = extractTituloUsuario(principal);
        List<AlertaDto> alertas = alertaService.listarAlertasPorUsuario(usuarioTitulo);

        return ResponseEntity.ok(alertas);
    }

    @GetMapping("/nao-lidos")
    @Operation(summary = "Lista alertas não lidos do usuário autenticado")
    public ResponseEntity<List<AlertaDto>> listarNaoLidos(@AuthenticationPrincipal Object principal) {
        String usuarioTitulo = extractTituloUsuario(principal);
        List<AlertaDto> alertas = alertaService.listarAlertasNaoLidos(usuarioTitulo);

        return ResponseEntity.ok(alertas);
    }

    @PostMapping("/marcar-como-lidos")
    @Operation(summary = "Marca múltiplos alertas como lidos")
    public ResponseEntity<Map<String, String>> marcarComoLidos(
            @RequestBody List<Long> codigos,
            @AuthenticationPrincipal Object principal) {

        String usuarioTitulo = extractTituloUsuario(principal);
        alertaService.marcarComoLidos(usuarioTitulo, codigos);
        return ResponseEntity.ok(Map.of("message", "Alertas marcados como lidos."));
    }

    private String extractTituloUsuario(Object principal) {
        return switch (principal) {
            case String string -> string;
            case Usuario usuario -> usuario.getTituloEleitoral();
            case UserDetails userDetails -> userDetails.getUsername();
            case null, default -> principal.toString();
        };

    }
}
