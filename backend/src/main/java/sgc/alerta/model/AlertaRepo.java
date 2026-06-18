package sgc.alerta.model;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.*;
import org.springframework.stereotype.*;

import java.util.*;

/**
 * Repositório para a entidade {@link Alerta}.
 */
@Repository
public interface AlertaRepo extends JpaRepository<Alerta, Long> {
    List<Alerta> findByProcessoCodigo(Long codProcesso);

    /**
     * Busca alertas destinados exclusivamente ao usuário logado (pessoais).
     */
    @Query("""
            SELECT DISTINCT a FROM Alerta a
            LEFT JOIN FETCH a.processo
            LEFT JOIN FETCH a.unidadeOrigem
            LEFT JOIN FETCH a.unidadeDestino
            WHERE a.usuarioDestinoTitulo = :usuarioTitulo
            ORDER BY a.dataHora DESC
            """)
    List<Alerta> buscarAlertasExclusivosDoUsuario(@Param("usuarioTitulo") String usuarioTitulo);

    /**
     * Busca alertas visíveis para perfis de gestão:
     * pessoais do usuário logado e coletivos da unidade ativa.
     */
    @Query("""
            SELECT DISTINCT a FROM Alerta a
            LEFT JOIN FETCH a.processo
            LEFT JOIN FETCH a.unidadeOrigem
            LEFT JOIN FETCH a.unidadeDestino
            WHERE a.usuarioDestinoTitulo = :usuarioTitulo
               OR (a.unidadeDestino.codigo = :codUnidade AND a.usuarioDestinoTitulo IS NULL)
            ORDER BY a.dataHora DESC
            """)
    List<Alerta> buscarAlertasDaGestao(
            @Param("codUnidade") Long codUnidade,
            @Param("usuarioTitulo") String usuarioTitulo);

    /**
     * Versão paginada para perfis de gestão.
     */
    @Query(value = """
            SELECT DISTINCT a FROM Alerta a
            LEFT JOIN FETCH a.processo
            LEFT JOIN FETCH a.unidadeOrigem
            LEFT JOIN FETCH a.unidadeDestino
            WHERE a.usuarioDestinoTitulo = :usuarioTitulo
               OR (a.unidadeDestino.codigo = :codUnidade AND a.usuarioDestinoTitulo IS NULL)
            ORDER BY a.dataHora DESC
            """,
            countQuery = """
                    SELECT COUNT(a) FROM Alerta a
                    WHERE a.usuarioDestinoTitulo = :usuarioTitulo
                       OR (a.unidadeDestino.codigo = :codUnidade AND a.usuarioDestinoTitulo IS NULL)
                    """)
    Page<Alerta> buscarAlertasDaGestao(
            @Param("codUnidade") Long codUnidade,
            @Param("usuarioTitulo") String usuarioTitulo,
            Pageable pageable);

    /**
     * Versão paginada para o perfil SERVIDOR (Apenas exclusivos/pessoais).
     */
    @Query(value = """
            SELECT DISTINCT a FROM Alerta a
            LEFT JOIN FETCH a.processo
            LEFT JOIN FETCH a.unidadeOrigem
            LEFT JOIN FETCH a.unidadeDestino
            WHERE a.usuarioDestinoTitulo = :usuarioTitulo
            ORDER BY a.dataHora DESC
            """,
            countQuery = """
                    SELECT COUNT(a) FROM Alerta a
                    WHERE a.usuarioDestinoTitulo = :usuarioTitulo
                    """)
    Page<Alerta> buscarAlertasExclusivosDoUsuario(
            @Param("usuarioTitulo") String usuarioTitulo,
            Pageable pageable);
}
