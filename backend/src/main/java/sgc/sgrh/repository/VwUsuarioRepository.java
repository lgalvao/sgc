package sgc.sgrh.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sgc.sgrh.entity.VwUsuario;

import java.util.List;
import java.util.Optional;

/**
 * Repository READ-ONLY para view VW_USUARIO do SGRH.
 * <p>
 * IMPORTANTE: Este repository acessa dados do Oracle SGRH.
 * Todas as operações são somente leitura (read-only).
 */
@Repository
public interface VwUsuarioRepository extends JpaRepository<VwUsuario, String> {
    
    /**
     * Busca usuário por título (CPF).
     */
    Optional<VwUsuario> findByTitulo(String titulo);
    
    /**
     * Busca usuário por email.
     */
    Optional<VwUsuario> findByEmail(String email);
    
    /**
     * Lista todos os usuários ativos.
     */
    List<VwUsuario> findByAtivoTrue();
    
    /**
     * Busca usuário por matrícula.
     */
    Optional<VwUsuario> findByMatricula(String matricula);
}