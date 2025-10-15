package sgc.alerta;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sgc.comum.erros.ErroDominioNaoEncontrado;

import java.util.Map;

@RestController
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
public class AlertaControle {
    private final AlertaService alertaService;

    /**
     * Marca um alerta como lido para o usuário atual.
     * CDU-02
     */
    @PostMapping("/{id}/marcar-como-lido")
    public ResponseEntity<?> marcarComoLido(@PathVariable Long id) {
        // O título (matrícula) do usuário viria do token JWT em um ambiente de produção
        String usuarioTitulo = "USUARIO_ATUAL"; // Exemplo
        alertaService.marcarComoLido(usuarioTitulo, id);
        return ResponseEntity.ok(Map.of("message", "Alerta marcado como lido."));
    }
}