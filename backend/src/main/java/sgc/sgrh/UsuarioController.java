package sgc.sgrh;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sgc.sgrh.dto.AutenticacaoReq;
import sgc.sgrh.dto.EntrarReq;
import sgc.sgrh.dto.LoginResp;
import sgc.sgrh.dto.PerfilUnidade;
import sgc.sgrh.model.Perfil;
import sgc.sgrh.service.UsuarioService;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class UsuarioController {
    private final UsuarioService usuarioService;

    /**
     * Busca todos os usuários do sistema.
     *
     * @return Uma lista de todos os usuários cadastrados.
     */
    @GetMapping
    public ResponseEntity<List<PerfilUnidade>> buscarTodosUsuarios() {
        // Para fins de simulação, retorna uma lista vazia
        // Em um ambiente real, buscaria do SGRH ou banco de dados
        return ResponseEntity.ok(List.of());
    }

    /**
     * Autentica um usuário com base no título de eleitor e senha.
     *
     * <p><b>Simulação:</b> Este endpoint simula o processo de autenticação.
     *
     * @param request O DTO contendo o título de eleitor e a senha.
     * @return Um {@link ResponseEntity} com {@code true} se a autenticação for bem-sucedida, {@code
     * false} caso contrário.
     */
    @PostMapping("/autenticar")
    public ResponseEntity<Boolean> autenticar(@Valid @RequestBody AutenticacaoReq request) {
        boolean autenticado =
                usuarioService.autenticar(request.getTituloEleitoral(), request.getSenha());
        return ResponseEntity.ok(autenticado);
    }

    /**
     * Autoriza um usuário, retornando a lista de perfis e unidades a que ele tem acesso.
     *
     * <p><b>Simulação:</b> Este endpoint simula a busca de perfis de um usuário no SGRH.
     *
     * @param tituloEleitoral O título de eleitor do usuário.
     * @return Um {@link ResponseEntity} contendo a lista de {@link PerfilUnidade}.
     */
    @PostMapping("/autorizar")
    public ResponseEntity<List<PerfilUnidade>> autorizar(@RequestBody String tituloEleitoral) {
        List<PerfilUnidade> perfis = usuarioService.autorizar(tituloEleitoral);
        return ResponseEntity.ok(perfis);
    }

    /**
     * Finaliza o processo de login, registrando o perfil e a unidade escolhidos pelo usuário.
     *
     * <p><b>Simulação:</b> Este endpoint simula a confirmação do perfil de acesso do usuário para a
     * sessão.
     *
     * @param request O DTO contendo o título de eleitor e o perfil/unidade selecionado.
     * @return Um {@link ResponseEntity} com status 200 OK.
     */
    @PostMapping("/entrar")
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public ResponseEntity<LoginResp> entrar(@Valid @RequestBody EntrarReq request) {
        usuarioService.entrar(request);
        LoginResp response =
                new LoginResp(
                        request.getTituloEleitoral(),
                        Perfil.valueOf(request.getPerfil()),
                        request.getUnidadeCodigo());

        // Gerar JWT simulado
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> claims = new HashMap<>();
            claims.put("tituloEleitoral", request.getTituloEleitoral());
            claims.put("perfil", request.getPerfil());
            claims.put("unidadeCodigo", request.getUnidadeCodigo());
            String jsonClaims = objectMapper.writeValueAsString(claims);
            String encodedClaims =
                    Base64.getEncoder().encodeToString(jsonClaims.getBytes(StandardCharsets.UTF_8));
            response.setToken(encodedClaims);
        } catch (Exception e) {
            // Logar o erro ou lançar uma exceção apropriada
            log.error("Erro ao gerar token simulado", e);
            response.setToken("erro_geracao_token"); // Token de fallback em caso de erro
        }

        return ResponseEntity.ok(response);
    }
}
