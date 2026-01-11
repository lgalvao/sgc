package sgc.comum.erros;

/**
 * Exceção lançada quando o código atinge um estado que deveria ser impossível.
 *
 * <p>Usada para programação defensiva quando certas condições são teoricamente impossíveis
 * se o UI está funcionando corretamente ou se validações anteriores foram executadas.
 *
 * <p><strong>Exemplos de uso:</strong>
 * <ul>
 *   <li>Switch/case sem match quando todos os casos deveriam estar cobertos</li>
 *   <li>Valor de enum desconhecido (que não deveria existir)</li>
 *   <li>Estado de entidade que o UI impede mas backend valida defensivamente</li>
 *   <li>Combinação de parâmetros que o frontend nunca deveria enviar</li>
 * </ul>
 *
 * <p><strong>Distinção importante:</strong>
 * Se o estado é <em>possível em uso normal</em> (ex: condição de corrida, múltiplos usuários),
 * então NÃO é um ErroEstadoImpossivel, é um erro de negócio e deve usar exceções apropriadas
 * como ErroProcessoEmSituacaoInvalida.
 *
 * <p>Esta exceção indica que há um bug no código, no UI, ou dados foram manipulados
 * externamente (ex: via SQL direto no banco).
 */
public class ErroEstadoImpossivel extends ErroInterno {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    public ErroEstadoImpossivel(String message) {
        super(message);
    }
}
