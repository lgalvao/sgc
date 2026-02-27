package sgc.subprocesso.model;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.*;
import org.springframework.stereotype.*;

import java.util.*;

/**
 * Repositório JPA para a entidade Movimentacao. Inclui query para recuperar movimentações de um
 * subprocesso em ordem decrescente de data/hora.
 */
@Repository
public interface MovimentacaoRepo extends JpaRepository<Movimentacao, Long> {
    @Query("""
            SELECT m FROM Movimentacao m
            LEFT JOIN FETCH m.unidadeOrigem
            LEFT JOIN FETCH m.unidadeDestino
            WHERE m.subprocesso.codigo = :subprocessoCodigo
            ORDER BY m.dataHora DESC, m.codigo DESC
            """)
    List<Movimentacao> findBySubprocessoCodigoOrderByDataHoraDesc(@Param("subprocessoCodigo") Long subprocessoCodigo);

    @Query("""
            SELECT m FROM Movimentacao m
            LEFT JOIN FETCH m.unidadeOrigem
            LEFT JOIN FETCH m.unidadeDestino
            WHERE m.subprocesso.codigo = :subprocessoCodigo
            ORDER BY m.dataHora DESC, m.codigo DESC
            LIMIT 1
            """)
    Optional<Movimentacao> findFirstBySubprocessoCodigoOrderByDataHoraDesc(@Param("subprocessoCodigo") Long subprocessoCodigo);

    List<Movimentacao> findBySubprocessoCodigo(Long subprocessoCodigo);
}
