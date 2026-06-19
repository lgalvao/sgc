package sgc.configuracoes;

import org.springframework.stereotype.Component;
import sgc.configuracoes.model.Configuracao;

@Component
public class ConfiguracaoMapper {

    public ConfiguracaoDto paraDto(Configuracao configuracao) {
        return ConfiguracaoDto.builder()
                .codigo(configuracao.getCodigo())
                .chave(configuracao.getChave())
                .descricao(configuracao.getDescricao())
                .valor(configuracao.getValor())
                .build();
    }
}
