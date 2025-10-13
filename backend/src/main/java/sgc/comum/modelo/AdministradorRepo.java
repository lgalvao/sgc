package sgc.comum.modelo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sgc.sgrh.Usuario;

@Repository
public interface AdministradorRepo extends JpaRepository<Administrador, String> {
    boolean existsByUsuario(Usuario usuario);
}