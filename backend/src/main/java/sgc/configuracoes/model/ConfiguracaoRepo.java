package sgc.configuracoes.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfiguracaoRepo extends JpaRepository<Configuracao, Long> {
    Optional<Configuracao> findByChave(String chave);
}
