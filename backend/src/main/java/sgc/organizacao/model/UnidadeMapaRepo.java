package sgc.organizacao.model;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

import java.util.*;

@Repository
public interface UnidadeMapaRepo extends JpaRepository<UnidadeMapa, Long> {
    @Query("SELECT um.unidadeCodigo FROM UnidadeMapa um")
    List<Long> findAllUnidadeCodigos();

    default Optional<UnidadeMapa> findByUnidadeCodigo(Long unidadeCodigo) {
        return findById(unidadeCodigo);
    }

    default boolean existsByUnidadeCodigo(Long unidadeCodigo) {
        return existsById(unidadeCodigo);
    }

    default List<UnidadeMapa> findAllByUnidadeCodigoIn(List<Long> unidadeCodigos) {
        return findAllById(unidadeCodigos);
    }
}
