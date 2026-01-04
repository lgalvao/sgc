package sgc.organizacao.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepo extends JpaRepository<Usuario, String> {
    Optional<Usuario> findByEmail(String email);

    List<Usuario> findByUnidadeLotacaoCodigo(Long codigoUnidade);

    @Query(
            "SELECT DISTINCT u FROM Usuario u " +
            "JOIN sgc.organizacao.model.UsuarioPerfil up ON up.usuarioTitulo = u.tituloEleitoral " +
            "WHERE up.unidadeCodigo = :codigoUnidade AND up.perfil = 'CHEFE'")
    Optional<Usuario> chefePorCodUnidade(@Param("codigoUnidade") Long codigoUnidade);

    @Query(
            "SELECT DISTINCT u FROM Usuario u " +
            "JOIN sgc.organizacao.model.UsuarioPerfil up ON up.usuarioTitulo = u.tituloEleitoral " +
            "WHERE up.unidadeCodigo IN :codigosUnidades AND up.perfil = 'CHEFE'")
    List<Usuario> findChefesByUnidadesCodigos(@Param("codigosUnidades") List<Long> codigosUnidades);

    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.atribuicoesTemporarias WHERE u.tituloEleitoral = :titulo")
    Optional<Usuario> findByIdWithAtribuicoes(@Param("titulo") String titulo);

    @Query("SELECT DISTINCT u FROM Usuario u " +
           "LEFT JOIN FETCH u.atribuicoesTemporarias " +
           "WHERE u.tituloEleitoral IN :titulos")
    List<Usuario> findByIdInWithAtribuicoes(@Param("titulos") List<String> titulos);
}
