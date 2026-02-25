package sgc.organizacao.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepo extends JpaRepository<Usuario, String> {
    List<Usuario> findByUnidadeLotacaoCodigo(Long codigoUnidade);

    @Query("""
            SELECT u FROM Usuario u WHERE u.tituloEleitoral = :titulo
            """)
    Optional<Usuario> findByIdWithAtribuicoes(@Param("titulo") String titulo);

    @Query("""
            SELECT DISTINCT u FROM Usuario u
            WHERE u.tituloEleitoral IN :titulos
            """)
    List<Usuario> findByIdInWithAtribuicoes(@Param("titulos") List<String> titulos);
}
