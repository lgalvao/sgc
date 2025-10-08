package sgc.sgrh.modelo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sgc.sgrh.modelo.VwUsuarioPerfilUnidade.VwUsuarioPerfilUnidadeId;

import java.util.List;

/**
 * Repository READ-ONLY para view VW_USUARIO_PERFIL_UNIDADE do SGRH.
 * <p>
 * IMPORTANTE: Este repository acessa dados do Oracle SGRH.
 * Todas as operações são somente leitura (read-only).
 */
@Repository
public interface VwUsuarioPerfilUnidadeRepo
    extends JpaRepository<VwUsuarioPerfilUnidade, VwUsuarioPerfilUnidadeId> {
    
    /**
     * Busca perfis ativos de um usuário.
     */
    List<VwUsuarioPerfilUnidade> findByIdUsuarioTituloAndAtivoTrue(String titulo);
    
    /**
     * Busca usuários ativos com perfil em uma unidade.
     */
    List<VwUsuarioPerfilUnidade> findByIdUnidadeCodigoAndAtivoTrue(Long unidadeCodigo);
    
    /**
     * Busca usuários com perfil específico em uma unidade.
     */
    List<VwUsuarioPerfilUnidade> findByIdUnidadeCodigoAndPerfilAndAtivoTrue(
        Long unidadeCodigo, 
        String perfil
    );
    
    /**
     * Busca todas as unidades onde o usuário tem um perfil específico.
     */
    List<VwUsuarioPerfilUnidade> findByIdUsuarioTituloAndPerfilAndAtivoTrue(
        String titulo, 
        String perfil
    );
    
    /**
     * Verifica se usuário tem perfil específico em uma unidade.
     */
    @Query("SELECT COUNT(u) > 0 FROM VwUsuarioPerfilUnidade u WHERE " +
           "u.id.usuarioTitulo = :titulo AND u.id.unidadeCodigo = :unidadeCodigo AND " +
           "u.perfil = :perfil AND u.ativo = true")
    boolean existsByUsuarioUnidadeAndPerfil(
        @Param("titulo") String titulo,
        @Param("unidadeCodigo") Long unidadeCodigo,
        @Param("perfil") String perfil
    );
    
    /**
     * Lista todos os perfis ativos.
     */
    List<VwUsuarioPerfilUnidade> findByAtivoTrue();
}