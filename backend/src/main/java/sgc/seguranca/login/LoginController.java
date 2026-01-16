package sgc.seguranca.login;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Usuario;
import sgc.seguranca.login.dto.AutenticarRequest;
import sgc.seguranca.login.dto.EntrarRequest;
import sgc.seguranca.login.dto.EntrarResponse;
import sgc.seguranca.login.dto.PerfilUnidadeDto;

import java.util.List;

/**
 * Controller responsável pelo fluxo de login: autenticação, autorização e
 * entrada.
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Login", description = "Autenticação e autorização de usuários")
public class LoginController {
    private final LoginService loginService;
    private final UsuarioFacade usuarioService;
    private final LimitadorTentativasLogin limitadorTentativasLogin;

    /**
     * Autentica um usuário com base no título de eleitor e senha.
     *
     * @param request O DTO contendo o título de eleitor e a senha.
     * @return Um {@link ResponseEntity} com {@code true} se a autenticação for
     *         bem-sucedida.
     */
    @PostMapping("/autenticar")
    @Operation(summary = "Autentica um usuário com título e senha")
    public ResponseEntity<Boolean> autenticar(
            @Valid @RequestBody AutenticarRequest request,
            HttpServletRequest httpRequest) {

        String ip = extrairIp(httpRequest);
        limitadorTentativasLogin.verificar(ip);

        boolean autenticado = loginService.autenticar(request.getTituloEleitoral(), request.getSenha());
        return ResponseEntity.ok(autenticado);
    }

    /**
     * Autoriza um usuário, retornando a lista de perfis e unidades a que ele tem
     * acesso.
     *
     * @param tituloEleitoral O título de eleitor do usuário.
     * @return Um {@link ResponseEntity} contendo a lista de
     *         {@link PerfilUnidadeDto}.
     */
    @PostMapping("/autorizar")
    @Operation(summary = "Retorna os perfis e unidades disponíveis para o usuário")
    public ResponseEntity<List<PerfilUnidadeDto>> autorizar(@RequestBody String tituloEleitoral) {
        List<PerfilUnidadeDto> perfis = loginService.autorizar(tituloEleitoral);
        return ResponseEntity.ok(perfis);
    }

    /**
     * Finaliza o processo de login, registrando o perfil e a unidade escolhidos
     * pelo usuário.
     *
     * @param request O DTO contendo o título de eleitor e o perfil/unidade
     *                selecionado.
     * @return Um {@link ResponseEntity} com o token de sessão.
     */
    @PostMapping("/entrar")
    @Operation(summary = "Finaliza o login e retorna o token JWT")
    public ResponseEntity<EntrarResponse> entrar(@Valid @RequestBody EntrarRequest request) {
        String token = loginService.entrar(request);
        Usuario usuario = usuarioService.buscarPorLogin(request.getTituloEleitoral());

        EntrarResponse response = EntrarResponse.builder()
                .tituloEleitoral(request.getTituloEleitoral())
                .nome(usuario.getNome())
                .perfil(Perfil.valueOf(request.getPerfil()))
                .unidadeCodigo(request.getUnidadeCodigo())
                .token(token)
                .build();

        return ResponseEntity.ok(response);
    }

    private String extrairIp(HttpServletRequest httpRequest) {
        String ip = httpRequest.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = httpRequest.getRemoteAddr();
        } else {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
