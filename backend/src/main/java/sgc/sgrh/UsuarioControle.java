package sgc.sgrh;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.sgrh.dto.AutenticacaoRequest;
import sgc.sgrh.dto.EntrarRequest;
import sgc.sgrh.dto.PerfilUnidade;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioControle {

    private final UsuarioService usuarioService;
    private final UnidadeRepo unidadeRepo;

    @PostMapping("/autenticar")
    public ResponseEntity<Boolean> autenticar(@Valid @RequestBody AutenticacaoRequest request) {
        boolean autenticado = usuarioService.autenticar(request.getTituloEleitoral(), request.getSenha());
        return ResponseEntity.ok(autenticado);
    }

    @PostMapping("/autorizar")
    public ResponseEntity<List<PerfilUnidade>> autorizar(@RequestBody Long tituloEleitoral) {
        List<PerfilUnidade> perfis = usuarioService.autorizar(tituloEleitoral);
        return ResponseEntity.ok(perfis);
    }

    @PostMapping("/entrar")
    public ResponseEntity<Void> entrar(@Valid @RequestBody EntrarRequest request) {
        usuarioService.entrar(request);
        return ResponseEntity.ok().build();
    }
}