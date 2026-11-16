package sesel.sgc.processo;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sesel.sgc.mapa.MapaUnidadeSituacao;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProcessoRepository extends JpaRepository<Processo, UUID> {
    @EntityGraph(attributePaths = { "processosUnidades", "processosUnidades.unidade",
            "processosUnidades.unidade.titular" })
    @Query("""
               SELECT DISTINCT p FROM Processo p \
                LEFT JOIN FETCH p.processosUnidades processoUnidade \
                LEFT JOIN FETCH processoUnidade.unidade unidade \
                LEFT JOIN FETCH unidade.titular titular \
                WHERE p.id = :id
            """)
    Optional<Processo> findByIdWithProcessoUnidadesAndUnidades(@Param("id") UUID id);

    @EntityGraph(attributePaths = { "processosUnidades", "processosUnidades.unidade" })
    @Query("""
            SELECT DISTINCT p FROM Processo p
                LEFT JOIN FETCH p.processosUnidades processoUnidade
                LEFT JOIN FETCH processoUnidade.unidade
            WHERE p.id = :id
            """)
    Optional<Processo> findByIdWithProcessoUnidades(@Param("id") UUID id);

    @Query("SELECT COUNT(p) FROM ProcessoUnidade p WHERE p.processo.id = :processoId")
    long countProcessoUnidadesByProcessoId(@Param("processoId") UUID processoId);

    @Query("""
            SELECT COUNT(p) FROM ProcessoUnidade p \
            WHERE p.processo.id = :processoId AND \
                  p.situacao = :situacao
            """)
    long countProcessoUnidadesByProcessoIdAndSituacao(
            @Param("processoId") UUID processoId,
            @Param("situacao") MapaUnidadeSituacao situacao);

    long countBySituacao(ProcessoSituacao situacao);
}
