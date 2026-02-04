package sgc.subprocesso.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório JPA para a entidade Movimentacao. Inclui query para recuperar movimentações de um
 * subprocesso em ordem decrescente de data/hora.
 */
@Repository
public interface MovimentacaoRepo extends JpaRepository<Movimentacao, Long> {
    /**
     * Recupera movimentações vinculadas a um subprocesso, ordenadas por dataHora desc (mais recente
     * primeiro).
     *
     * @param subprocessoCodigo codigo do subprocesso
     * @return lista de Movimentacao ordenada por dataHora desc
     */
    @Query("""
            SELECT m FROM Movimentacao m
            LEFT JOIN FETCH m.unidadeOrigem
            LEFT JOIN FETCH m.unidadeDestino
            WHERE m.subprocesso.codigo = :subprocessoCodigo
            ORDER BY m.dataHora DESC
            """)
    List<Movimentacao> findBySubprocessoCodigoOrderByDataHoraDesc(@Param("subprocessoCodigo") Long subprocessoCodigo);

    List<Movimentacao> findBySubprocessoCodigo(Long subprocessoCodigo);
}
