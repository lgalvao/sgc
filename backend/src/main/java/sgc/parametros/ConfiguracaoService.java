package sgc.parametros;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroConfiguracao;
import sgc.parametros.model.Parametro;
import sgc.parametros.model.ParametroRepo;

import java.util.List;

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
     */
    public Parametro buscarPorId(Long codigo) {
        return parametroRepo.findById(codigo)
                .orElseThrow(() -> new ErroConfiguracao(
                        "Parâmetro com código '%d' não encontrado.".formatted(codigo)));
    }

    /**
     * Busca um parâmetro por chave.
     */
    public Parametro buscarPorChave(String chave) {
        return parametroRepo.findByChave(chave)
                .orElseThrow(() -> new ErroConfiguracao(
                        "Parâmetro '%s' não encontrado. Configure o parâmetro no banco de dados.".formatted(chave)));
    }

    /**
     * Salva uma lista de parâmetros.
     */
    @Transactional
    public List<Parametro> salvar(List<Parametro> parametros) {
        return parametroRepo.saveAll(parametros);
    }

    /**
     * Atualiza o valor de um parâmetro existente.
     */
    @Transactional
    public Parametro atualizar(String chave, String novoValor) {
        Parametro parametro = buscarPorChave(chave);
        parametro.setValor(novoValor);
        return parametroRepo.save(parametro);
    }
}
