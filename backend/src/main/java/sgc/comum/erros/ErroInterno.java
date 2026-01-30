package sgc.comum.erros;

/**
 * Exceção base para erros internos que indicam bugs, configuração incorreta ou violação de
 * invariantes do sistema.
 *
 * <p>Estas exceções <strong>NÃO</strong> são erros de negócio esperados. Representam situações que
 * nunca deveriam ocorrer se o sistema estiver configurado e funcionando corretamente. Sempre
 * resultam em HTTP 500 e são logadas como ERROR com traceId.
 *
 * <p><strong>Quando usar:</strong>
 * <ul>
 *   <li>Configuração inválida ou ausente (ex: JWT secret não configurado)</li>
 *   <li>Violação de invariantes do sistema (ex: dados corrompidos, FK inválida)</li>
 *   <li>Estado que deveria ser impossível se o UI funciona corretamente (programação defensiva)</li>
 *   <li>Condições que indicam bugs no código</li>
 * </ul>
 *
 * <p><strong>Quando NÃO usar:</strong>
 * <ul>
 *   <li>Validações de entrada do usuário → Use {@link ErroValidacao}</li>
 *   <li>Recursos não encontrados → Use {@link ErroEntidadeNaoEncontrada}</li>
 *   <li>Operações em estado inválido de workflow → Use exceções específicas como ErroProcessoEmSituacaoInvalida</li>
 *   <li>Violações de permissão → Use {@link ErroAcessoNegado}</li>
 * </ul>
 *
 * <p><strong>Tratamento:</strong>
 * Erros internos sempre geram logs detalhados com stack trace e traceId para depuração,
 * mas retornam mensagem genérica ao usuário para não expor detalhes de implementação.
 *
 * @see ErroConfiguracao
 * @see ErroInvarianteViolada
 * @see ErroEstadoImpossivel
 */
public abstract class ErroInterno extends RuntimeException {

    /**
     * Construtor com mensagem de erro.
     * A mensagem será logada mas não exposta ao usuário final.
     *
     * @param message descrição técnica do erro para logs e depuração
     */
    protected ErroInterno(String message) {
        super(message);
    }
}
