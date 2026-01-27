package sgc.comum.erros;

import org.springframework.http.HttpStatus;

/**
 * Exceção base para operações executadas em estado/situação inválido de workflow.
 *
 * <p>Esta é uma exceção <strong>abstrata</strong>. Use sempre subclasses específicas por domínio
 * (ex: ErroProcessoEmSituacaoInvalida, ErroMapaEmSituacaoInvalida).
 *
 * <p><strong>Quando usar:</strong>
 * <ul>
 *   <li>Tentativa de executar ação em estado de workflow incorreto</li>
 *   <li>Operação permitida apenas em determinadas situações</li>
 *   <li>Transição de estado inválida</li>
 * </ul>
 *
 * <p><strong>Importante:</strong> Este é um erro de negócio esperado (pode ocorrer com múltiplos
 * usuários ou condições de corrida), não um erro interno. Retorna HTTP 422 (Unprocessable Content).
 *
 * @see sgc.processo.erros.ErroProcessoEmSituacaoInvalida
 * @see sgc.subprocesso.erros.ErroMapaEmSituacaoInvalida
 * @see sgc.subprocesso.erros.ErroAtividadesEmSituacaoInvalida
 */
public abstract class ErroSituacaoInvalida extends ErroNegocioBase {

    protected ErroSituacaoInvalida(String message, String code) {
        super(message, code, HttpStatus.UNPROCESSABLE_CONTENT);
    }
}
