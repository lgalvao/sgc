package sgc.atividade.api.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório JPA para a entidade Conhecimento.
 */
@Repository
public interface ConhecimentoRepo extends JpaRepository<Conhecimento, Long> {
    /**
     * Recupera conhecimentos vinculados a uma atividade.
     *
     * @param atividadeCodigo codigo da atividade
     * @return lista de Conhecimento
     */
    List<Conhecimento> findByAtividadeCodigo(Long atividadeCodigo);

    @Query("SELECT c FROM Conhecimento c JOIN c.atividade a WHERE a.mapa.codigo = :codMapa")
    List<Conhecimento> findByMapaCodigo(@Param("codMapa") Long codMapa);

    long countByAtividadeCodigo(Long atividadeCodigo);
}
