package sgc.atividade.modelo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repositório JPA para a entidade Atividade.
 */
@Repository
public interface AtividadeRepo extends JpaRepository<Atividade, Long> {
    /**
     * Recupera as atividades vinculadas a um mapa.
     *
     * @param mapaCodigo id do mapa
     * @return lista de Atividade
     */
    List<Atividade> findByMapaCodigo(Long mapaCodigo);

    Optional<Atividade> findByMapaCodigoAndDescricao(Long codigo, String descricao);

    @Query("SELECT a FROM Atividade a LEFT JOIN FETCH a.conhecimentos WHERE a.mapa.codigo = :mapaCodigo")
    List<Atividade> findByMapaCodigoWithConhecimentos(@Param("mapaCodigo") Long mapaCodigo);
}
