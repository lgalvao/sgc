package sgc.mapa.model;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.*;
import org.springframework.stereotype.*;

import java.util.*;

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
            LEFT JOIN FETCH a.competencias ac
            LEFT JOIN FETCH m.competencias c
            LEFT JOIN FETCH c.atividades ca
            WHERE m.subprocesso.codigo = :subprocessoCodigo
            """)
    Optional<Mapa> findFullBySubprocessoCodigo(@Param("subprocessoCodigo") Long subprocessoCodigo);
}
