package sgc.alerta;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sgc.alerta.dto.AlertaDto;
import sgc.alerta.model.Alerta;
import sgc.organizacao.ContextoUsuarioAutenticado;
import sgc.organizacao.UsuarioAplicacaoService;

import java.util.List;

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
