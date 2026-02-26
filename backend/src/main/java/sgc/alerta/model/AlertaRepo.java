package sgc.alerta.model;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.*;
import org.springframework.stereotype.*;

import java.util.*;

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
