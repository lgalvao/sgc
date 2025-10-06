package sgc.sgrh.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import sgc.sgrh.entity.VwUnidade;

import java.util.List;
import java.util.Optional;

/**
 * Repository READ-ONLY para view VW_UNIDADE do SGRH.
 * <p>
 * IMPORTANTE: Este repository acessa dados do Oracle SGRH.
 * Todas as operações são somente leitura (read-only).
 */
@Repository
public interface VwUnidadeRepository extends JpaRepository<VwUnidade, Long> {
    
    /**
     * Lista todas as unidades ativas.
     */
    List<VwUnidade> findByAtivaTrue();
    
    /**
     * Busca subunidades de uma unidade pai.
     */
    List<VwUnidade> findByCodigoPai(Long codigoPai);
    
    /**
     * Busca subunidades ativas de uma unidade pai.
     */
    List<VwUnidade> findByCodigoPaiAndAtivaTrue(Long codigoPai);
    
    /**
     * Busca unidade por código.
     */
    Optional<VwUnidade> findByCodigo(Long codigo);
    
    /**
     * Busca unidades por tipo.
     */
    List<VwUnidade> findByTipoAndAtivaTrue(String tipo);
    
    /**
     * Busca unidades raiz (sem pai).
     */
    @Query("SELECT u FROM VwUnidade u WHERE u.codigoPai IS NULL AND u.ativa = true")
    List<VwUnidade> findUnidadesRaiz();
}