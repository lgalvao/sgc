package sgc.comum.erros;

/**
 * Exceção lançada quando um domínio (entidade) não é encontrado.
 * Usada para sinalizar retornos 404 nas camadas de serviço/controle.
 */
public class ErroEntidadeNaoEncontrada extends RuntimeException {
    public ErroEntidadeNaoEncontrada(String message) {
        super(message);
    }
}