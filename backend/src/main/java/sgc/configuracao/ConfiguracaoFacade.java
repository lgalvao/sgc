package sgc.configuracao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sgc.configuracao.dto.ParametroRequest;
import sgc.configuracao.mapper.ParametroMapper;
import sgc.configuracao.model.Parametro;

import java.util.List;

/**
 * Facade para gerenciamento de configurações do sistema.
 *
 * <p>Esta facade orquestra operações relacionadas a parâmetros de configuração,
 * delegando a persistência para {@link ConfiguracaoService}.
 *
 * @see ConfiguracaoService
 */
@Service
@RequiredArgsConstructor
public class ConfiguracaoFacade {
    private final ConfiguracaoService configuracaoService;
    private final ParametroMapper parametroMapper;

    /**
     * Busca todos os parâmetros de configuração.
     */
    public List<Parametro> buscarTodos() {
        return configuracaoService.buscarTodos();
    }

    /**
     * Busca um parâmetro por chave.
     */
    public Parametro buscarPorChave(String chave) {
        return configuracaoService.buscarPorChave(chave);
    }

    /**
     * Salva uma lista de parâmetros.
     */
    public List<Parametro> salvar(List<ParametroRequest> requests) {
        // Buscar parâmetros existentes e atualizar com dados das requests
        List<Parametro> parametros = requests.stream()
                .map(request -> {
                    Parametro parametro = configuracaoService.buscarPorId(request.codigo());
                    parametroMapper.atualizarEntidade(request, parametro);
                    return parametro;
                })
                .toList();
        
        List<Parametro> parametrosSalvos = configuracaoService.salvar(parametros);
        
        return parametrosSalvos;
    }

    /**
     * Atualiza o valor de um parâmetro existente.
     */
    public Parametro atualizar(String chave, String novoValor) {
        return configuracaoService.atualizar(chave, novoValor);
    }
}
