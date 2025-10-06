package sgc.mapa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório JPA para a entidade Mapa.

 */
@Repository
public interface MapaRepository extends JpaRepository<Mapa, Long> {
}