package sgc.usuario;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sgc.autenticacao.dto.*;
import sgc.usuario.dto.*;
import sgc.usuario.model.Perfil;
import sgc.usuario.model.Usuario;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Slf4j
public class UsuarioController {
    private final UsuarioService usuarioService;

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
    public ResponseEntity<Boolean> autenticar(@Valid @RequestBody AutenticacaoReq request) {
        boolean autenticado = usuarioService.autenticar(request.getTituloEleitoral(), request.getSenha());
        return ResponseEntity.ok(autenticado);
    }

    /**
     * Autoriza um usuário, retornando a lista de perfis e unidades a que ele tem acesso.
     *
     * @param tituloEleitoral O título de eleitor do usuário (chave).
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
     * @param request O DTO contendo o título de eleitor e o perfil/unidade selecionado.
     * @return Um {@link ResponseEntity} com o token de sessão.
     */
    @PostMapping("/entrar")
    public ResponseEntity<LoginResp> entrar(@Valid @RequestBody EntrarReq request) {
        String token = usuarioService.entrar(request);
        Usuario usuario = usuarioService.buscarUsuarioPorLogin(request.getTituloEleitoral());
        
        LoginResp response = LoginResp.builder()
                .tituloEleitoral(request.getTituloEleitoral())
                .nome(usuario.getNome())
                .perfil(Perfil.valueOf(request.getPerfil()))
                .unidadeCodigo(request.getUnidadeCodigo())
                .token(token)
                .build();

        return ResponseEntity.ok(response);
    }
}
