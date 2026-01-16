package sgc.configuracao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.configuracao.model.Parametro;
import sgc.configuracao.model.ParametroRepo;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConfiguracaoFacade {

    private final ParametroRepo parametroRepo;

    public List<Parametro> buscarTodos() {
        return parametroRepo.findAll();
    }

    public Parametro buscarPorChave(String chave) {
        return parametroRepo.findByChave(chave)
                .orElseThrow(() -> new sgc.comum.erros.ErroConfiguracao(
                        "Parâmetro '%s' não encontrado. Configure o parâmetro no banco de dados.".formatted(chave)));
    }

    @Transactional
    public List<Parametro> salvar(List<Parametro> parametros) {
        return parametroRepo.saveAll(parametros);
    }

    @Transactional
    public Parametro atualizar(String chave, String novoValor) {
        Parametro parametro = buscarPorChave(chave);
        parametro.setValor(novoValor);
        return parametroRepo.save(parametro);
    }
}
