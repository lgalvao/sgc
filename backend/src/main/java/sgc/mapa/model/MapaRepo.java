package sgc.mapa.model;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.*;
import org.springframework.stereotype.*;

import java.util.*;

@Repository
public interface MapaRepo extends JpaRepository<Mapa, Long> {
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
            SELECT m FROM Mapa m
            JOIN FETCH m.subprocesso subprocesso
            WHERE subprocesso.codigo IN :codigosSubprocessos
            """)
    List<Mapa> listarPorSubprocessos(@Param("codigosSubprocessos") Collection<Long> codigosSubprocessos);

    @Query("""
            SELECT DISTINCT subprocesso.unidade.codigo
            FROM Mapa mapa
            JOIN mapa.subprocesso subprocesso
            JOIN subprocesso.processo processo
            WHERE processo.dataFinalizacao IS NOT NULL
            """)
    List<Long> buscarCodigosUnidadesComMapaVigente();

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
            LEFT JOIN FETCH m.atividades a
            LEFT JOIN FETCH a.conhecimentos k
            LEFT JOIN FETCH a.competencias ac
            LEFT JOIN FETCH m.competencias c
            LEFT JOIN FETCH c.atividades ca
            WHERE m.codigo = :codigo
            """)
    Optional<Mapa> buscarCompletoPorCodigo(@Param("codigo") Long codigo);

    @Query("""
            SELECT DISTINCT m FROM Mapa m
            LEFT JOIN FETCH m.subprocesso
            LEFT JOIN FETCH m.competencias c
            LEFT JOIN FETCH c.atividades a
            WHERE m.subprocesso.codigo = :subprocessoCodigo
            """)
    Optional<Mapa> buscarComCompetenciasEAtividadesPorSubprocesso(@Param("subprocessoCodigo") Long subprocessoCodigo);
}
