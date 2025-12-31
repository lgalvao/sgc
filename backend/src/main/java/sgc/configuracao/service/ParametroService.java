package sgc.configuracao.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.configuracao.model.Parametro;
import sgc.configuracao.model.ParametroRepo;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParametroService {

    private final ParametroRepo parametroRepo;

    public List<Parametro> buscarTodos() {
        return parametroRepo.findAll();
    }

    public Parametro buscarPorChave(String chave) {
        return parametroRepo.findByChave(chave)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Parâmetro não encontrado: " + chave));
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
