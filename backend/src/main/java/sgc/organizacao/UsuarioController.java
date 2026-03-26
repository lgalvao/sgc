package sgc.organizacao;

import com.fasterxml.jackson.annotation.*;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.*;
import org.springframework.security.core.annotation.*;
import org.springframework.web.bind.annotation.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;

import java.util.*;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Usuários", description = "Gerenciamento de usuários e administradores")
@PreAuthorize("isAuthenticated()")
public class UsuarioController {
    private final UsuarioFacade usuarioFacade;
    private final UsuarioService usuarioService;

    @GetMapping("/{titulo}")
    public ResponseEntity<UsuarioConsultaDto> buscarUsuarioPorTitulo(@PathVariable String titulo) {
        return usuarioService.buscarOpt(titulo)
                .map(UsuarioConsultaDto::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/pesquisar")
    @Operation(summary = "Pesquisa usuários por nome ou matrícula")
    public ResponseEntity<List<UsuarioConsultaDto>> pesquisarUsuarios(@RequestParam String termo) {
        return ResponseEntity.ok(usuarioService.buscarPorNomeOuMatricula(termo).stream()
                .map(UsuarioConsultaDto::fromEntity)
                .toList());
    }

    @JsonView(OrganizacaoViews.Interna.class)
    @GetMapping("/administradores")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lista todos os administradores")
    public ResponseEntity<List<AdministradorDto>> listarAdministradores() {
        return ResponseEntity.ok(usuarioFacade.listarAdministradores());
    }

    @JsonView(OrganizacaoViews.Interna.class)
    @PostMapping("/administradores")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Adiciona um administrador")
    public ResponseEntity<AdministradorDto> adicionarAdministrador(
            @RequestBody Map<String, String> request) {

        String usuarioTitulo = request.get("usuarioTitulo");
        AdministradorDto administrador = usuarioFacade.adicionarAdministrador(usuarioTitulo);
        return ResponseEntity.ok(administrador);
    }

    @PostMapping("/administradores/{usuarioTitulo}/remover")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove um administrador")
    public ResponseEntity<Void> removerAdministrador(
            @PathVariable String usuarioTitulo,
            @AuthenticationPrincipal Usuario usuarioAtual) {

        usuarioFacade.removerAdministrador(usuarioTitulo, usuarioAtual.getTituloEleitoral());
        return ResponseEntity.ok().build();
    }
}
