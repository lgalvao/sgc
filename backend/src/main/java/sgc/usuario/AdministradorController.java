package sgc.usuario;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sgc.usuario.dto.AdministradorDto;
import sgc.usuario.model.Usuario;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/administradores")
@RequiredArgsConstructor
@Tag(name = "Administradores", description = "Gerenciamento de administradores")
public class AdministradorController {
    private final AdministradorService administradorService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lista todos os administradores")
    public ResponseEntity<List<AdministradorDto>> listarAdministradores() {
        return ResponseEntity.ok(administradorService.listarAdministradores());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Adiciona um administrador")
    public ResponseEntity<AdministradorDto> adicionarAdministrador(
            @RequestBody Map<String, String> request) {

        String usuarioTitulo = request.get("usuarioTitulo");
        AdministradorDto administrador = administradorService.adicionarAdministrador(usuarioTitulo);
        return ResponseEntity.ok(administrador);
    }

    @PostMapping("/{usuarioTitulo}/remover")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove um administrador")
    public ResponseEntity<Void> removerAdministrador(
            @PathVariable String usuarioTitulo,
            @AuthenticationPrincipal Usuario usuarioAtual) {

        administradorService.removerAdministrador(usuarioTitulo, usuarioAtual.getTituloEleitoral());
        return ResponseEntity.ok().build();
    }
}
