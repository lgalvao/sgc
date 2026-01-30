package sgc.configuracao.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * DTO de resposta para parâmetros de configuração.
 * Utilizado na listagem e retorno de parâmetros salvos.
 */
@Getter
@Builder
public class ParametroResponse {
    private Long codigo;
    private String chave;
    private String descricao;
    private String valor;
}
