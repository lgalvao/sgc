package sgc.processo.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repositório para operações com snapshots de unidades em processos.
 */
public interface UnidadeProcessoRepo extends JpaRepository<UnidadeProcesso, UnidadeProcessoId> {
    
    /**
     * Busca todos os snapshots de unidades de um processo específico.
     */
    List<UnidadeProcesso> findByProcessoCodigo(Long processoCodigo);
    
    /**
     * Busca os códigos de unidades participantes de um processo.
     */
    @Query("SELECT up.id.unidadeCodigo FROM UnidadeProcesso up WHERE up.id.processoCodigo = :processoCodigo")
    List<Long> findUnidadeCodigosByProcessoCodigo(@Param("processoCodigo") Long processoCodigo);
    
    /**
     * Verifica se uma unidade participa de um processo específico.
     */
    boolean existsByIdProcessoCodigoAndIdUnidadeCodigo(Long processoCodigo, Long unidadeCodigo);
    
    /**
     * Remove todos os snapshots de um processo.
     */
    @Modifying
    @Query("DELETE FROM UnidadeProcesso up WHERE up.id.processoCodigo = :processoCodigo")
    void deleteByProcessoCodigo(@Param("processoCodigo") Long processoCodigo);
}
