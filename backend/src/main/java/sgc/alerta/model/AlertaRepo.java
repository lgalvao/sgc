package sgc.alerta.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
     * Busca todos os alertas associados a um processo específico.
     *
     * @param codProcesso O código do processo.
     * @return Uma lista de alertas.
     */
    List<Alerta> findByProcessoCodigo(Long codProcesso);

    /**
     * Busca alertas destinados a uma unidade específica.
     * Carrega eagerly as entidades relacionadas para evitar LazyInitializationException.
     * Usa INNER JOIN para garantir que apenas alertas com processos válidos sejam retornados.
     *
     * @param codUnidade O código da unidade.
     * @return Lista de alertas para a unidade.
     */
    @Query("""
        SELECT DISTINCT a FROM Alerta a
        JOIN FETCH a.processo
        JOIN FETCH a.unidadeOrigem
        JOIN FETCH a.unidadeDestino
        WHERE a.unidadeDestino.codigo = :codUnidade
        """)
    List<Alerta> findByUnidadeDestino_Codigo(@Param("codUnidade") Long codUnidade);

    /**
     * Busca alertas destinados a uma unidade específica, de forma paginada.
     * Carrega eagerly as entidades relacionadas para evitar LazyInitializationException.
     * Usa INNER JOIN para garantir que apenas alertas com processos válidos sejam retornados.
     *
     * @param codUnidade O código da unidade.
     * @param pageable   Informações de paginação.
     * @return Uma página de alertas.
     */
    @Query(value = """
        SELECT DISTINCT a FROM Alerta a
        JOIN FETCH a.processo
        JOIN FETCH a.unidadeOrigem
        JOIN FETCH a.unidadeDestino
        WHERE a.unidadeDestino.codigo = :codUnidade
        """,
        countQuery = """
        SELECT COUNT(a) FROM Alerta a
        WHERE a.unidadeDestino.codigo = :codUnidade
        AND a.processo IS NOT NULL
        """)
    Page<Alerta> findByUnidadeDestino_Codigo(@Param("codUnidade") Long codUnidade, Pageable pageable);
}
