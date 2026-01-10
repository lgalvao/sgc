package sgc.alerta.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório para a entidade {@link Alerta}.
 */
@Repository
public interface AlertaRepo extends JpaRepository<Alerta, Long> {
    /**
     * Busca todos os alertas associados a um processo específico.
     *
     * @param codProcesso O código do processo.
     * @return Uma lista de alertas.
     */
    List<Alerta> findByProcessoCodigo(Long codProcesso);

    /**
     * Busca alertas destinados a uma unidade específica.
     *
     * @param codUnidade O código da unidade.
     * @return Lista de alertas para a unidade.
     */
    List<Alerta> findByUnidadeDestino_Codigo(Long codUnidade);

    /**
     * Busca alertas destinados a uma unidade específica, de forma paginada.
     *
     * @param codUnidade O código da unidade.
     * @param pageable   Informações de paginação.
     * @return Uma página de alertas.
     */
    Page<Alerta> findByUnidadeDestino_Codigo(Long codUnidade, Pageable pageable);
}
