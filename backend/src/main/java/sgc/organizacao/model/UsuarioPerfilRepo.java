package sgc.organizacao.model;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.*;
import org.springframework.stereotype.*;

import java.util.*;

@Repository
public interface UsuarioPerfilRepo extends JpaRepository<UsuarioPerfil, UsuarioPerfilId> {
    List<UsuarioPerfil> findByUsuarioTitulo(String usuarioTitulo);

    @Query("""
            SELECT up.perfil
            FROM UsuarioPerfil up
            WHERE up.usuarioTitulo = :usuarioTitulo
            """)
    List<Perfil> listarPerfisPorUsuarioTitulo(@Param("usuarioTitulo") String usuarioTitulo);

    @Query("""
            SELECT new sgc.organizacao.model.UsuarioPerfilAutorizacaoLeitura(
                up.usuarioTitulo,
                up.perfil,
                u.codigo,
                u.nome,
                u.sigla,
                u.tipo,
                u.situacao
            )
            FROM UsuarioPerfil up
            JOIN up.unidade u
            WHERE up.usuarioTitulo = :usuarioTitulo
            """)
    List<UsuarioPerfilAutorizacaoLeitura> listarAutorizacoesPorUsuarioTitulo(@Param("usuarioTitulo") String usuarioTitulo);
}
