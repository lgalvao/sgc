package sgc.mapa;

/**
 * Exceção lançada quando há erro de domínio relacionado ao mapa de competências.
 * Usada para validações de integridade do mapa (competências sem atividades, etc).
 */
public class ErroMapa extends RuntimeException {
    public ErroMapa(String message) {
        super(message);
    }
}