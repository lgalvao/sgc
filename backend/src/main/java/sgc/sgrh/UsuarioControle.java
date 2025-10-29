package sgc.sgrh;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sgc.sgrh.dto.AutenticacaoRequest;
import sgc.sgrh.dto.EntrarRequest;
import sgc.sgrh.dto.PerfilUnidade;
import sgc.sgrh.modelo.Perfil;
import sgc.sgrh.service.UsuarioService;
import sgc.unidade.modelo.UnidadeRepo;
import sgc.sgrh.dto.LoginResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;

import java.util.List;

// Importações JJWT
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import sgc.comum.config.JwtTokenProvider; // Importar JwtTokenProvider
import io.jsonwebtoken.io.Decoders; // Adicionar import

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioControle {

    private final UsuarioService usuarioService;
    private final UnidadeRepo unidadeRepo;

    /**
     * Autentica um usuário com base no título de eleitor e senha.
     * <p>
     * <b>Simulação:</b> Este endpoint simula o processo de autenticação.
     *
     * @param request O DTO contendo o título de eleitor e a senha.
     * @return Um {@link ResponseEntity} com {@code true} se a autenticação for bem-sucedida,
     *         {@code false} caso contrário.
     */
    @PostMapping("/autenticar")
    public ResponseEntity<Boolean> autenticar(@Valid @RequestBody AutenticacaoRequest request) {
        boolean autenticado = usuarioService.autenticar(request.getTituloEleitoral(), request.getSenha());
        return ResponseEntity.ok(autenticado);
    }

    /**
     * Autoriza um usuário, retornando a lista de perfis e unidades a que ele tem acesso.
     * <p>
     * <b>Simulação:</b> Este endpoint simula a busca de perfis de um usuário no SGRH.
     *
     * @param tituloEleitoral O título de eleitor do usuário.
     * @return Um {@link ResponseEntity} contendo a lista de {@link PerfilUnidade}.
     */
    @PostMapping("/autorizar")
    public ResponseEntity<List<PerfilUnidade>> autorizar(@RequestBody Long tituloEleitoral) {
        List<PerfilUnidade> perfis = usuarioService.autorizar(tituloEleitoral);
        return ResponseEntity.ok(perfis);
    }

    /**
     * Finaliza o processo de login, registrando o perfil e a unidade escolhidos pelo usuário.
     * <p>
     * <b>Simulação:</b> Este endpoint simula a confirmação do perfil de acesso do usuário
     * para a sessão.
     *
     * @param request O DTO contendo o título de eleitor e o perfil/unidade selecionado.
     * @return Um {@link ResponseEntity} com status 200 OK.
     */
    @PostMapping("/entrar")
    public ResponseEntity<LoginResponse> entrar(@Valid @RequestBody EntrarRequest request) {
        usuarioService.entrar(request);
        LoginResponse response = new LoginResponse(request.getTituloEleitoral(), Perfil.valueOf(request.getPerfil()), request.getUnidadeCodigo());

        // Gerar JWT assinado para perfil e2e e jules
        try {
            long tempoExpiracaoMillis = 3600000; // 1 hora
            Date agora = new Date();
            Date expiracao = new Date(agora.getTime() + tempoExpiracaoMillis);

            String token = Jwts.builder()
                    .subject(request.getTituloEleitoral().toString())
                    .claim("perfil", request.getPerfil())
                    .claim("unidadeCodigo", request.getUnidadeCodigo())
                    .issuedAt(agora)
                    .expiration(expiracao)
                    .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode("aW5jcmVkaXZlbG1lbnRlU2VjcmV0YUtleVBhcmFUb2tlbnNKV1RUZXN0ZXM=")))
                    .compact();

            response.setToken(token);
        } catch (Exception e) {
            // Logar o erro ou lançar uma exceção apropriada
            System.err.println("Erro ao gerar token JWT: " + e.getMessage());
            response.setToken("erro_geracao_token"); // Token de fallback em caso de erro
        }

        return ResponseEntity.ok(response);
    }
}