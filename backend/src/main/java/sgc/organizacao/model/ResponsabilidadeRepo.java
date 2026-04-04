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

    List<Responsabilidade> findByUsuarioTitulo(String usuarioTitulo);
}
