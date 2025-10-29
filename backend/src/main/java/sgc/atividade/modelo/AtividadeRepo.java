package sgc.atividade.modelo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório JPA para a entidade Atividade.
 */
@Repository
public interface AtividadeRepo extends JpaRepository<Atividade, Long> {
    /**
     * Recupera as atividades vinculadas a um mapa.
     *
     * @param mapaCodigo codigo do mapa
     * @return lista de Atividade
     */
    List<Atividade> findByMapaCodigo(Long mapaCodigo);

    @Query("SELECT a FROM Atividade a LEFT JOIN FETCH a.conhecimentos WHERE a.mapa.codigo = :mapaCodigo")
    List<Atividade> findByMapaCodigoWithConhecimentos(@Param("mapaCodigo") Long mapaCodigo);
}
