package sgc.comum.erros;

/**
 * Exceção lançada quando há problemas de configuração do sistema.
 *
 * <p>Indica que uma propriedade obrigatória está ausente, inválida ou com valor inadequado
 * que impede o funcionamento correto da aplicação.
 *
 * <p><strong>Exemplos de uso:</strong>
 * <ul>
 *   <li>JWT secret não configurado ou muito curto</li>
 *   <li>Propriedades de ambiente ausentes</li>
 *   <li>Configurações de banco de dados inválidas</li>
 *   <li>Valores de configuração fora dos limites esperados</li>
 * </ul>
 *
 * <p>Geralmente detectada durante a inicialização da aplicação ou primeiro uso de um componente.
 */
public class ErroConfiguracao extends ErroInterno {
    public ErroConfiguracao(String message) {
        super(message);
    }
}
