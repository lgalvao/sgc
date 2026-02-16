package sgc.mapa.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MapaRepo extends JpaRepository<Mapa, Long> {
    @Query("""
            SELECT um.mapaVigente FROM UnidadeMapa um
            WHERE um.unidadeCodigo = :unidadeCodigo
            """)
    Optional<Mapa> findMapaVigenteByUnidade(@Param("unidadeCodigo") Long unidadeCodigo);

    @Query("""
            SELECT m FROM Mapa m
            WHERE m.subprocesso.codigo = :subprocessoCodigo
            """)
    Optional<Mapa> findBySubprocessoCodigo(@Param("subprocessoCodigo") Long subprocessoCodigo);

    @Query("""
            SELECT DISTINCT m FROM Mapa m
            LEFT JOIN FETCH m.atividades a
            LEFT JOIN FETCH a.conhecimentos k
            WHERE m.subprocesso.codigo = :subprocessoCodigo
            """)
    Optional<Mapa> findFullBySubprocessoCodigo(@Param("subprocessoCodigo") Long subprocessoCodigo);
}
