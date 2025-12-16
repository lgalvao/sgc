package sgc.sgrh;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sgc.sgrh.dto.AutenticacaoReq;
import sgc.sgrh.dto.EntrarReq;
import sgc.sgrh.dto.LoginResp;
import sgc.sgrh.dto.PerfilUnidade;
import sgc.sgrh.model.Perfil;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Slf4j
public class SgrhController {
    private final SgrhService sgrhService;

    /**
     * Autentica um usuário com base no título de eleitor e senha.
     *
     * @param request O DTO contendo o título de eleitor e a senha.
     * @return Um {@link ResponseEntity} com {@code true} se a autenticação for bem-sucedida, {@code
     * false} caso contrário.
     */
    @PostMapping("/autenticar")
    public ResponseEntity<Boolean> autenticar(@Valid @RequestBody AutenticacaoReq request) {
        boolean autenticado = sgrhService.autenticar(request.getTituloEleitoral(), request.getSenha());
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
        List<PerfilUnidade> perfis = sgrhService.autorizar(tituloEleitoral);
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
        String token = sgrhService.entrar(request);
        
        LoginResp response = LoginResp.builder()
                .tituloEleitoral(request.getTituloEleitoral())
                .perfil(Perfil.valueOf(request.getPerfil()))
                .unidadeCodigo(request.getUnidadeCodigo())
                .token(token)
                .build();

        return ResponseEntity.ok(response);
    }
}
