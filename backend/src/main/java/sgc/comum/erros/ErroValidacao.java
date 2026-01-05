package sgc.comum.erros;

import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Exceção para erros de validação de dados de entrada ou regras de negócio.
 *
 * <p>Retorna HTTP 422 (Unprocessable Content) indicando que a requisição está sintaticamente
 * correta mas semanticamente inválida.
 *
 * <p>Pode incluir detalhes adicionais sobre campos específicos que falharam na validação.
 */
public class ErroValidacao extends ErroNegocioBase {
    
    public ErroValidacao(String message) {
        super(message, "VALIDACAO", HttpStatus.UNPROCESSABLE_CONTENT);
    }

    public ErroValidacao(String message, Map<String, ?> details) {
        super(message, "VALIDACAO", HttpStatus.UNPROCESSABLE_CONTENT, details);
    }
}
