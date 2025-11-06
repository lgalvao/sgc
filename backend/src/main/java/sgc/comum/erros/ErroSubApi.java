package sgc.comum.erros;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Representa um erro específico de validação dentro de uma estrutura de erro de API.
 * <p>
 * Esta classe encapsula detalhes de um erro de validação de campo em uma requisição,
 * incluindo qual objeto foi validado, qual campo falhou, qual valor foi rejeitado,
 * e a mensagem de erro correspondente. Utilizada pela {@link ErroApi} para detalhar
 * erros de validação em requisições HTTP.
 * <p>
 * Exemplo de uso: quando uma requisição contém um campo inválido, um {@link ErroSubApi}
 * é criado para comunicar ao cliente qual campo específico falhou a validação.
 *
 * @see ErroApi
 */
@Data
@AllArgsConstructor
public class ErroSubApi {
    private String object;
    private String field;
    private Object rejectedValue;
    private String message;
}