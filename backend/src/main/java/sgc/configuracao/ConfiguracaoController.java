package sgc.configuracao;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sgc.configuracao.model.Parametro;

import java.util.List;

@RestController
@RequestMapping("/api/configuracoes")
@RequiredArgsConstructor
@Tag(name = "Configurações", description = "Gerenciamento de configurações")
public class ConfiguracaoController {
    private final ConfiguracaoFacade configuracaoFacade;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todas as configurações")
    public List<Parametro> listar() {
        return configuracaoFacade.buscarTodos();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar configurações em bloco")
    public List<Parametro> atualizar(@RequestBody List<Parametro> parametros) {
        return configuracaoFacade.salvar(parametros);
    }
}
