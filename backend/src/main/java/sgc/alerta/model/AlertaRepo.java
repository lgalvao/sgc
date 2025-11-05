package sgc.alerta.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório para a entidade {@link Alerta}.
 */
@Repository
public interface AlertaRepo extends JpaRepository<Alerta, Long> {
    /**
     * Busca alertas destinados a uma unidade específica, de forma paginada.
     * @param unidadeCodigo O código da unidade de destino.
     * @param pageable      Informações de paginação.
     * @return Uma página de alertas.
     */
    Page<Alerta> findByUnidadeDestino_Codigo(Long unidadeCodigo, Pageable pageable);

    /**
     * Busca todos os alertas associados a um processo específico.
     * @param codProcesso O código do processo.
     * @return Uma lista de alertas.
     */
    List<Alerta> findByProcessoCodigo(Long codProcesso);

    @Query("select a.codigo from Alerta a where a.processo.codigo = :proc")
    List<Long> findIdsByProcessoCodigo(@Param("proc") Long processoCodigo);

    @Query("select a.codigo from Alerta a where a.processo.codigo in :procs")
    List<Long> findIdsByProcessoCodigoIn(@Param("procs") List<Long> processosCodigo);

    @Modifying
    @Query("delete from Alerta a where a.processo.codigo = :proc")
    void deleteByProcessoCodigo(@Param("proc") Long processoCodigo);

    @Modifying
    @Query("delete from Alerta a where a.processo.codigo in :procs")
    void deleteByProcessoCodigoIn(@Param("procs") List<Long> processosCodigo);

    /**
     * Busca alertas destinados a um usuário específico, de forma paginada.
     * @param tituloEleitoral O título de eleitor do usuário.
     * @param pageable        Informações de paginação.
     * @return Uma página de alertas.
     */
    Page<Alerta> findByUsuarioDestino_TituloEleitoral(Long tituloEleitoral, Pageable pageable);

    /**
     * Busca alertas destinados a uma lista de unidades, de forma paginada.
     * @param unidadeCodigos A lista de códigos de unidades.
     * @param pageable       Informações de paginação.
     * @return Uma página de alertas.
     */
    Page<Alerta> findByUnidadeDestino_CodigoIn(List<Long> unidadeCodigos, Pageable pageable);
}