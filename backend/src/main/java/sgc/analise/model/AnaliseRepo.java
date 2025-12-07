package sgc.analise.model;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repositório para a entidade {@link Analise}. */
@Repository
public interface AnaliseRepo extends JpaRepository<Analise, Long> {
    /**
     * Deleta todas as análises associadas a um subprocesso.
     *
     * @param codSubprocesso O código do subprocesso.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Analise a WHERE a.subprocesso.codigo = :codSubprocesso")
    void deleteBySubprocessoCodigo(@Param("codSubprocesso") Long codSubprocesso);

    /**
     * Busca todas as análises de um subprocesso, ordenadas pela data e hora em ordem decrescente.
     *
     * @param codSubprocesso O código do subprocesso.
     * @return Uma lista de análises.
     */
    List<Analise> findBySubprocessoCodigoOrderByDataHoraDesc(Long codSubprocesso);

    /**
     * Busca todas as análises de um subprocesso.
     *
     * @param codSubprocesso O código do subprocesso.
     * @return Uma lista de análises.
     */
    List<Analise> findBySubprocessoCodigo(Long codSubprocesso);
}
