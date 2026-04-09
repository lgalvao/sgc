package sgc.alerta;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.*;
import lombok.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import sgc.alerta.dto.*;
import sgc.alerta.model.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;

import java.util.*;

@RestController
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
@Tag(name = "Alertas", description = "Endpoints para gerenciamento de alertas")
public class AlertaController {
    private final AlertaFacade alertaFacade;
    private final UsuarioFacade usuarioFacade;

    @GetMapping
    @Operation(summary = "Lista todos os alertas do usuário autenticado usando o contexto do JWT")
    public ResponseEntity<List<AlertaDto>> listarAlertas() {
        ContextoUsuarioAutenticado contextoUsuario = usuarioFacade.contextoAutenticado();
        List<Alerta> alertas = alertaFacade.alertasPorUsuario(contextoUsuario);

        return ResponseEntity.ok(alertas.stream().map(AlertaDto::fromEntity).toList());
    }

    @GetMapping("/nao-lidos")
    @Operation(summary = "Lista alertas não lidos do usuário autenticado usando o contexto do JWT")
    public ResponseEntity<List<AlertaDto>> listarNaoLidos() {
        ContextoUsuarioAutenticado contextoUsuario = usuarioFacade.contextoAutenticado();
        List<Alerta> alertas = alertaFacade.listarNaoLidos(contextoUsuario);

        return ResponseEntity.ok(alertas.stream().map(AlertaDto::fromEntity).toList());
    }

    @PostMapping("/marcar-como-lidos")
    @Operation(summary = "Marca múltiplos alertas como lidos")
    public ResponseEntity<Map<String, String>> marcarComoLidos(@RequestBody List<Long> codigos) {
        ContextoUsuarioAutenticado contextoUsuario = usuarioFacade.contextoAutenticado();
        alertaFacade.marcarComoLidos(contextoUsuario, codigos);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Alertas marcados como lidos.");
        return ResponseEntity.ok(response);
    }
}
