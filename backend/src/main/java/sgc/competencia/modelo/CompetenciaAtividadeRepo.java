package sgc.competencia.modelo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório JPA para a associação CompetenciaAtividade (N-N).
 * Usa a chave composta CompetenciaAtividade.Id.
 */
@Repository
public interface CompetenciaAtividadeRepo extends JpaRepository<CompetenciaAtividade, CompetenciaAtividade.Id> {
    /**
     * Busca todos os vínculos de uma competência.
     *
     * @param competenciaCodigo Código da competência
     * @return Lista de vínculos da competência com atividades
     */
    @Query("SELECT ca FROM CompetenciaAtividade ca WHERE ca.id.competenciaCodigo = :competenciaCodigo")
    List<CompetenciaAtividade> findByCompetenciaCodigo(@Param("competenciaCodigo") Long competenciaCodigo);
    
    /**
     * Busca todos os vínculos de uma atividade.
     *
     * @param atividadeCodigo Código da atividade
     * @return Lista de vínculos da atividade com competências
     */
    @Query("SELECT ca FROM CompetenciaAtividade ca WHERE ca.id.atividadeCodigo = :atividadeCodigo")
    List<CompetenciaAtividade> findByAtividadeCodigo(@Param("atividadeCodigo") Long atividadeCodigo);
    
    /**
     * Verifica se existe vínculo para uma atividade.
     *
     * @param atividadeCodigo Código da atividade
     * @return true se existe ao menos um vínculo
     */
    @Query("SELECT CASE WHEN COUNT(ca) > 0 THEN true ELSE false END FROM CompetenciaAtividade ca WHERE ca.id.atividadeCodigo = :atividadeCodigo")
    boolean existsByAtividadeCodigo(@Param("atividadeCodigo") Long atividadeCodigo);

    /**
     * Conta o número de competências associadas a uma atividade.
     *
     * @param atividadeCodigo O código da atividade.
     * @return O número de competências associadas.
     */
    long countByAtividadeCodigo(Long atividadeCodigo);

    /**
     * Conta o número de atividades associadas a uma competência.
     *
     * @param competenciaCodigo O código da competência.
     * @return O número de atividades associadas.
     */
    long countByCompetenciaCodigo(Long competenciaCodigo);
    
    /**
     * Remove todos os vínculos de uma competência.
     *
     * @param competenciaCodigo Código da competência
     */
    @Modifying
    @Query("DELETE FROM CompetenciaAtividade ca WHERE ca.id.competenciaCodigo = :competenciaCodigo")
    void deleteByCompetenciaCodigo(@Param("competenciaCodigo") Long competenciaCodigo);

    @Query("SELECT ca FROM CompetenciaAtividade ca JOIN Atividade a ON ca.id.atividadeCodigo = a.codigo WHERE a.mapa.codigo = :idMapa")
    List<CompetenciaAtividade> findByMapaCodigo(@Param("idMapa") Long idMapa);

    @Modifying
    @Query("DELETE FROM CompetenciaAtividade ca WHERE ca.id.competenciaCodigo IN (SELECT c.codigo FROM Competencia c WHERE c.mapa.codigo = :mapaCodigo)")
    void deleteByCompetenciaMapaCodigo(@Param("mapaCodigo") Long mapaCodigo);
}