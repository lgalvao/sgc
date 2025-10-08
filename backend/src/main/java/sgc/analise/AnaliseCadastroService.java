package sgc.analise;

import sgc.analise.modelo.AnaliseCadastro;

import java.util.List;

/**
 * Serviço para gerenciar análises de cadastro (ANALISE_CADASTRO). *
 */
public interface AnaliseCadastroService {

    /**
     * Recupera as análises vinculadas a um subprocesso.
     *
     * @param subprocessoCodigo id do subprocesso
     * @return lista de AnaliseCadastro
     */
    List<AnaliseCadastro> listarPorSubprocesso(Long subprocessoCodigo);

    /**
     * Cria e persiste uma nova análise de cadastro para o subprocesso informado.
     *
     * @param subprocessoCodigo id do subprocesso
     * @param observacoes       texto com observações da análise
     * @return entidade AnaliseCadastro persistida
     */
    AnaliseCadastro criarAnalise(Long subprocessoCodigo, String observacoes);

    /**
     * Remove todas as análises de cadastro vinculadas ao subprocesso.
     *
     * @param subprocessoCodigo id do subprocesso
     */
    void removerPorSubprocesso(Long subprocessoCodigo);
}