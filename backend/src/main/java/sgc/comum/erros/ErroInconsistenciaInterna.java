package sgc.comum.erros;

/**
 * Exceção para invariantes quebradas ou estados persistidos inconsistentes que indicam bug no sistema.
 */
public class ErroInconsistenciaInterna extends ErroInterno {
    public ErroInconsistenciaInterna(String message) {
        super(message);
    }
}
