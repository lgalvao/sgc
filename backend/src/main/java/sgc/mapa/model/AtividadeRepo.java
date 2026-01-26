package sgc.mapa.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AtividadeRepo extends JpaRepository<Atividade, Long> {
    @Query("""
            SELECT a FROM Atividade a
            LEFT JOIN FETCH a.mapa
            """)
    List<Atividade> findAllWithMapa();

    @Query("""
            SELECT DISTINCT a FROM Atividade a
            LEFT JOIN FETCH a.competencias
            WHERE a.mapa.codigo = :mapaCodigo
            """)
    List<Atividade> findByMapaCodigo(@Param("mapaCodigo") Long mapaCodigo);

    @Query("SELECT a FROM Atividade a WHERE a.mapa.codigo = :mapaCodigo")
    List<Atividade> findByMapaCodigoSemFetch(@Param("mapaCodigo") Long mapaCodigo);

    @Query("""
            SELECT DISTINCT a FROM Atividade a
            LEFT JOIN FETCH a.conhecimentos
            WHERE a.mapa.codigo = :mapaCodigo
            """)
    List<Atividade> findByMapaCodigoWithConhecimentos(@Param("mapaCodigo") Long mapaCodigo);

    @Query("""
            SELECT a FROM Atividade a
            JOIN Subprocesso s ON a.mapa.codigo = s.mapa.codigo
            WHERE s.codigo = :subprocessoCodigo
            """)
    List<Atividade> findBySubprocessoCodigo(@Param("subprocessoCodigo") Long subprocessoCodigo);

    @Query("""
            SELECT DISTINCT a FROM Atividade a
            JOIN FETCH a.competencias
            WHERE :competencia MEMBER OF a.competencias
            """)
    List<Atividade> listarPorCompetencia(@Param("competencia") Competencia competencia);

    long countByMapaCodigo(Long mapaCodigo);
}
