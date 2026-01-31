package sgc.configuracao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroConfiguracao;
import sgc.configuracao.model.Parametro;
import sgc.configuracao.model.ParametroRepo;

import java.util.List;

/**
 * Service responsável pelas operações de persistência de parâmetros de configuração.
 *
 * <p>Este service encapsula o acesso ao {@link ParametroRepo}, permitindo que
 * {@link ConfiguracaoFacade} delegue operações de dados sem acessar repositórios diretamente.
 *
 * @see ConfiguracaoFacade
 * @see ParametroRepo
 */
@Service
@RequiredArgsConstructor
public class ConfiguracaoService {

    private final ParametroRepo parametroRepo;

    /**
     * Busca todos os parâmetros de configuração.
     */
    public List<Parametro> buscarTodos() {
        return parametroRepo.findAll();
    }

    /**
     * Busca um parâmetro por código.
     *
     * @param codigo código do parâmetro
     * @return parâmetro encontrado
     * @throws ErroConfiguracao se o parâmetro não for encontrado
     */
    public Parametro buscarPorId(Long codigo) {
        return parametroRepo.findById(codigo)
                .orElseThrow(() -> new ErroConfiguracao(
                        "Parâmetro com código '%d' não encontrado.".formatted(codigo)));
    }

    /**
     * Busca um parâmetro por chave.
     *
     * @param chave chave do parâmetro
     * @return parâmetro encontrado
     * @throws ErroConfiguracao se o parâmetro não for encontrado
     */
    public Parametro buscarPorChave(String chave) {
        return parametroRepo.findByChave(chave)
                .orElseThrow(() -> new ErroConfiguracao(
                        "Parâmetro '%s' não encontrado. Configure o parâmetro no banco de dados.".formatted(chave)));
    }

    /**
     * Salva uma lista de parâmetros.
     *
     * @param parametros lista de parâmetros a salvar
     * @return lista de parâmetros salvos
     */
    @Transactional
    public List<Parametro> salvar(List<Parametro> parametros) {
        return parametroRepo.saveAll(parametros);
    }

    /**
     * Atualiza o valor de um parâmetro existente.
     *
     * @param chave chave do parâmetro
     * @param novoValor novo valor do parâmetro
     * @return parâmetro atualizado
     * @throws ErroConfiguracao se o parâmetro não for encontrado
     */
    @Transactional
    public Parametro atualizar(String chave, String novoValor) {
        Parametro parametro = buscarPorChave(chave);
        parametro.setValor(novoValor);
        return parametroRepo.save(parametro);
    }
}
