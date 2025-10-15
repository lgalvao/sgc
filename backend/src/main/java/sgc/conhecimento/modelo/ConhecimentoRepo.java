package sgc.conhecimento.modelo;
 
import org.springframework.data.jpa.repository.JpaRepository;
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
     * @param atividadeCodigo id da atividade
     * @return lista de Conhecimento
     */
    List<Conhecimento> findByAtividadeCodigo(Long atividadeCodigo);

    List<Conhecimento> findByAtividadeCodigoIn(List<Long> atividadeCodigos);

    @org.springframework.data.jpa.repository.Query("SELECT c FROM Conhecimento c JOIN c.atividade a WHERE a.mapa.codigo = :idMapa")
    List<Conhecimento> findByMapaCodigo(@org.springframework.data.repository.query.Param("idMapa") Long idMapa);
}