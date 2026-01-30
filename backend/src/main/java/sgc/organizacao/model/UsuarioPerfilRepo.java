package sgc.organizacao.model;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioPerfilRepo extends JpaRepository<UsuarioPerfil, UsuarioPerfilId> {
    List<UsuarioPerfil> findByUsuarioTitulo(String usuarioTitulo);

    /**
     * Busca perfis de usuário com eager loading de unidade.
     * Evita N+1 queries ao carregar as unidades relacionadas em uma única query.
     *
     * @param usuarioTitulo título eleitoral do usuário
     * @return lista de perfis do usuário com unidades carregadas
     */
    @EntityGraph(attributePaths = {"unidade"})
    @Query("SELECT up FROM UsuarioPerfil up WHERE up.usuarioTitulo = :usuarioTitulo")
    List<UsuarioPerfil> findByUsuarioTituloWithUnidade(@Param("usuarioTitulo") String usuarioTitulo);

    @Query("""
            SELECT up FROM UsuarioPerfil up WHERE up.usuarioTitulo IN :titulos
            """)
    List<UsuarioPerfil> findByUsuarioTituloIn(@Param("titulos") List<String> titulos);
}
