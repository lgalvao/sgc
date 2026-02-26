package sgc.parametros;

import lombok.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.erros.*;
import sgc.parametros.model.*;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ConfiguracaoService {
    private final ParametroRepo parametroRepo;

    public List<Parametro> buscarTodos() {
        return parametroRepo.findAll();
    }

    public Parametro buscarPorId(Long codigo) {
        return parametroRepo.findById(codigo)
                .orElseThrow(() -> new ErroConfiguracao(
                        "Parâmetro com código '%d' não encontrado.".formatted(codigo)));
    }

    public Parametro buscarPorChave(String chave) {
        return parametroRepo.findByChave(chave)
                .orElseThrow(() -> new ErroConfiguracao(
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
