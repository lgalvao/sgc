package sgc.organizacao.model;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.*;
import org.springframework.stereotype.*;

import java.util.*;

@Repository
public interface UsuarioPerfilRepo extends JpaRepository<UsuarioPerfil, UsuarioPerfilId> {
    List<UsuarioPerfil> findByUsuarioTitulo(String usuarioTitulo);

    @Query("""
            SELECT new sgc.organizacao.model.UsuarioPerfilAutorizacaoLeitura(
                p.usuarioTitulo,
                p.perfil,
                p.unidadeCodigo,
                u.nome,
                u.sigla,
                u.tipo,
                u.situacao
            )
            FROM UsuarioPerfil p
            LEFT JOIN Unidade u ON u.codigo = p.unidadeCodigo
            WHERE p.usuarioTitulo = :usuarioTitulo
            """)
    List<UsuarioPerfilAutorizacaoLeitura> listarAutorizacoesPorTitulo(@Param("usuarioTitulo") String usuarioTitulo);
}
