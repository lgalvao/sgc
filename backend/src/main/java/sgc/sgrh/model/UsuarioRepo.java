package sgc.sgrh.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepo extends JpaRepository<Usuario, String> {
    Optional<Usuario> findByEmail(String email);

    List<Usuario> findByUnidadeLotacaoCodigo(Long codigoUnidade);

    @Query("SELECT u FROM Usuario u JOIN u.atribuicoes a WHERE a.unidade.codigo = :codigoUnidade AND a.perfil = 'CHEFE'")
    Optional<Usuario> findChefeByUnidadeCodigo(@Param("codigoUnidade") Long codigoUnidade);

    @Query("SELECT u FROM Usuario u JOIN u.atribuicoes a WHERE a.unidade.codigo IN :codigosUnidades AND a.perfil = 'CHEFE'")
    List<Usuario> findChefesByUnidadesCodigos(@Param("codigosUnidades") List<Long> codigosUnidades);
}
