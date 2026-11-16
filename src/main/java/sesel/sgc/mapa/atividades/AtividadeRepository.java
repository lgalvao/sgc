package sesel.sgc.mapa.atividades;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório para operações de banco de dados relacionadas a Atividades.
 */
@Repository
public interface AtividadeRepository extends JpaRepository<Atividade, UUID> {
    /**
     * Encontra todas as atividades de uma Processo de unidade.
     *
     * @param processoUnidadeId o ID da Processo de unidade
     * @return Lista de atividades da Processo de unidade
     */
    List<Atividade> findByProcessoUnidadeId(UUID processoUnidadeId);

    /**
     * Encontra todas as atividades de uma Processo de unidade com paginação.
     *
     * @param processoUnidadeId ID da Processo de unidade
     * @param pageable          Informações de paginação
     * @return Página de atividades da Processo de unidade
     */
    Page<Atividade> findByProcessoUnidadeId(UUID processoUnidadeId, Pageable pageable);

    /**
     * Busca uma atividade pelo ID e pelo ID da Processo de unidade.
     *
     * @param id                ID da atividade
     * @param processoUnidadeId ID da Processo de unidade
     * @return A atividade encontrada, se existir
     */
    @Query("""
            SELECT a FROM Atividade a
                    LEFT JOIN FETCH a.conhecimentos
            WHERE a.id = :id AND
                  a.processoUnidade.id = :processoUnidadeId
            """)
    Optional<Atividade> findByIdAndProcessoUnidadeId(
            @Param("id") UUID id,
            @Param("processoUnidadeId") UUID processoUnidadeId);

    /**
     * Verifica se existe uma atividade com o ID e ID da Processo de unidade fornecidos.
     *
     * @param id                ID da atividade
     * @param processoUnidadeId ID da Processo de unidade
     * @return true se existir, false caso contrário
     */
    @Query("""
            SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
            FROM Atividade a
            WHERE a.id = :id AND
                  a.processoUnidade.id = :processoUnidadeId
            """)
    boolean existsByIdAndProcessoUnidadeId(
            @Param("id") UUID id,
            @Param("processoUnidadeId") UUID processoUnidadeId);

    /**
     * Exclui uma atividade pelo ID e ID da Processo de unidade.
     *
     * @param id                ID da atividade
     * @param processoUnidadeId ID da Processo de unidade
     * @return Número de registros excluídos
     */
    @Modifying
    @Transactional
    @Query("""
            DELETE FROM Atividade a
            WHERE a.id = :id AND
                  a.processoUnidade.id = :processoUnidadeId
           """)
    int deleteByIdAndProcessoUnidadeId(
            @Param("id") UUID id,
            @Param("processoUnidadeId") UUID processoUnidadeId);

    /**
     * Conta quantas atividades existem para uma Processo de unidade.
     *
     * @param processoUnidadeId ID da Processo de unidade
     * @return Número de atividades da Processo de unidade
     */
    long countByProcessoUnidadeId(UUID processoUnidadeId);

    /**
     * Verifica se existem atividades para uma Processo de unidade.
     *
     * @param processoUnidadeId ID da Processo de unidade
     * @return true se existirem atividades, false caso contrário
     */
    boolean existsByProcessoUnidadeId(UUID processoUnidadeId);

    /**
     * Exclui todas as atividades de uma Processo de unidade.
     *
     * @param processoUnidadeId o ID da Processo de unidade.
     * @return o número de atividades excluídas.
     */
    @Modifying
    @Query("DELETE FROM Atividade a WHERE a.processoUnidade.id = :processoUnidadeId")
    int deleteByProcessoUnidadeId(@Param("processoUnidadeId") UUID processoUnidadeId);

    /**
     * Busca todas as atividades de um processoUnidade já trazendo os conhecimentos
     * (fetch join).
     */
    @Query("""
            SELECT a FROM Atividade a
               LEFT JOIN FETCH a.conhecimentos
            WHERE a.processoUnidade.id = :processoUnidadeId""")
    List<Atividade> findByProcessoUnidadeIdComConhecimentos(@Param("processoUnidadeId") UUID processoUnidadeId);
}
