package sgc.alerta.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório para a entidade {@link AlertaUsuario}.
 */
@Repository
public interface AlertaUsuarioRepo extends JpaRepository<AlertaUsuario, AlertaUsuario.Chave> {
    @Query("SELECT au FROM AlertaUsuario au WHERE au.id.usuarioTitulo = :usuarioTitulo AND au.id.alertaCodigo IN :alertaCodigos")
    List<AlertaUsuario> findByUsuarioAndAlertas(@Param("usuarioTitulo") String usuarioTitulo, @Param("alertaCodigos") List<Long> alertaCodigos);
}
