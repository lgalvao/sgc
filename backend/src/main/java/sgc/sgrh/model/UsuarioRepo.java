package sgc.sgrh.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepo extends JpaRepository<Usuario, String> {
    Optional<Usuario> findByTituloEleitoral(String tituloEleitoral);
    List<Usuario> findByUnidadeCodigo(Long codigoUnidade);
}
