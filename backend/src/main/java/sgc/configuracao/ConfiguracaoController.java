package sgc.configuracao;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import sgc.configuracao.dto.ParametroRequest;
import sgc.configuracao.mapper.ParametroMapper;
import sgc.configuracao.model.ConfiguracaoViews;
import sgc.configuracao.model.Parametro;

import java.util.List;

@RestController
@RequestMapping("/api/configuracoes")
@RequiredArgsConstructor
@Tag(name = "Configurações", description = "Gerenciamento de configurações")
@Validated
public class ConfiguracaoController {
    private final ConfiguracaoService configuracaoService;
    private final ParametroMapper parametroMapper;

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
                    parametroMapper.atualizarEntidade(request, parametro);
                    return parametro;
                })
                .toList();
        
        return configuracaoService.salvar(parametrosAtualizados);
    }
}
