package sgc.configuracoes;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroConfiguracao;
import sgc.configuracoes.model.Configuracao;
import sgc.configuracoes.model.ConfiguracaoRepo;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConfiguracaoService {
    public static final String CHAVE_DIAS_INATIVACAO_PROCESSO = "DIAS_INATIVACAO_PROCESSO";
    public static final String CHAVE_DIAS_ALERTA_NOVO = "DIAS_ALERTA_NOVO";
    public static final int PADRAO_DIAS_INATIVACAO_PROCESSO = 10;
    public static final int PADRAO_DIAS_ALERTA_NOVO = 3;

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

    @Transactional(readOnly = true)
    public int buscarDiasInativacaoProcesso() {
        return buscarValorInteiro(CHAVE_DIAS_INATIVACAO_PROCESSO, PADRAO_DIAS_INATIVACAO_PROCESSO);
    }

    @Transactional(readOnly = true)
    public int buscarDiasAlertaNovo() {
        return buscarValorInteiro(CHAVE_DIAS_ALERTA_NOVO, PADRAO_DIAS_ALERTA_NOVO);
    }

    @Transactional(readOnly = true)
    public int buscarValorInteiro(String chave, int valorPadrao) {
        Optional<Configuracao> configuracaoOpt = configuracaoRepo.findByChave(chave);
        if (configuracaoOpt.isEmpty()) {
            return valorPadrao;
        }

        String valor = configuracaoOpt.get().getValor();
        try {
            int valorConvertido = Integer.parseInt(valor);
            if (valorConvertido < 1) {
                throw new ErroConfiguracao("Configuração '%s' deve ser maior ou igual a 1.".formatted(chave));
            }
            return valorConvertido;
        } catch (NumberFormatException e) {
            throw new ErroConfiguracao("Configuração '%s' possui valor inválido: '%s'.".formatted(chave, valor));
        }
    }
}
