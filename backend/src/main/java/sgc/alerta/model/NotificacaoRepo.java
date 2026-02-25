package sgc.alerta.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório JPA para persistência de Notificações.
 */
@Repository
public interface NotificacaoRepo extends JpaRepository<Notificacao, Long> {
}
