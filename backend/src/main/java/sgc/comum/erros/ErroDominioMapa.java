package sgc.comum.erros;

/**
 * Exceção lançada quando há erro de domínio relacionado ao mapa de competências.
 * Usada para validações de integridade do mapa (competências sem atividades, etc).
 */
public class ErroDominioMapa extends RuntimeException {
    public ErroDominioMapa(String message) {
        super(message);
    }
}