package sgc.organizacao.model;

import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AtribuicaoTemporariaRepo extends JpaRepository<AtribuicaoTemporaria, Long> {
    @Query("""
            SELECT a FROM AtribuicaoTemporaria a
            JOIN FETCH a.unidade
            """)
    List<AtribuicaoTemporaria> listarTodasComUnidade();

    @Query("""
            SELECT a FROM AtribuicaoTemporaria a
            JOIN FETCH a.unidade
            WHERE a.unidade.codigo = :codUnidade
            ORDER BY a.dataInicio DESC, a.codigo DESC
            """)
    List<AtribuicaoTemporaria> listarPorUnidadeComUnidade(@Param("codUnidade") Long codUnidade);

    @Query("""
            SELECT COUNT(a) > 0 FROM AtribuicaoTemporaria a
            WHERE a.unidade.codigo = :codUnidade
            AND (:codigoIgnorado IS NULL OR a.codigo <> :codigoIgnorado)
            AND a.dataInicio <= :dataTermino
            AND a.dataTermino >= :dataInicio
            """)
    boolean existeSobreposicaoPeriodo(
            @Param("codUnidade") Long codUnidade,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataTermino") LocalDateTime dataTermino,
            @Param("codigoIgnorado") @Nullable Long codigoIgnorado
    );
}
