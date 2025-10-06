package sgc.alerta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertaUsuarioRepository extends JpaRepository<AlertaUsuario, AlertaUsuario.Id> {
    List<AlertaUsuario> findByIdUsuarioTitulo(String usuarioTitulo);
    List<AlertaUsuario> findByIdAlertaCodigo(Long alertaCodigo);
}