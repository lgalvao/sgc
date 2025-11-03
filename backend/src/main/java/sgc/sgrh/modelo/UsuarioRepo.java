package sgc.sgrh.modelo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sgc.sgrh.modelo.Usuario;

import java.util.Optional;

@Repository
public interface UsuarioRepo extends JpaRepository<Usuario, Long> {
    // findById is provided by JpaRepository
}
