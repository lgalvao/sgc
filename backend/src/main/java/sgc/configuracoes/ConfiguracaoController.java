package sgc.configuracoes;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import sgc.configuracoes.model.Configuracao;

import java.util.List;

@RestController
@RequestMapping("/api/configuracoes")
@RequiredArgsConstructor
@Tag(name = "Configurações", description = "Gerenciamento de configurações")
@Validated
public class ConfiguracaoController {
    private final ConfiguracaoService configuracaoService;
    private final ConfiguracaoMapper configuracaoMapper;

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
    public List<ConfiguracaoDto> atualizar(@RequestBody @Valid List<ConfiguracaoRequest> parametros) {
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
