package sgc.parametros.model;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

import java.util.*;

@Repository
public interface ParametroRepo extends JpaRepository<Parametro, Long> {
    Optional<Parametro> findByChave(String chave);
}
