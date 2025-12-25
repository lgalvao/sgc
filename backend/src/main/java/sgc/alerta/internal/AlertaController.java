package sgc.alerta.internal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sgc.alerta.AlertaService;
import sgc.alerta.api.AlertaDto;
import sgc.sgrh.api.model.Usuario;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
@Tag(name = "Alertas", description = "Endpoints para gerenciamento de alertas")
public class AlertaController {
    private final AlertaService alertaService;

    @GetMapping
    @Operation(summary = "Lista todos os alertas do usuário autenticado")
    public ResponseEntity<List<AlertaDto>> listarAlertas(@AuthenticationPrincipal Usuario usuario) {
        List<AlertaDto> alertas = alertaService.listarAlertasPorUsuario(String.valueOf(usuario.getTituloEleitoral()));
        return ResponseEntity.ok(alertas);
    }

    @PostMapping("/{codigo}/marcar-como-lido")
    @Operation(summary = "Marca um alerta como lido")
    public ResponseEntity<Map<String, String>> marcarComoLido(@PathVariable Long codigo, @AuthenticationPrincipal Usuario usuario) {
        alertaService.marcarComoLido(String.valueOf(usuario.getTituloEleitoral()), codigo);
        return ResponseEntity.ok(Map.of("message", "Alerta marcado como lido."));
    }
}
