package sgc.mapa.modelo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório para UNIDADE_MAPA permitindo gerenciar mapas vigentes das unidades.
 * Cada unidade pode ter apenas um mapa vigente por vez.
 */
@Repository
public interface UnidadeMapaRepo extends JpaRepository<UnidadeMapa, Long> {
    /**
     * Busca o mapa vigente de uma unidade específica.
     *
     * @param unidadeCodigo Código da unidade
     * @return Optional contendo o registro de mapa vigente se existir
     */
    Optional<UnidadeMapa> findByUnidadeCodigo(Long unidadeCodigo);

    /**
     * Busca os mapas vigentes de uma lista de unidades.
     *
     * @param unidadeCodigos Lista de códigos de unidade
     * @return Lista de registros UnidadeMapa para as unidades especificadas
     */
    List<UnidadeMapa> findByUnidadeCodigoIn(List<Long> unidadeCodigos);

    /**
     * Busca todas as unidades que utilizam um mapa específico como vigente.
     *
     * @param mapaVigenteCodigo Código do mapa
     * @return Lista de registros UnidadeMapa que apontam para este mapa
     */
    List<UnidadeMapa> findByMapaVigenteCodigo(Long mapaVigenteCodigo);

    /**
     * Busca o mapa vigente de uma unidade com data de vigência.
     * Retorna o mais recente caso haja múltiplos registros (não deveria ocorrer devido à constraint).
     *
     * @param unidadeCodigo Código da unidade
     * @return Optional contendo o mapa vigente mais recente
     */
    @Query("""
            SELECT um FROM UnidadeMapa um
            WHERE um.unidadeCodigo = :unidadeCodigo
            AND um.dataVigencia IS NOT NULL
            ORDER BY um.dataVigencia DESC
            """)
    Optional<UnidadeMapa> findMapaVigenteByUnidade(@Param("codUnidade") Long unidadeCodigo);

    @Query("SELECT um.unidadeCodigo FROM UnidadeMapa um WHERE um.unidadeCodigo IN :codigos AND um.mapaVigenteCodigo IS NOT NULL")
    List<Long> findCodigosUnidadesComMapaVigente(@Param("codigos") List<Long> codigosUnidades);
}