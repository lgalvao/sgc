package sgc.subprocesso;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sgc.atividade.AnaliseValidacao;

import java.util.List;

/**
 * Repositório para registros de análise de validação (ANALISE_VALIDACAO).
 */
@Repository
public interface AnaliseValidacaoRepository extends JpaRepository<AnaliseValidacao, Long> {

    /**
     * Remove todas as análises de validação relacionadas ao subprocesso informado.
     *
     * @param subprocessoCodigo id do subprocesso
     */
    void deleteBySubprocessoCodigo(Long subprocessoCodigo);

    /**
     * Recupera análises de validação vinculadas a um subprocesso.
     *
     * @param subprocessoCodigo id do subprocesso
     * @return lista de AnaliseValidacao
     */
    List<AnaliseValidacao> findBySubprocessoCodigo(Long subprocessoCodigo);
}