package sgc.comum.erros;

/**
 * Exceção lançada quando uma invariante do sistema é violada.
 *
 * <p>Invariantes são condições que devem sempre ser verdadeiras se o sistema está funcionando
 * corretamente. A violação indica corrupção de dados, falha de integridade referencial ou bug
 * no código.
 *
 * <p><strong>Exemplos de uso:</strong>
 * <ul>
 *   <li>Entidade obrigatória (FK) não encontrada quando deveria existir</li>
 *   <li>Dados que deveriam ser mutuamente exclusivos mas existem simultaneamente</li>
 *   <li>Contador ou soma que não bate com valores esperados</li>
 *   <li>Estrutura de dados corrompida (lista vazia quando deveria ter itens)</li>
 * </ul>
 *
 * <p>Geralmente indica que validações ou constraints do banco falharam, ou há um bug na
 * lógica de negócio que permitiu o sistema chegar em estado inconsistente.
 */
public class ErroInvarianteViolada extends ErroInterno {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    public ErroInvarianteViolada(String message) {
        super(message);
    }
}
