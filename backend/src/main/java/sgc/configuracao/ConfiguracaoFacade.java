package sgc.configuracao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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

    /**
     * Busca todos os parâmetros de configuração.
     *
     * @return lista com todos os parâmetros
     */
    public List<Parametro> buscarTodos() {
        return configuracaoService.buscarTodos();
    }

    /**
     * Busca um parâmetro por chave.
     *
     * @param chave chave do parâmetro
     * @return parâmetro encontrado
     */
    public Parametro buscarPorChave(String chave) {
        return configuracaoService.buscarPorChave(chave);
    }

    /**
     * Salva uma lista de parâmetros.
     *
     * @param parametros lista de parâmetros a salvar
     * @return lista de parâmetros salvos
     */
    public List<Parametro> salvar(List<Parametro> parametros) {
        return configuracaoService.salvar(parametros);
    }

    /**
     * Atualiza o valor de um parâmetro existente.
     *
     * @param chave chave do parâmetro
     * @param novoValor novo valor do parâmetro
     * @return parâmetro atualizado
     */
    public Parametro atualizar(String chave, String novoValor) {
        return configuracaoService.atualizar(chave, novoValor);
    }
}
