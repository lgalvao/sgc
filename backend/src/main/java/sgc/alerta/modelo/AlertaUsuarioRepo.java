package sgc.alerta.modelo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertaUsuarioRepo extends JpaRepository<AlertaUsuario, AlertaUsuario.Chave> {
    List<AlertaUsuario> findByIdUsuarioTitulo(String usuarioTitulo);
    List<AlertaUsuario> findByIdAlertaCodigo(Long alertaCodigo);
}