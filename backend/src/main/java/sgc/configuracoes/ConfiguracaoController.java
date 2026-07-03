package sgc.configuracoes;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.*;
import jakarta.validation.*;
import lombok.*;
import org.springframework.security.access.prepost.*;
import org.springframework.validation.annotation.*;
import org.springframework.web.bind.annotation.*;
import sgc.configuracoes.model.*;

import java.util.*;

@RestController
@RequestMapping("/api/configuracoes")
@RequiredArgsConstructor
@Tag(name = "Configurações", description = "Gerenciamento de configurações")
@Validated
public class ConfiguracaoController {
    private final ConfiguracaoService configuracaoService;
    private final ConfiguracaoMapper configuracaoMapper;

    public record DiasInativacaoProcessoResponse(int dias) {
    }

    @GetMapping("/dias-inativacao-processo")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Consultar quantidade de dias para inativação de processos")
    public DiasInativacaoProcessoResponse consultarDiasInativacaoProcesso() {
        return new DiasInativacaoProcessoResponse(configuracaoService.buscarDiasInativacaoProcesso());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todas as configurações")
    public List<ConfiguracaoDto> listar() {
        return configuracaoService.buscarTodos().stream()
                .map(configuracaoMapper::paraDto)
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar configurações em bloco")
    public List<ConfiguracaoDto> atualizar(@RequestBody List<@Valid ConfiguracaoRequest> parametros) {
        // Buscar Configuraçãos existentes e atualizar com dados das requests
        List<Configuracao> configsAtualizadas = parametros.stream()
                .map(request -> {
                    Configuracao configuracao = configuracaoService.buscarPorCodigo(request.codigo());
                    configuracao.atualizarDe(request);
                    return configuracao;
                })
                .toList();

        return configuracaoService.salvar(configsAtualizadas).stream()
                .map(configuracaoMapper::paraDto)
                .toList();
    }
}
