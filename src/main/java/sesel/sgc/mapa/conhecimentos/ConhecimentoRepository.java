package sesel.sgc.mapa.conhecimentos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório para operações de banco de dados relacionadas a Conhecimento.
 */
@Repository
public interface ConhecimentoRepository extends JpaRepository<Conhecimento, UUID> {
    /**
     * Busca um conhecimento pelo ID e pelo ID da atividade.
     *
     * @param conhecimentoId o ID do conhecimento.
     * @param atividadeId o ID da atividade.
     * @return um Optional contendo o conhecimento, se encontrado.
     */
    @Query("SELECT c FROM Conhecimento c WHERE c.id = :conhecimentoId AND c.atividade.id = :atividadeId")
    Optional<Conhecimento> findByIdAndAtividadeId(
            @Param("conhecimentoId") UUID conhecimentoId,
            @Param("atividadeId") UUID atividadeId
    );
    
    /**
     * Busca todos os conhecimentos de uma atividade.
     *
     * @param atividadeId o ID da atividade.
     * @return uma lista de conhecimentos da atividade.
     */
    List<Conhecimento> findByAtividadeId(UUID atividadeId);
    
    /**
     * Verifica se existe um conhecimento com o ID especificado na atividade.
     *
     * @param conhecimentoId o ID do conhecimento.
     * @param atividadeId o ID da atividade.
     * @return true se existir, false caso contrário.
     */
    boolean existsByIdAndAtividadeId(UUID conhecimentoId, UUID atividadeId);
    
    /**
     * Exclui todos os conhecimentos de uma atividade.
     *
     * @param atividadeId o ID da atividade.
     * @return o número de conhecimentos excluídos.
     */
    @Modifying
    @Query("DELETE FROM Conhecimento c WHERE c.atividade.id = :atividadeId")
    int deleteByAtividadeId(@Param("atividadeId") UUID atividadeId);
}
