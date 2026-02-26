package sgc.alerta.model;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

/**
 * Repositório JPA para persistência de Notificações.
 */
@Repository
public interface NotificacaoRepo extends JpaRepository<Notificacao, Long> {
}
