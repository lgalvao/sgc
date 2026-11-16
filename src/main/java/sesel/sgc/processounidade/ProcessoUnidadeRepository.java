package sesel.sgc.processounidade;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProcessoUnidadeRepository extends JpaRepository<ProcessoUnidade, UUID> {
    @Query("""
            SELECT p FROM ProcessoUnidade p
            JOIN FETCH p.unidade u
            LEFT JOIN FETCH u.titular
            WHERE p.processo.id = :processoId AND p.unidade.id = :unidadeId
            """)
    Optional<ProcessoUnidade> findByProcessoIdAndUnidadeId(
            @Param("processoId") UUID processoId,
            @Param("unidadeId") UUID unidadeId
    );

    @Query("""
            SELECT p FROM ProcessoUnidade p
            JOIN FETCH p.unidade u
            LEFT JOIN FETCH u.titular
            WHERE p.processo.id = :processoId
            """)
    List<ProcessoUnidade> findByProcessoId(@Param("processoId") UUID processoId);
}
