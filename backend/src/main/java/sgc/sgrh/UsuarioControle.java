package sgc.sgrh;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sgc.sgrh.dto.AutenticacaoRequest;
import sgc.sgrh.dto.EntrarRequest;
import sgc.sgrh.dto.LoginResponse;
import sgc.sgrh.dto.UsuarioDto;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioControle {

    private final UsuarioService usuarioService;

    @PostMapping("/autenticar")
    public ResponseEntity<Void> autenticar(@Valid @RequestBody AutenticacaoRequest request) {
        usuarioService.autenticar(request.getTituloEleitoral(), request.getSenha());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/autorizar/{tituloEleitoral}")
    public ResponseEntity<LoginResponse> autorizar(@PathVariable Long tituloEleitoral) {
        LoginResponse response = usuarioService.autorizar(tituloEleitoral);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/entrar")
    public ResponseEntity<UsuarioDto> entrar(@Valid @RequestBody EntrarRequest request) {
        UsuarioDto usuario = usuarioService.entrar(request);
        return ResponseEntity.ok(usuario);
    }
}