package sgc.alerta;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.*;
import lombok.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import sgc.alerta.dto.*;
import sgc.alerta.model.*;
import sgc.organizacao.*;

import java.util.*;

@RestController
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
@Tag(name = "Alertas", description = "Endpoints para gerenciamento de alertas")
public class AlertaController {
    private final AlertaAplicacaoService alertaAplicacaoService;
    private final UsuarioAplicacaoService usuarioAplicacaoService;
    private final AlertaDtoMapper alertaDtoMapper;

    @GetMapping
    @Operation(summary = "Lista todos os alertas do usuário autenticado usando o contexto do JWT")
    public ResponseEntity<List<AlertaDto>> listarAlertas() {
        ContextoUsuarioAutenticado contextoUsuario = usuarioAplicacaoService.contextoAutenticado();
        List<Alerta> alertas = alertaAplicacaoService.alertasPorUsuario(contextoUsuario);

        return ResponseEntity.ok(alertas.stream().map(alertaDtoMapper::paraAlertaDto).toList());
    }

    @GetMapping("/nao-lidos")
    @Operation(summary = "Lista alertas não lidos do usuário autenticado usando o contexto do JWT")
    public ResponseEntity<List<AlertaDto>> listarNaoLidos() {
        ContextoUsuarioAutenticado contextoUsuario = usuarioAplicacaoService.contextoAutenticado();
        List<Alerta> alertas = alertaAplicacaoService.listarNaoLidos(contextoUsuario);

        return ResponseEntity.ok(alertas.stream().map(alertaDtoMapper::paraAlertaDto).toList());
    }

}
