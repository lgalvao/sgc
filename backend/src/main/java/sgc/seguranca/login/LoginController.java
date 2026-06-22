package sgc.seguranca.login;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sgc.comum.erros.ErroAutenticacao;
import sgc.comum.erros.ErroConfiguracao;
import sgc.organizacao.UsuarioAplicacaoService;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Usuario;
import sgc.seguranca.LoginAplicacaoService;
import sgc.seguranca.config.JwtProperties;
import sgc.seguranca.dto.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Controller responsável pelo fluxo de login: autenticação, autorização e entrada.
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Login", description = "Autenticação e autorização de usuários")
public class LoginController {
    private final LoginAplicacaoService loginAplicacaoService;
    private final UsuarioAplicacaoService usuarioAplicacaoService;
    private final LimitadorTentativasLogin limitadorTentativasLogin;
    private final GerenciadorJwt gerenciadorJwt;
    private final ListaNegraJwt listaNegraJwt;
    private final JwtProperties jwtProperties;

    @Value("${aplicacao.cookies.secure:false}")
    private boolean cookieSecure;

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

        boolean autenticado = loginAplicacaoService.autenticar(request.tituloEleitoral(), request.senha());
        if (!autenticado) {
            throw new ErroAutenticacao("Título ou senha inválidos.");
        }

        List<PerfilUnidadeDto> perfis = loginAplicacaoService.buscarAutorizacoesUsuario(request.tituloEleitoral());
        if (perfis.isEmpty()) {
            throw new ErroConfiguracao("Usuário autenticado sem perfil no SGC. Verifique as views de autorização.");
        }
        if (perfis.size() == 1) {
            PerfilUnidadeDto perfilUnidade = perfis.getFirst();
            EntrarRequest entrarRequest = EntrarRequest.builder()
                    .perfil(perfilUnidade.perfil())
                    .unidadeCodigo(perfilUnidade.unidade().codigo())
                    .build();
            String token = loginAplicacaoService.entrar(entrarRequest, request.tituloEleitoral(), perfis);
            Usuario usuario = usuarioAplicacaoService.buscarPorLogin(request.tituloEleitoral());
            Perfil perfilSelecionado = Perfil.valueOf(perfilUnidade.perfil());

            adicionarCookieJwt(httpResponse, token);
            limparCookiePreAuth(httpResponse);

            EntrarResponse sessao = EntrarResponse.builder()
                    .tituloEleitoral(request.tituloEleitoral())
                    .nome(usuario.getNome())
                    .perfil(perfilSelecionado.name())
                    .unidadeCodigo(perfilUnidade.unidade().codigo())
                    .permissoes(construirPermissoesSessao(perfilSelecionado))
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
                .requerSelecaoPerfil(true)
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
        String token = loginAplicacaoService.entrar(request, tituloEleitoral);
        Usuario usuario = usuarioAplicacaoService.buscarPorLogin(tituloEleitoral);

        EntrarResponse response = EntrarResponse.builder()
                .tituloEleitoral(tituloEleitoral)
                .nome(usuario.getNome())
                .perfil(request.perfil())
                .unidadeCodigo(request.unidadeCodigo())
                .permissoes(construirPermissoesSessao(Perfil.valueOf(request.perfil())))
                .build();

        adicionarCookieJwt(httpResponse, token);
        limparCookiePreAuth(httpResponse);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Encerra a sessão e remove os cookies de autenticação")
    public ResponseEntity<Void> logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        extrairTokenCookie(httpRequest, "jwtToken")
                .flatMap(gerenciadorJwt::validarToken)
                .ifPresent(claims -> listaNegraJwt.revogar(claims.jti(), obterExpiracaoLogout(claims)));
        limparCookieJwt(httpResponse);
        limparCookiePreAuth(httpResponse);
        return ResponseEntity.noContent().build();
    }

    private void adicionarCookiePreAuth(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("SGC_PRE_AUTH", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge(300);
        response.addCookie(cookie);
    }

    private void adicionarCookieJwt(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("jwtToken", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge(86400);
        response.addCookie(cookie);
    }

    private void limparCookiePreAuth(HttpServletResponse response) {
        Cookie cookie = new Cookie("SGC_PRE_AUTH", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private void limparCookieJwt(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwtToken", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private PermissoesSessaoResponse construirPermissoesSessao(Perfil perfil) {
        boolean admin = perfil == Perfil.ADMIN;
        boolean gestor = perfil == Perfil.GESTOR;
        return PermissoesSessaoResponse.builder()
                .mostrarCriarProcesso(admin)
                .mostrarArvoreCompletaUnidades(admin)
                .mostrarCtaPainelVazio(admin)
                .mostrarRelatorios(admin || gestor)
                .mostrarDiagnosticoOrganizacional(admin)
                .mostrarMenuConfiguracoes(admin)
                .mostrarMenuAdministradores(admin)
                .mostrarCriarAtribuicaoTemporaria(admin)
                .build();
    }

    private String extrairTituloPreAuth(HttpServletRequest request) {
        String token = extrairTokenCookie(request, "SGC_PRE_AUTH")
                .orElseThrow(() -> new ErroAutenticacao("Sessão expirada ou inválida. Faça login novamente."));

        return gerenciadorJwt.validarTokenPreAuth(token)
                .map(GerenciadorJwt.JwtPreAuthClaims::tituloEleitoral)
                .orElseThrow(() -> new ErroAutenticacao("Sessão inválida. Faça login novamente."));
    }

    private Optional<String> extrairTokenCookie(HttpServletRequest request, String nomeCookie) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        for (Cookie cookie : request.getCookies()) {
            if (nomeCookie.equals(cookie.getName())) {
                return Optional.ofNullable(cookie.getValue());
            }
        }
        return Optional.empty();
    }

    private Instant obterExpiracaoLogout(GerenciadorJwt.JwtClaims claims) {
        if (claims.expiracao() != null) {
            return claims.expiracao();
        }
        return Instant.now().plus(Duration.ofMinutes(jwtProperties.expiracaoMinutos()));
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
