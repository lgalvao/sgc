package sgc.organizacao;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sgc.organizacao.dto.AdministradorDto;
import sgc.organizacao.dto.UsuarioDto;
import sgc.organizacao.model.Usuario;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Usuários", description = "Gerenciamento de usuários e administradores")
public class UsuarioController {
    private final UsuarioFacade usuarioService;

    /**
     * Busca um usuário pelo título de eleitor.
     *
     * @param titulo O título de eleitor.
     * @return O DTO do usuário, se encontrado.
     */
    @GetMapping("/{titulo}")
    public ResponseEntity<UsuarioDto> buscarUsuarioPorTitulo(@PathVariable String titulo) {
        return usuarioService.buscarUsuarioPorTitulo(titulo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ===================== ADMINISTRADORES =====================

    @GetMapping("/administradores")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lista todos os administradores")
    public ResponseEntity<List<AdministradorDto>> listarAdministradores() {
        return ResponseEntity.ok(usuarioService.listarAdministradores());
    }

    @PostMapping("/administradores")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Adiciona um administrador")
    public ResponseEntity<AdministradorDto> adicionarAdministrador(
            @RequestBody Map<String, String> request) {

        String usuarioTitulo = request.get("usuarioTitulo");
        AdministradorDto administrador = usuarioService.adicionarAdministrador(usuarioTitulo);
        return ResponseEntity.ok(administrador);
    }

    @PostMapping("/administradores/{usuarioTitulo}/remover")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove um administrador")
    public ResponseEntity<Void> removerAdministrador(
            @PathVariable String usuarioTitulo,
            @AuthenticationPrincipal Usuario usuarioAtual) {

        usuarioService.removerAdministrador(usuarioTitulo, usuarioAtual.getTituloEleitoral());
        return ResponseEntity.ok().build();
    }
}
