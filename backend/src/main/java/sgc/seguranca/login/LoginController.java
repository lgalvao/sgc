package sgc.seguranca.login;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.*;
import jakarta.servlet.http.*;
import jakarta.validation.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.jspecify.annotations.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import sgc.comum.erros.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.seguranca.*;
import sgc.seguranca.dto.*;

import java.util.*;

/**
 * Controller responsável pelo fluxo de login: autenticação, autorização e entrada.
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Login", description = "Autenticação e autorização de usuários")
public class LoginController {
    private final LoginFacade loginFacade;
    private final UsuarioFacade usuarioFacade;
    private final LimitadorTentativasLogin limitadorTentativasLogin;
    private final GerenciadorJwt gerenciadorJwt;

    @Value("${aplicacao.ambiente-testes:false}")
    private boolean ambienteTestes;

    /**
     * Inicia o fluxo de login. Se houver um único par perfil/unidade disponível,
     * a sessão já é concluída nesta chamada.
     */
    @PostMapping("/login")
    @Operation(summary = "Inicia o login e retorna as opções de perfil/unidade")
    public ResponseEntity<FluxoLoginResponse> login(
            @Valid @RequestBody AutenticarRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        String ip = extrairIp(httpRequest);
        if (ip != null) limitadorTentativasLogin.verificar(ip);

        boolean autenticado = loginFacade.autenticar(request.tituloEleitoral(), request.senha());
        if (!autenticado) {
            throw new ErroAutenticacao("Título ou senha inválidos.");
        }

        List<PerfilUnidadeDto> perfis = loginFacade.buscarAutorizacoesUsuario(request.tituloEleitoral());
        if (perfis.isEmpty()) {
            throw new ErroConfiguracao("Usuário autenticado sem perfil no SGC. Verifique as views de autorização.");
        }
        if (perfis.size() == 1) {
            PerfilUnidadeDto perfilUnidade = perfis.getFirst();
            EntrarRequest entrarRequest = EntrarRequest.builder()
                    .perfil(perfilUnidade.perfil().name())
                    .unidadeCodigo(perfilUnidade.unidade().getCodigo())
                    .build();
            String token = loginFacade.entrar(entrarRequest, request.tituloEleitoral(), perfis);
            Usuario usuario = usuarioFacade.buscarPorLogin(request.tituloEleitoral());

            adicionarCookieJwt(httpResponse, token);
            limparCookiePreAuth(httpResponse);

            EntrarResponse sessao = EntrarResponse.builder()
                    .tituloEleitoral(request.tituloEleitoral())
                    .nome(usuario.getNome())
                    .perfil(perfilUnidade.perfil())
                    .unidadeCodigo(perfilUnidade.unidade().getCodigo())
                    .build();

            return ResponseEntity.ok(FluxoLoginResponse.builder()
                    .autenticado(true)
                    .requerSelecaoPerfil(false)
                    .perfisUnidades(perfis)
                    .sessao(sessao)
                    .build());
        }

        String token = gerenciadorJwt.gerarTokenPreAuth(request.tituloEleitoral());
        adicionarCookiePreAuth(httpResponse, token);

        return ResponseEntity.ok(FluxoLoginResponse.builder()
                .autenticado(true)
                .requerSelecaoPerfil(perfis.size() > 1)
                .perfisUnidades(perfis)
                .sessao(null)
                .build());
    }

    /**
     * Finaliza o processo de login, registrando o perfil e a unidade escolhidos
     * pelo usuário.
     */
    @PostMapping("/entrar")
    @Operation(summary = "Finaliza o login e retorna o token JWT")
    public ResponseEntity<EntrarResponse> entrar(
            @Valid @RequestBody EntrarRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        String tituloEleitoral = extrairTituloPreAuth(httpRequest);
        String token = loginFacade.entrar(request, tituloEleitoral);
        Usuario usuario = usuarioFacade.buscarPorLogin(tituloEleitoral);

        EntrarResponse response = EntrarResponse.builder()
                .tituloEleitoral(tituloEleitoral)
                .nome(usuario.getNome())
                .perfil(Perfil.valueOf(request.perfil()))
                .unidadeCodigo(request.unidadeCodigo())
                .build();

        adicionarCookieJwt(httpResponse, token);
        limparCookiePreAuth(httpResponse);

        return ResponseEntity.ok(response);
    }

    private void adicionarCookiePreAuth(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("SGC_PRE_AUTH", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(!ambienteTestes);
        cookie.setPath("/");
        cookie.setMaxAge(300);
        response.addCookie(cookie);
    }

    private void adicionarCookieJwt(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("jwtToken", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(!ambienteTestes);
        cookie.setPath("/");
        cookie.setMaxAge(86400);
        response.addCookie(cookie);
    }

    private void limparCookiePreAuth(HttpServletResponse response) {
        Cookie cookie = new Cookie("SGC_PRE_AUTH", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(!ambienteTestes);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private String extrairTituloPreAuth(HttpServletRequest request) {
        String token = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("SGC_PRE_AUTH".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token == null) {
            throw new ErroAutenticacao("Sessão expirada ou inválida. Faça login novamente.");
        }

        Optional<String> sujeito = gerenciadorJwt.validarTokenPreAuth(token);
        if (sujeito.isEmpty()) {
            throw new ErroAutenticacao("Sessão inválida. Faça login novamente.");
        }
        return sujeito.get();
    }

    @SuppressWarnings("ConstantConditions")
    private @Nullable String extrairIp(HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();

        // Sanitização contra Log injection (CWE-117)
        if (ip != null) {
            return ip.replaceAll("[\\n\\r]", "_");
        }
        return ip;
    }
}
