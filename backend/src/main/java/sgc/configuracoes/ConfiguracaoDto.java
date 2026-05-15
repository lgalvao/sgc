package sgc.configuracoes;

import lombok.*;
import sgc.configuracoes.model.*;

@Builder
public record ConfiguracaoDto(
        Long codigo,
        String chave,
        String descricao,
        String valor) {

    public static ConfiguracaoDto fromEntity(Configuracao configuracao) {
        return ConfiguracaoDto.builder()
                .codigo(configuracao.getCodigo())
                .chave(configuracao.getChave())
                .descricao(configuracao.getDescricao())
                .valor(configuracao.getValor())
                .build();
    }
}

