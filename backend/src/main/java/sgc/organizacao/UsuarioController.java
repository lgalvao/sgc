package sgc.organizacao;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sgc.organizacao.dto.AdministradorDto;
import sgc.organizacao.dto.UsuarioDto;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Usuario;
import sgc.seguranca.LimitadorTentativasLogin;
import sgc.seguranca.autenticacao.AutenticarReq;
import sgc.seguranca.dto.EntrarReq;
import sgc.seguranca.dto.EntrarResp;
import sgc.seguranca.dto.PerfilUnidadeDto;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Usuários", description = "Gerenciamento de usuários e administradores")
public class UsuarioController {
    private final UsuarioService usuarioService;
    private final LimitadorTentativasLogin limitadorTentativasLogin;

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

    /**
     * Autentica um usuário com base no título de eleitor e senha.
     *
     * @param request O DTO contendo o título de eleitor e a senha.
     * @return Um {@link ResponseEntity} com {@code true} se a autenticação for bem-sucedida, {@code
     * false} caso contrário.
     */
    @PostMapping("/autenticar")
    public ResponseEntity<Boolean> autenticar(@Valid @RequestBody AutenticarReq request, HttpServletRequest httpRequest) {
        String ip = httpRequest.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = httpRequest.getRemoteAddr();
        } else {
            // Pega o primeiro IP da lista (client original)
            ip = ip.split(",")[0].trim();
        }

        limitadorTentativasLogin.verificar(ip);
        boolean autenticado = usuarioService.autenticar(request.getTituloEleitoral(), request.getSenha());
        return ResponseEntity.ok(autenticado);
    }

    /**
     * Autoriza um usuário, retornando a lista de perfis e unidades a que ele tem acesso.
     *
     * @param tituloEleitoral O título de eleitor do usuário (chave).
     * @return Um {@link ResponseEntity} contendo a lista de {@link PerfilUnidadeDto}.
     */
    @PostMapping("/autorizar")
    public ResponseEntity<List<PerfilUnidadeDto>> autorizar(@RequestBody String tituloEleitoral) {
        List<PerfilUnidadeDto> perfis = usuarioService.autorizar(tituloEleitoral);
        return ResponseEntity.ok(perfis);
    }
    
    /**
     * Finaliza o processo de login, registrando o perfil e a unidade escolhidos pelo usuário.
     *
     * @param request O DTO contendo o título de eleitor e o perfil/unidade selecionado.
     * @return Um {@link ResponseEntity} com o token de sessão.
     */
    @PostMapping("/entrar")
    public ResponseEntity<EntrarResp> entrar(@Valid @RequestBody EntrarReq request) {
        String token = usuarioService.entrar(request);
        Usuario usuario = usuarioService.buscarPorLogin(request.getTituloEleitoral());
        
        EntrarResp response = EntrarResp.builder()
                .tituloEleitoral(request.getTituloEleitoral())
                .nome(usuario.getNome())
                .perfil(Perfil.valueOf(request.getPerfil()))
                .unidadeCodigo(request.getUnidadeCodigo())
                .token(token)
                .build();

        return ResponseEntity.ok(response);
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
