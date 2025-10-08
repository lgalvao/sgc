package sgc.analise.modelo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório para registros de análise de validação (ANALISE_VALIDACAO).
 */
@Repository
public interface AnaliseValidacaoRepo extends JpaRepository<AnaliseValidacao, Long> {

    /**
     * Remove todas as análises de validação relacionadas ao subprocesso informado.
     *
     * @param subprocessoCodigo id do subprocesso
     */
    @Modifying
    @Query("DELETE FROM AnaliseValidacao av WHERE av.subprocesso.codigo = :codigo")
    void deleteBySubprocesso_Codigo(@Param("codigo") Long subprocessoCodigo);

    /**
     * Recupera análises de validação vinculadas a um subprocesso.
     *
     * @param subprocessoCodigo id do subprocesso
     * @return lista de AnaliseValidacao
     */
    List<AnaliseValidacao> findBySubprocesso_Codigo(Long subprocessoCodigo);

    /**
     * Recupera análises de validação ordenadas por data decrescente (CDU-20 item 6).
     *
     * @param subprocessoCodigo id do subprocesso
     * @return lista de AnaliseValidacao ordenada por data decrescente
     */
    List<AnaliseValidacao> findBySubprocesso_CodigoOrderByDataHoraDesc(Long subprocessoCodigo);
}