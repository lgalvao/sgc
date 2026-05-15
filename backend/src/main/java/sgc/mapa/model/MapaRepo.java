package sgc.mapa.model;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.*;
import org.springframework.stereotype.*;

import java.util.*;

@Repository
public interface MapaRepo extends JpaRepository<Mapa, Long> {
    @Query("""
            SELECT DISTINCT m.subprocesso.unidade.codigo
            FROM Mapa m
            WHERE m.subprocesso.unidade.codigo IS NOT NULL
            """)
    List<Long> listarCodigosUnidadesComHistoricoMapa();

    @Query("""
            SELECT um.mapaVigente FROM UnidadeMapa um
            LEFT JOIN FETCH um.mapaVigente.subprocesso
            WHERE um.unidadeCodigo = :unidadeCodigo
            """)
    Optional<Mapa> buscarMapaVigentePorUnidade(@Param("unidadeCodigo") Long unidadeCodigo);

    @Query("""
            SELECT m FROM Mapa m
            LEFT JOIN FETCH m.subprocesso
            WHERE m.subprocesso.codigo = :subprocessoCodigo
            """)
    Optional<Mapa> buscarPorSubprocesso(@Param("subprocessoCodigo") Long subprocessoCodigo);

    @Query("""
            SELECT DISTINCT m FROM Mapa m
            LEFT JOIN FETCH m.atividades a
            LEFT JOIN FETCH a.conhecimentos k
            LEFT JOIN FETCH a.competencias ac
            LEFT JOIN FETCH m.competencias c
            LEFT JOIN FETCH c.atividades ca
            WHERE m.subprocesso.codigo = :subprocessoCodigo
            """)
    Optional<Mapa> buscarCompletoPorSubprocesso(@Param("subprocessoCodigo") Long subprocessoCodigo);

    @Query("""
            SELECT DISTINCT m FROM Mapa m
            LEFT JOIN FETCH m.subprocesso
            LEFT JOIN FETCH m.competencias c
            LEFT JOIN FETCH c.atividades a
            WHERE m.subprocesso.codigo = :subprocessoCodigo
            """)
    Optional<Mapa> buscarComCompetenciasEAtividadesPorSubprocesso(@Param("subprocessoCodigo") Long subprocessoCodigo);
}
