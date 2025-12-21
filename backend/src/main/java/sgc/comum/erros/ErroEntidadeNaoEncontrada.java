package sgc.comum.erros;

import org.springframework.http.HttpStatus;

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
}
