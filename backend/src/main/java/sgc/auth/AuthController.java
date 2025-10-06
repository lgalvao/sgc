package sgc.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import sgc.auth.dto.LoginRequest;
import sgc.auth.dto.LoginResponse;
import sgc.auth.dto.PerfilDto;

import java.util.List;

/**
 * Controller responsável pelos endpoints de autenticação.
 * Gerencia login, logout e consulta de perfis do usuário autenticado.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * Endpoint de login.
     * Autentica o usuário via AD e retorna token JWT com perfis.
     *
     * @param request Requisição de login com título e senha
     * @return LoginResponse com token, perfis e dados do servidor
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Requisição de login recebida para usuário: {}", request.titulo());
        
        LoginResponse response = authService.authenticate(request);
        
        log.info("Login bem-sucedido para usuário: {}", request.titulo());
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de logout.
     * Como JWT é stateless, apenas retorna 200 OK.
     * O cliente deve descartar o token.
     *
     * @return ResponseEntity vazio com status 200
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated()) {
            log.info("Logout realizado para usuário: {}", auth.getName());
        }
        
        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint para obter perfis do usuário autenticado.
     * Retorna os perfis com base no token JWT do contexto de segurança.
     *
     * @return Lista de perfis do usuário autenticado
     */
    @GetMapping("/perfis")
    public ResponseEntity<List<PerfilDto>> getPerfis() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Tentativa de obter perfis sem autenticação");
            return ResponseEntity.status(401).build();
        }
        
        String titulo = auth.getName();
        log.debug("Recuperando perfis para usuário autenticado: {}", titulo);
        
        List<PerfilDto> perfis = authService.getPerfisUsuarioAutenticado(titulo);
        
        return ResponseEntity.ok(perfis);
    }

    /**
     * Endpoint para verificar se o usuário está autenticado.
     * Útil para validação de sessão no frontend.
     *
     * @return ResponseEntity com status 200 se autenticado, 401 caso contrário
     */
    @GetMapping("/verify")
    public ResponseEntity<Void> verify() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            return ResponseEntity.status(401).build();
        }
        
        return ResponseEntity.ok().build();
    }
}