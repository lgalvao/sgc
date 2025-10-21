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
/**
 * Controlador REST para gerenciar alertas.
 */
public class AlertaControle {
    private final AlertaService alertaService;

    /**
     * Marca um alerta específico como lido para o usuário autenticado.
     * <p>
     * Este método corresponde ao CDU-02: Visualizar alertas. A ação de marcar como
     * lido é uma parte fundamental deste caso de uso.
     *
     * @param id O identificador único do alerta a ser marcado como lido.
     * @return Uma resposta HTTP 200 OK com uma mensagem de confirmação.
     */
    @PostMapping("/{id}/marcar-como-lido")
    @Operation(summary = "Marca um alerta como lido (CDU-02)")
    public ResponseEntity<Map<String, String>> marcarComoLido(@PathVariable Long id) {
        String usuarioTitulo = "USUARIO_ATUAL"; // Exemplo
        alertaService.marcarComoLido(usuarioTitulo, id);
        return ResponseEntity.ok(Map.of("message", "Alerta marcado como lido."));
    }
}