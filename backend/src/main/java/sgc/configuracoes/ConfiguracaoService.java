package sgc.configuracoes;

import lombok.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.erros.*;
import sgc.configuracoes.model.*;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ConfiguracaoService {
    private final ConfiguracaoRepo configuracaoRepo;

    public List<Configuracao> buscarTodos() {
        return configuracaoRepo.findAll();
    }

    public Configuracao buscarPorCodigo(Long codigo) {
        return configuracaoRepo.findById(codigo)
                .orElseThrow(() -> new ErroConfiguracao(
                        "Configuração com código '%d' não encontrado.".formatted(codigo)));
    }

    public Configuracao buscarPorChave(String chave) {
        return configuracaoRepo.findByChave(chave)
                .orElseThrow(() -> new ErroConfiguracao(
                        "Configuração '%s' não encontrado. Configure o Configuração no banco de dados.".formatted(chave)));
    }

    @Transactional
    public List<Configuracao> salvar(List<Configuracao> configuracaos) {
        return configuracaoRepo.saveAll(configuracaos);
    }

    @Transactional
    public Configuracao atualizar(String chave, String novoValor) {
        Configuracao configuracao = buscarPorChave(chave);
        configuracao.setValor(novoValor);
        return configuracaoRepo.save(configuracao);
    }
}
