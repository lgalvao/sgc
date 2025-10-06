package sgc.notificacao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório JPA para persistência de Notificações.
 */
@Repository
public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {
}