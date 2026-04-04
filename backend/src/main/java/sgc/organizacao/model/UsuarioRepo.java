package sgc.organizacao.model;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.*;
import org.springframework.stereotype.*;
import org.springframework.data.domain.*;

import java.util.*;

@Repository
public interface UsuarioRepo extends JpaRepository<Usuario, String> {
    @Query("""
            SELECT u FROM Usuario u
            JOIN FETCH u.unidadeLotacao
            WHERE u.unidadeLotacao.codigo = :codigoUnidade
            """)
    List<Usuario> listarPorCodigoUnidadeLotacao(@Param("codigoUnidade") Long codigoUnidade);

    @Query("""
            SELECT u FROM Usuario u
            JOIN FETCH u.unidadeLotacao
            WHERE u.tituloEleitoral = :titulo
            """)
    Optional<Usuario> buscarPorTituloComUnidadeLotacao(@Param("titulo") String titulo);

    @Query("""
            SELECT DISTINCT u FROM Usuario u
            JOIN FETCH u.unidadeLotacao
            WHERE u.tituloEleitoral IN :titulos
            """)
    List<Usuario> listarPorTitulosComUnidadeLotacao(@Param("titulos") List<String> titulos);

    @Query("""
            SELECT DISTINCT u FROM Usuario u
            JOIN FETCH u.unidadeLotacao
            WHERE LOWER(u.nome) LIKE LOWER(CONCAT(:termo, '%'))
            ORDER BY u.nome
            """)
    List<Usuario> buscarPorNomeComUnidadeLotacao(@Param("termo") String termo, Pageable pageable);
}
