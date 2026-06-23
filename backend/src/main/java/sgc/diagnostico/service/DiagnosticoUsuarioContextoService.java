package sgc.diagnostico.service;

import lombok.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiagnosticoUsuarioContextoService {
    private final UsuarioAplicacaoService usuarioService;

    public Usuario usuarioAutenticado() {
        return usuarioService.usuarioAutenticado();
    }
}
