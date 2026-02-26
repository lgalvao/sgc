package sgc.organizacao.model;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

import java.util.*;

@Repository
public interface UnidadeMapaRepo extends JpaRepository<UnidadeMapa, Long> {
    @Query("SELECT um.unidadeCodigo FROM UnidadeMapa um")
    List<Long> findAllUnidadeCodigos();
}
