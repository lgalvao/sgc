package sgc.alerta.modelo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório para a entidade {@link AlertaUsuario}.
 */
@Repository
public interface AlertaUsuarioRepo extends JpaRepository<AlertaUsuario, AlertaUsuario.Chave> {
    /**
     * Busca todas as associações de Alerta-Usuário para um determinado alerta.
     * @param alertaCodigo O código do alerta.
     * @return Uma lista de {@link AlertaUsuario}.
     */
    List<AlertaUsuario> findById_AlertaCodigo(Long alertaCodigo);

    /**
     * Busca todas as associações de Alerta-Usuário para um determinado usuário.
     * @param usuarioTituloEleitoral O título de eleitor do usuário.
     * @return Uma lista de {@link AlertaUsuario}.
     */
    List<AlertaUsuario> findById_UsuarioTituloEleitoral(Long usuarioTituloEleitoral);
    
    /**
     * Deleta todas as associações de usuários para os alertas especificados.
     * @param alertaCodigos Lista de códigos de alertas.
     */
    @Modifying
    @Query("DELETE FROM AlertaUsuario au WHERE au.id.alertaCodigo IN :alertaCodigos")
    void deleteByIdAlertaCodigoIn(@Param("alertaCodigos") List<Long> alertaCodigos);
}