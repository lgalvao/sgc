package sgc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sgc.model.Usuario;

/**
 * Repositório JPA para a entidade Usuario.
 * Nomes e documentação em português conforme solicitado.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> {
}