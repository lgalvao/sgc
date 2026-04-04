package sgc.organizacao.model;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.*;
import org.springframework.stereotype.*;

import java.util.*;

@Repository
public interface ResponsabilidadeRepo extends JpaRepository<Responsabilidade, Long> {
    @Query("""
            SELECT r FROM Responsabilidade r
            JOIN FETCH r.unidade u
            WHERE r.unidadeCodigo IN :unidadeCodigos
            """)
    List<Responsabilidade> listarPorCodigosUnidade(@Param("unidadeCodigos") List<Long> unidadeCodigos);

    @Query("""
            SELECT new sgc.organizacao.model.ResponsabilidadeLeitura(
                r.unidadeCodigo,
                r.usuarioTitulo
            )
            FROM Responsabilidade r
            WHERE r.unidadeCodigo IN :unidadeCodigos
            """)
    List<ResponsabilidadeLeitura> listarLeiturasPorCodigosUnidade(@Param("unidadeCodigos") List<Long> unidadeCodigos);

    @Query("""
            SELECT new sgc.organizacao.model.ResponsabilidadeUnidadeLeitura(
                r.unidadeCodigo,
                r.usuarioTitulo,
                u.tituloTitular,
                r.tipo,
                r.dataInicio,
                r.dataFim
            )
            FROM Responsabilidade r
            JOIN Unidade u ON u.codigo = r.unidadeCodigo
            WHERE r.unidadeCodigo = :unidadeCodigo
            """)
    Optional<ResponsabilidadeUnidadeLeitura> buscarLeituraDetalhadaPorCodigoUnidade(@Param("unidadeCodigo") Long unidadeCodigo);

    @Query("""
            SELECT new sgc.organizacao.model.ResponsabilidadeUnidadeLeitura(
                r.unidadeCodigo,
                r.usuarioTitulo,
                u.tituloTitular,
                r.tipo,
                r.dataInicio,
                r.dataFim
            )
            FROM Responsabilidade r
            JOIN Unidade u ON u.codigo = r.unidadeCodigo
            WHERE r.unidadeCodigo IN :unidadeCodigos
            """)
    List<ResponsabilidadeUnidadeLeitura> listarLeiturasDetalhadasPorCodigosUnidade(@Param("unidadeCodigos") List<Long> unidadeCodigos);

    @Query("""
            SELECT new sgc.organizacao.model.ResponsabilidadeUnidadeResumoLeitura(
                r.unidadeCodigo,
                r.usuarioTitulo,
                responsavel.nome,
                u.tituloTitular,
                titular.nome
            )
            FROM Responsabilidade r
            JOIN Unidade u ON u.codigo = r.unidadeCodigo
            LEFT JOIN Usuario responsavel ON responsavel.tituloEleitoral = r.usuarioTitulo
            LEFT JOIN Usuario titular ON titular.tituloEleitoral = u.tituloTitular
            WHERE r.unidadeCodigo IN :unidadeCodigos
            """)
    List<ResponsabilidadeUnidadeResumoLeitura> listarResumosPorCodigosUnidade(@Param("unidadeCodigos") List<Long> unidadeCodigos);

    List<Responsabilidade> findByUsuarioTitulo(String usuarioTitulo);
}
