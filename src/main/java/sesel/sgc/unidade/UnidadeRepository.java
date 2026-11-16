package sesel.sgc.unidade;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing Unidade entities.
 */
@Repository
public interface UnidadeRepository extends JpaRepository<Unidade, UUID> {

    /**
     * Finds a Unidade by its name.
     *
     * @param nome the name of the unidade to find (case-sensitive)
     * @return an Optional containing the found unidade or empty if not found
     */
    Optional<Unidade> findByNome(String nome);

    @Query("SELECT u FROM Unidade u LEFT JOIN FETCH u.unidadesSubordinadas")
    List<Unidade> findAllWithSubordinadas();
}