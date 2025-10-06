package sgc.sgrh.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sgc.sgrh.entity.VwResponsabilidade;

import java.util.List;
import java.util.Optional;

/**
 * Repository READ-ONLY para view VW_RESPONSABILIDADE do SGRH.
 * <p>
 * IMPORTANTE: Este repository acessa dados do Oracle SGRH.
 * Todas as operações são somente leitura (read-only).
 */
@Repository
public interface VwResponsabilidadeRepository extends JpaRepository<VwResponsabilidade, Long> {
    
    /**
     * Busca responsabilidade ativa de uma unidade.
     */
    Optional<VwResponsabilidade> findByUnidadeCodigoAndAtivaTrue(Long unidadeCodigo);
    
    /**
     * Lista todas as responsabilidades ativas de uma unidade.
     */
    List<VwResponsabilidade> findByUnidadeCodigoAndAtivaTrueOrderByDataInicioDesc(Long unidadeCodigo);
    
    /**
     * Busca unidades onde o servidor é titular.
     */
    List<VwResponsabilidade> findByTitularTituloAndAtivaTrue(String titulo);
    
    /**
     * Busca unidades onde o servidor é substituto.
     */
    List<VwResponsabilidade> findBySubstitutoTituloAndAtivaTrue(String titulo);
    
    /**
     * Busca unidades onde o servidor é titular ou substituto.
     */
    @Query("SELECT r FROM VwResponsabilidade r WHERE r.ativa = true AND " +
           "(r.titularTitulo = :titulo OR r.substitutoTitulo = :titulo)")
    List<VwResponsabilidade> findByTitularOuSubstituto(@Param("titulo") String titulo);
    
    /**
     * Lista todas as responsabilidades ativas.
     */
    List<VwResponsabilidade> findByAtivaTrue();
}