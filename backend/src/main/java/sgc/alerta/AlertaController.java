package sgc.alerta;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sgc.alerta.dto.AlertaDto;
import sgc.sgrh.model.Usuario;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gerenciar alertas.
 * <p>
 * Este controller expõe apenas operações de leitura de alertas pelo usuário.
 * A criação de alertas é feita internamente pelo sistema através de um listener
 * de eventos de domínio (EventoProcessoListener), garantindo que alertas sejam
 * gerados automaticamente e consistentemente ao longo do ciclo de vida dos processos.
 * <p>
 * Não há endpoint POST para criação de alertas neste controller porque:
 * - Alertas são criados automaticamente em resposta a eventos do sistema
 * - O cliente (frontend) não deve ter a responsabilidade de criar alertas
 * - Isso garante integridade e consistência dos dados de alertas
 */
@RestController
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
@Tag(name = "Alertas", description = "Endpoints para gerenciamento de alertas")
public class AlertaController {
    private final AlertaService alertaService;

    /**
     * Lista todos os alertas para o usuário autenticado.
     *
     * @param usuario O usuário autenticado.
     * @return Uma lista de {@link AlertaDto}.
     */
    @GetMapping
    @Operation(summary = "Lista todos os alertas do usuário autenticado")
    public ResponseEntity<List<AlertaDto>> listarAlertas(@AuthenticationPrincipal Usuario usuario) {
        List<AlertaDto> alertas = alertaService.listarAlertasPorUsuario(String.valueOf(usuario.getTituloEleitoral()));
        return ResponseEntity.ok(alertas);
    }

    /**
     * Marca um alerta específico como lido para o usuário autenticado.
     * <p>
     * Este método corresponde ao CDU-02: Visualizar alertas. A ação de marcar como
     * lido é uma parte fundamental deste caso de uso.
     *
     * @param codigo O identificador único do alerta a ser marcado como lido.
     * @return Uma resposta HTTP 200 OK com uma mensagem de confirmação.
     */
    @PostMapping("/{codigo}/marcar-como-lido")
    @Operation(summary = "Marca um alerta como lido")
    public ResponseEntity<Map<String, String>> marcarComoLido(@PathVariable Long codigo, @AuthenticationPrincipal Usuario usuario) {
        alertaService.marcarComoLido(String.valueOf(usuario.getTituloEleitoral()), codigo);
        return ResponseEntity.ok(Map.of("message", "Alerta marcado como lido."));
    }
}