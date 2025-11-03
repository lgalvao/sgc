package sgc.sgrh.modelo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepo extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByTituloEleitoral(long l);
    List<Usuario> findByUnidadeCodigo(Long codigoUnidade);
}
