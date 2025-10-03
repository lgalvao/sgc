package sgc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sgc.model.Mapa;

/**
 * Repositório JPA para a entidade Mapa.
 * Nomes e documentação em português conforme solicitado.
 */
@Repository
public interface MapaRepository extends JpaRepository<Mapa, Long> {
}