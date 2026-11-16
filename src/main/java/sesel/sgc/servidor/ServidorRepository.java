package sesel.sgc.servidor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Servidor entity.
 */
@Repository
public interface ServidorRepository extends JpaRepository<Servidor, UUID> {

    /**
     * Finds all Servidor instances associated with the given unidadeId.
     *
     * @param unidadeId the ID of the unidade to search for
     * @return a list of Servidor instances
     */
    List<Servidor> findByUnidadeId(UUID unidadeId);

    /**
     * Finds a Servidor instance by email.
     *
     * @param email the email to search for
     * @return an Optional containing the Servidor instance, or an empty Optional if not found
     */
    Optional<Servidor> findByEmail(String email);
}