package sgc.comum.modelo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepo extends JpaRepository<Usuario, String> {
    Optional<Usuario> findByTitulo(String titulo);
}
