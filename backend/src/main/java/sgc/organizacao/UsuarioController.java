package sgc.organizacao;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sgc.organizacao.dto.AdicionarAdministradorRequest;
import sgc.organizacao.dto.AdministradorDto;
import sgc.organizacao.dto.UsuarioConsultaDto;
import sgc.organizacao.dto.UsuarioPesquisaDto;
import sgc.organizacao.service.UsuarioService;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Usuários", description = "Gerenciamento de usuários e administradores")
@PreAuthorize("isAuthenticated()")
public class UsuarioController {
    private final UsuarioAplicacaoService usuarioAplicacaoService;
    private final UsuarioService usuarioService;
    private final OrganizacaoDtoMapper organizacaoDtoMapper;

    @GetMapping("/{titulo}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTOR', 'CHEFE')")
    public ResponseEntity<UsuarioConsultaDto> buscarUsuarioPorTitulo(@PathVariable String titulo) {
        return usuarioService.buscarConsultaPorTitulo(titulo)
                .map(organizacaoDtoMapper::paraUsuarioConsultaDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/pesquisar")
    @Operation(summary = "Pesquisa usuários por nome")
    public ResponseEntity<List<UsuarioPesquisaDto>> pesquisarUsuarios(@RequestParam String termo) {
        return ResponseEntity.ok(usuarioService.pesquisarPorNome(termo));
    }

    @GetMapping("/administradores")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lista todos os administradores")
    public ResponseEntity<List<AdministradorDto>> listarAdministradores() {
        return ResponseEntity.ok(usuarioAplicacaoService.listarAdministradores());
    }

    @PostMapping("/administradores")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Adiciona um administrador")
    public ResponseEntity<AdministradorDto> adicionarAdministrador(
            @jakarta.validation.Valid @RequestBody AdicionarAdministradorRequest request) {

        AdministradorDto administrador = usuarioAplicacaoService.adicionarAdministrador(request.tituloEleitoral());
        return ResponseEntity.ok(administrador);
    }

    @PostMapping("/administradores/{usuarioTitulo}/remover")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove um administrador")
    public ResponseEntity<Void> removerAdministrador(
            @PathVariable String usuarioTitulo) {
        usuarioAplicacaoService.removerAdministrador(usuarioTitulo);
        return ResponseEntity.ok().build();
    }
}
