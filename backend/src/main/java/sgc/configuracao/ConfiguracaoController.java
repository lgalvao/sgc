package sgc.configuracao;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import sgc.configuracao.dto.ParametroRequest;
import sgc.configuracao.dto.ParametroResponse;

import java.util.List;

@RestController
@RequestMapping("/api/configuracoes")
@RequiredArgsConstructor
@Tag(name = "Configurações", description = "Gerenciamento de configurações")
@Validated
public class ConfiguracaoController {
    private final ConfiguracaoFacade configuracaoFacade;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todas as configurações")
    public List<ParametroResponse> listar() {
        return configuracaoFacade.buscarTodos();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar configurações em bloco")
    public List<ParametroResponse> atualizar(@RequestBody @Valid List<ParametroRequest> parametros) {
        return configuracaoFacade.salvar(parametros);
    }
}
