package sgc.subprocesso.model;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.*;
import org.springframework.stereotype.*;
import sgc.organizacao.model.*;

import java.util.*;

/**
 * Repositório JPA para a entidade Movimentacao. Inclui query para recuperar movimentações de um
 * subprocesso em ordem decrescente de data/hora.
 */
@Repository
public interface MovimentacaoRepo extends JpaRepository<Movimentacao, Long> {
    @Query("""
            SELECT m FROM Movimentacao m
            LEFT JOIN FETCH m.unidadeOrigem
            LEFT JOIN FETCH m.unidadeDestino
            LEFT JOIN FETCH m.usuario
            WHERE m.subprocesso.codigo = :subprocessoCodigo
            ORDER BY m.dataHora DESC, m.codigo DESC
            """)
    List<Movimentacao> listarPorSubprocessoOrdenadasPorDataHoraDesc(@Param("subprocessoCodigo") Long subprocessoCodigo);

    @Query("""
            SELECT m FROM Movimentacao m
            LEFT JOIN FETCH m.unidadeOrigem
            LEFT JOIN FETCH m.unidadeDestino
            LEFT JOIN FETCH m.usuario
            WHERE m.subprocesso.codigo = :subprocessoCodigo
            ORDER BY m.dataHora DESC, m.codigo DESC
            LIMIT 1
            """)
    Optional<Movimentacao> buscarUltimaPorSubprocesso(@Param("subprocessoCodigo") Long subprocessoCodigo);

    @Query("""
            SELECT m.unidadeDestino FROM Movimentacao m
            WHERE m.subprocesso.codigo = :subprocessoCodigo
            ORDER BY m.dataHora DESC, m.codigo DESC
            """)
    List<Unidade> listarUltimasUnidadesDestinoPorSubprocesso(
            @Param("subprocessoCodigo") Long subprocessoCodigo,
            Pageable pageable
    );

    @Query("""
            SELECT m FROM Movimentacao m
            LEFT JOIN FETCH m.subprocesso
            LEFT JOIN FETCH m.unidadeOrigem
            LEFT JOIN FETCH m.unidadeDestino
            LEFT JOIN FETCH m.usuario
            WHERE m.subprocesso.codigo IN :subprocessoCodigos
              AND NOT EXISTS (
                  SELECT 1 FROM Movimentacao maisRecente
                  WHERE maisRecente.subprocesso.codigo = m.subprocesso.codigo
                    AND (
                        maisRecente.dataHora > m.dataHora
                        OR (maisRecente.dataHora = m.dataHora AND maisRecente.codigo > m.codigo)
                    )
              )
            """)
    List<Movimentacao> listarUltimasPorSubprocessos(@Param("subprocessoCodigos") List<Long> subprocessoCodigos);

    List<Movimentacao> findBySubprocessoCodigo(Long subprocessoCodigo);
}
