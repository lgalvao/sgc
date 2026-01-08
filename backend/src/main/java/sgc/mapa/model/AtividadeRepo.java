package sgc.mapa.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AtividadeRepo extends JpaRepository<Atividade, Long> {
    @Override
    @Query("""
            SELECT a FROM Atividade a LEFT JOIN FETCH a.mapa
            """)
    List<Atividade> findAll();

    @Query("""
            SELECT DISTINCT a FROM Atividade a LEFT JOIN FETCH a.competencias WHERE a.mapa.codigo = :mapaCodigo
            """)
    List<Atividade> findByMapaCodigo(@Param("mapaCodigo") Long mapaCodigo);

    @Query("""
            SELECT DISTINCT a FROM Atividade a LEFT JOIN FETCH a.conhecimentos WHERE a.mapa.codigo = :mapaCodigo
            """)
    List<Atividade> findByMapaCodigoWithConhecimentos(@Param("mapaCodigo") Long mapaCodigo);

    @Query("""
            SELECT a FROM Atividade a WHERE a.mapa.codigo = (SELECT s.mapa.codigo FROM Subprocesso s WHERE s.codigo = :subprocessoCodigo)
            """)
    List<Atividade> findBySubprocessoCodigo(@Param("subprocessoCodigo") Long subprocessoCodigo);

    @Query("""
            SELECT DISTINCT a FROM Atividade a JOIN FETCH a.competencias WHERE :competencia MEMBER OF a.competencias
            """)
    List<Atividade> listarPorCompetencia(@Param("competencia") Competencia competencia);
}
