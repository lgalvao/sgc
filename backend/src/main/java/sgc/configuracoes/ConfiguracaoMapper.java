package sgc.configuracoes;

import org.springframework.stereotype.*;
import sgc.configuracoes.model.*;

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
