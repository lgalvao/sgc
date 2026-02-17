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
    List<Alerta> findByProcessoCodigo(Long codProcesso);

    /**
     * Carrega eagerly as entidades relacionadas (JOIN FETCH) para evitar LazyInitializationException.
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
     * Versão paginada. JOIN FETCH para evitar LazyInitializationException.
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
