package sgc.alerta;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
@Tag(name = "Alertas", description = "Endpoints para gerenciamento de alertas")
public class AlertaControle {
    private final AlertaService alertaService;

    /**
     * Marca um alerta como lido para o usuário atual.
     * CDU-02
     */
    @PostMapping("/{id}/marcar-como-lido")
    @Operation(summary = "Marca um alerta como lido (CDU-02)")
    public ResponseEntity<?> marcarComoLido(@PathVariable Long id) {
        // O título (matrícula) do usuário viria do token JWT em um ambiente de produção
        String usuarioTitulo = "USUARIO_ATUAL"; // Exemplo
        alertaService.marcarComoLido(usuarioTitulo, id);
        return ResponseEntity.ok(Map.of("message", "Alerta marcado como lido."));
    }
}