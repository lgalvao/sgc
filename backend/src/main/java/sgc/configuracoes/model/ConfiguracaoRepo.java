package sgc.configuracoes.model;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

import java.util.*;

@Repository
public interface ConfiguracaoRepo extends JpaRepository<Configuracao, Long> {
    Optional<Configuracao> findByChave(String chave);
}
