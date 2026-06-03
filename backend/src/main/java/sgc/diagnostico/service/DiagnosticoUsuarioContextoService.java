package sgc.diagnostico.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.organizacao.UsuarioAplicacaoService;
import sgc.organizacao.model.Usuario;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiagnosticoUsuarioContextoService {
    private final UsuarioAplicacaoService usuarioService;

    public Usuario usuarioAutenticado() {
        return usuarioService.usuarioAutenticado();
    }
}
