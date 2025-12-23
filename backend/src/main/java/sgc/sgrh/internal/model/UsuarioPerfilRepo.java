package sgc.sgrh.internal.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioPerfilRepo extends JpaRepository<UsuarioPerfil, UsuarioPerfilId> {
    List<UsuarioPerfil> findByUsuarioTitulo(String usuarioTitulo);
}
