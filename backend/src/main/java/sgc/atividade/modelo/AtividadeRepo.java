package sgc.atividade.modelo;

import org.springframework.data.jpa.repository.JpaRepository;
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
     * @param mapaCodigo id do mapa
     * @return lista de Atividade
     */
    List<Atividade> findByMapaCodigo(Long mapaCodigo);
}