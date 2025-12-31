package sgc.configuracao.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParametroRepo extends JpaRepository<Parametro, Long> {
    Optional<Parametro> findByChave(String chave);
}
