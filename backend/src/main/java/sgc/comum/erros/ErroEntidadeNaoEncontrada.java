package sgc.comum.erros;

import org.springframework.http.HttpStatus;

import java.util.function.Supplier;

/**
 * Exceção lançada quando um domínio (entidade) não é encontrado. Usada para sinalizar retornos 404
 * nas camadas de serviço/controle.
 */
public class ErroEntidadeNaoEncontrada extends ErroNegocioBase {

    public ErroEntidadeNaoEncontrada(String message) {
        super(message, "ENTIDADE_NAO_ENCONTRADA", HttpStatus.NOT_FOUND);
    }

    public ErroEntidadeNaoEncontrada(String entidade, Object codigo) {
        super("'%s' com codigo '%s' não encontrado(a).".formatted(entidade, codigo),
                "ENTIDADE_NAO_ENCONTRADA",
                HttpStatus.NOT_FOUND
        );
    }

    /**
     * Factory method para criar Supplier de exceção - uso com Optional.orElseThrow()
     * 
     * @param entidade Nome da entidade (ex: "Unidade", "Processo")
     * @param codigo Código/ID da entidade não encontrada
     * @return Supplier que cria a exceção quando invocado
     */
    public static Supplier<ErroEntidadeNaoEncontrada> naoEncontrada(String entidade, Object codigo) {
        return () -> new ErroEntidadeNaoEncontrada(entidade, codigo);
    }

    /**
     * Factory method para criar Supplier de exceção com mensagem customizada
     * 
     * @param mensagem Mensagem de erro customizada
     * @return Supplier que cria a exceção quando invocado
     */
    public static Supplier<ErroEntidadeNaoEncontrada> naoEncontrada(String mensagem) {
        return () -> new ErroEntidadeNaoEncontrada(mensagem);
    }
}
