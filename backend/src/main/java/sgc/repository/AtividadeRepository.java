package sgc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sgc.model.Atividade;

import java.util.List;

/**
 * Repositório JPA para a entidade Atividade.
 * Nomes e documentação em português conforme solicitado.
 */
@Repository
public interface AtividadeRepository extends JpaRepository<Atividade, Long> {

    /**
     * Recupera as atividades vinculadas a um mapa.
     *
     * @param mapaCodigo id do mapa
     * @return lista de Atividade
     */
    List<Atividade> findByMapaCodigo(Long mapaCodigo);
}