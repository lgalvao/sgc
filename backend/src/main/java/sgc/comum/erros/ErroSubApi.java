package sgc.comum.erros;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Representa um erro específico de validação dentro de uma estrutura de erro de API.
 *
 * <p>Esta classe encapsula detalhes de um erro de validação de campo em uma requisição, incluindo
 * qual objeto foi validado, qual campo falhou, e a mensagem de erro correspondente.
 *
 * <p>Exemplo de uso: quando uma requisição contém um campo inválido, um {@link ErroSubApi} é criado
 * para comunicar ao cliente qual campo específico falhou a validação.
 *
 * @see ErroApi
 */
@Data
@AllArgsConstructor
public class ErroSubApi {
    private String object;
    private String field;
    private String message;
}
