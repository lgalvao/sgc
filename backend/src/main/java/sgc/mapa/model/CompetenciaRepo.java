package sgc.mapa.model;

import org.springframework.data.jpa.repository.EntityGraph;
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
     * Busca todas as competências de um mapa, incluindo suas atividades associadas.
     *
     * @param mapaCodigo Código do mapa
     * @return Lista de competências do mapa
     */
    @EntityGraph(attributePaths = {"atividades"})
    List<Competencia> findByMapaCodigo(@Param("mapaCodigo") Long mapaCodigo);

    /**
     * Busca dados projetados (id, descricao, id_atividade) das competências de um mapa.
     * Otimizado para visualização, evitando carregar entidades Atividade completas duplicadas.
     *
     * @param mapaCodigo Código do mapa
     * @return Lista de arrays de objetos [Long competenciaId, String descricao, Long atividadeId]
     */
    @Query("""
            SELECT c.codigo, c.descricao, a.codigo
            FROM Competencia c
            LEFT JOIN c.atividades a
            WHERE c.mapa.codigo = :mapaCodigo
            """)
    List<Object[]> findCompetenciaAndAtividadeIdsByMapaCodigo(@Param("mapaCodigo") Long mapaCodigo);

    /**
     * Busca competências de um mapa sem carregar relacionamentos.
     *
     * @param mapaCodigo Código do mapa
     * @return Lista de competências
     */
    @Query("SELECT c FROM Competencia c WHERE c.mapa.codigo = :mapaCodigo")
    List<Competencia> findByMapaCodigoSemFetch(@Param("mapaCodigo") Long mapaCodigo);
}
