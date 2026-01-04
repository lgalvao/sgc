package sgc.organizacao.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioPerfilRepo extends JpaRepository<UsuarioPerfil, UsuarioPerfilId> {
    List<UsuarioPerfil> findByUsuarioTitulo(String usuarioTitulo);

    @Query("SELECT up FROM UsuarioPerfil up WHERE up.usuarioTitulo IN :titulos")
    List<UsuarioPerfil> findByUsuarioTituloIn(@Param("titulos") List<String> titulos);
}
