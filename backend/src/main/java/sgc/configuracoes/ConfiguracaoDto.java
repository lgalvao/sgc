package sgc.configuracoes;

import lombok.Builder;

@Builder
public record ConfiguracaoDto(
        Long codigo,
        String chave,
        String descricao,
        String valor) {
}
