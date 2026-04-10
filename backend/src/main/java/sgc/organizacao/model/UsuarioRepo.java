package sgc.organizacao.model;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.*;
import org.springframework.stereotype.*;
import sgc.organizacao.dto.*;

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
                unidade.tituloTitular
            )
            FROM Usuario u
            JOIN u.unidadeLotacao unidade
            WHERE unidade.codigo = :codigoUnidade
            ORDER BY u.nome
            """)
    List<UsuarioConsultaLeitura> listarConsultasPorCodigoUnidadeLotacao(@Param("codigoUnidade") Long codigoUnidade);

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
                unidade.tituloTitular
            )
            FROM Usuario u
            JOIN u.unidadeLotacao unidade
            WHERE u.tituloEleitoral = :titulo
            """)
    Optional<UsuarioConsultaLeitura> buscarConsultaPorTitulo(@Param("titulo") String titulo);

    @Query("""
            SELECT DISTINCT u FROM Usuario u
            JOIN FETCH u.unidadeLotacao
            WHERE u.tituloEleitoral IN :titulos
            """)
    List<Usuario> listarPorTitulosComUnidadeLotacao(@Param("titulos") List<String> titulos);

    @Query("""
            SELECT new sgc.organizacao.dto.UsuarioPesquisaDto(
                u.tituloEleitoral,
                u.nome
            )
            FROM Usuario u
            WHERE LOWER(u.nome) LIKE LOWER(CONCAT(:termo, '%'))
               OR u.tituloEleitoral LIKE CONCAT(:termo, '%')
            ORDER BY u.nome
            """)
    List<UsuarioPesquisaDto> pesquisarPorNome(@Param("termo") String termo, Pageable pageable);
}
