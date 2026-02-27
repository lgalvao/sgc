package sgc.parametros;

import com.fasterxml.jackson.annotation.*;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.*;
import jakarta.validation.*;
import lombok.*;
import org.springframework.security.access.prepost.*;
import org.springframework.validation.annotation.*;
import org.springframework.web.bind.annotation.*;
import sgc.parametros.model.*;

import java.util.*;

@RestController
@RequestMapping("/api/configuracoes")
@RequiredArgsConstructor
@Tag(name = "Configurações", description = "Gerenciamento de configurações")
@Validated
public class ConfiguracaoController {
    private final ConfiguracaoService configuracaoService;

    @JsonView(ConfiguracaoViews.Publica.class)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todas as configurações")
    public List<Parametro> listar() {
        return configuracaoService.buscarTodos();
    }

    @JsonView(ConfiguracaoViews.Publica.class)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar configurações em bloco")
    public List<Parametro> atualizar(@RequestBody @Valid List<ParametroRequest> parametros) {
        // Buscar parâmetros existentes e atualizar com dados das requests
        List<Parametro> parametrosAtualizados = parametros.stream()
                .map(request -> {
                    Parametro parametro = configuracaoService.buscarPorId(request.codigo());
                    parametro.atualizarDe(request);
                    return parametro;
                })
                .toList();
        
        return configuracaoService.salvar(parametrosAtualizados);
    }
}
