package sgc.comum.erros;

/**
 * Representa uma violação de uma regra de negócio da aplicação.
 * <p>
 * Lançada quando uma operação não pode ser concluída porque viola
 * uma pré-condição ou estado do sistema (e.g., tentar iniciar um processo
 * que já está em andamento).
 */
// TODO em vez dessa classe geral demais, melhor criar erros mais específicos.
public class ErroNegocio extends RuntimeException {
    public ErroNegocio(String message) {
        super(message);
    }
}
