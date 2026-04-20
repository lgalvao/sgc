package sgc.organizacao.model;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.*;
import org.springframework.stereotype.*;

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
            WHERE u.tituloEleitoral = :titulo
            """)
    Optional<Usuario> buscarPorTitulo(@Param("titulo") String titulo);

    @Query("""
            SELECT u FROM Usuario u
            JOIN FETCH u.unidadeLotacao
            WHERE u.tituloEleitoral = :titulo
            """)
    Optional<Usuario> buscarPorTituloComUnidadeLotacao(@Param("titulo") String titulo);

    @Query("""
            SELECT new sgc.organizacao.model.UsuarioConsultaLeitura(
                u.tituloEleitoral,
                u.matricula,
                u.nome,
                u.email,
                u.ramal,
                unidade.codigo,
                unidade.nome,
                unidade.sigla,
                unidade.tipo,
                unidade.tituloTitular,
                u.unidadeCompetenciaCodigo
            )
            FROM Usuario u
            JOIN u.unidadeLotacao unidade
            """)
    List<UsuarioConsultaLeitura> listarTodasConsultas();

    @Query("""
            SELECT DISTINCT u FROM Usuario u
            JOIN FETCH u.unidadeLotacao
            WHERE u.tituloEleitoral IN :titulos
            """)
    List<Usuario> listarPorTitulosComUnidadeLotacao(@Param("titulos") List<String> titulos);
}
