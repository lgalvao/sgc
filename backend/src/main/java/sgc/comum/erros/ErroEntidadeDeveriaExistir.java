package sgc.comum.erros;

/**
 * Exceção lançada quando uma entidade deveria existir (por FK ou integridade referencial) mas não foi encontrada.
 * Indica bug, corrupção de dados ou violação de invariante.
 * 
 * <p>Use esta exceção em vez de ErroEntidadeNaoEncontrada quando:
 * <ul>
 *   <li>Mappers/DTOs buscam entidades por FK que deveriam existir</li>
 *   <li>Lookups internos de entidades relacionadas em workflows</li>
 *   <li>Validações de integridade referencial</li>
 * </ul>
 */
public class ErroEntidadeDeveriaExistir extends ErroInterno {

    
    public ErroEntidadeDeveriaExistir(String message) {
        super(message);
    }
    
    public ErroEntidadeDeveriaExistir(String entidade, Object codigo) {
        super(("Entidade '%s' com codigo '%s' deveria existir mas não foi encontrada. "
            + "Possível corrupção de dados ou violação de integridade referencial.")
            .formatted(entidade, codigo));
    }
    
    public ErroEntidadeDeveriaExistir(String entidade, Object codigo, String contexto) {
        super(("Entidade '%s' com codigo '%s' deveria existir mas não foi encontrada. "
            + "Contexto: %s. Possível corrupção de dados ou violação de integridade referencial.")
            .formatted(entidade, codigo, contexto));
    }
    
    public static ErroEntidadeDeveriaExistir fkViolada(String entidade, Object codigo, String origemFK) {
        return new ErroEntidadeDeveriaExistir(entidade, codigo, "FK violada em " + origemFK);
    }
    
    public static ErroEntidadeDeveriaExistir relacaoObrigatoria(String entidadePai, Object codigoPai,
                                                                 String entidadeFilha) {
        return new ErroEntidadeDeveriaExistir(entidadeFilha, "N/A",
            "%s %s deveria ter %s associado(a)".formatted(entidadePai, codigoPai, entidadeFilha));
    }
}
