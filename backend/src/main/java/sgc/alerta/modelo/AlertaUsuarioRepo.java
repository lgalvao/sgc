package sgc.alerta.modelo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
/**
 * Repositório para a entidade {@link AlertaUsuario}.
 */
public interface AlertaUsuarioRepo extends JpaRepository<AlertaUsuario, AlertaUsuario.Chave> {
    /**
     * Busca todas as associações de Alerta-Usuário para um determinado alerta.
     * @param alertaCodigo O código do alerta.
     * @return Uma lista de {@link AlertaUsuario}.
     */
    List<AlertaUsuario> findById_AlertaCodigo(Long alertaCodigo);
}