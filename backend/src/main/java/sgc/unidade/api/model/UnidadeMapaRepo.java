package sgc.unidade.api.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UnidadeMapaRepo extends JpaRepository<UnidadeMapa, Long> {

    @Query("SELECT um.unidadeCodigo FROM UnidadeMapa um")
    List<Long> findAllUnidadeCodigos();
}
