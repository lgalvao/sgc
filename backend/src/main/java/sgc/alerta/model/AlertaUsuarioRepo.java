package sgc.alerta.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório para a entidade {@link AlertaUsuario}.
 */
@Repository
public interface AlertaUsuarioRepo extends JpaRepository<AlertaUsuario, AlertaUsuario.Chave> {
}
