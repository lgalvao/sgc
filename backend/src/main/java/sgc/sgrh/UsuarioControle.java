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
import sgc.unidade.modelo.UnidadeRepo;

import java.util.List;

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
    public ResponseEntity<Void> entrar(@Valid @RequestBody EntrarRequest request) {
        usuarioService.entrar(request);
        return ResponseEntity.ok().build();
    }
}