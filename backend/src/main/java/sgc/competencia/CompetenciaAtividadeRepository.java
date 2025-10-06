package sgc.competencia;

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
public interface CompetenciaAtividadeRepository extends JpaRepository<CompetenciaAtividade, CompetenciaAtividade.Id> {
    
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
     * Remove todos os vínculos de uma competência.
     *
     * @param competenciaCodigo Código da competência
     */
    @Modifying
    @Query("DELETE FROM CompetenciaAtividade ca WHERE ca.id.competenciaCodigo = :competenciaCodigo")
    void deleteByCompetenciaCodigo(@Param("competenciaCodigo") Long competenciaCodigo);
}