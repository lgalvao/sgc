package sgc.analise;

import sgc.analise.modelo.AnaliseValidacao;

import java.util.List;

/**
 * Serviço para gerenciar análises de validação (ANALISE_VALIDACAO). *
 */
public interface AnaliseValidacaoService {

    /**
     * Recupera as análises de validação vinculadas a um subprocesso.
     *
     * @param subprocessoCodigo id do subprocesso
     * @return lista de AnaliseValidacao
     */
    List<AnaliseValidacao> listarPorSubprocesso(Long subprocessoCodigo);

    /**
     * Cria e persiste uma nova análise de validação para o subprocesso informado.
     *
     * @param subprocessoCodigo id do subprocesso
     * @param observacoes       texto com observações da análise
     * @return entidade AnaliseValidacao persistida
     */
    AnaliseValidacao criarAnalise(Long subprocessoCodigo, String observacoes);

    /**
     * Remove todas as análises de validação vinculadas ao subprocesso.
     *
     * @param subprocessoCodigo id do subprocesso
     */
    void removerPorSubprocesso(Long subprocessoCodigo);
}