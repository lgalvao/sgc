package sgc.competencia.modelo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório JPA para a entidade Competencia.
 */
@Repository
public interface CompetenciaRepo extends JpaRepository<Competencia, Long> {
    /**
     * Busca todas as competências de um mapa.
     *
     * @param mapaCodigo Código do mapa
     * @return Lista de competências do mapa
     */
    @Query("SELECT c FROM Competencia c WHERE c.mapa.codigo = :mapaCodigo")
    List<Competencia> findByMapaCodigo(@Param("mapaCodigo") Long mapaCodigo);

    /**
     * Busca todas as competências de um mapa ordenadas por descrição.
     *
     * @param mapaCodigo Código do mapa
     * @return Lista de competências do mapa ordenadas
     */
    @Query("SELECT c FROM Competencia c WHERE c.mapa.codigo = :mapaCodigo ORDER BY c.descricao")
    List<Competencia> findByMapaCodigoOrderByDescricao(@Param("mapaCodigo") Long mapaCodigo);
}
