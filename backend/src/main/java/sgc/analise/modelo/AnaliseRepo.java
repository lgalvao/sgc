package sgc.analise.modelo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório para a entidade {@link Analise}.
 */
@Repository
public interface AnaliseRepo extends JpaRepository<Analise, Long> {
    /**
     * Deleta todas as análises associadas a um subprocesso.
     * @param codSuprocesso O código do subprocesso.
     */
    void deleteBySubprocessoCodigo(Long codSuprocesso);

    /**
     * Busca todas as análises de um subprocesso, ordenadas pela data e hora em ordem decrescente.
     * @param codSuprocesso O código do subprocesso.
     * @return Uma lista de análises.
     */
    List<Analise> findBySubprocessoCodigoOrderByDataHoraDesc(Long codSuprocesso);

    /**
     * Busca todas as análises de um subprocesso.
     * @param codSubprocesso O código do subprocesso.
     * @return Uma lista de análises.
     */
    List<Analise> findBySubprocesso_Codigo(Long codSubprocesso);
}
