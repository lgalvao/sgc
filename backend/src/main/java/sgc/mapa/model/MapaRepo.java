package sgc.mapa.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositório JPA para a entidade Mapa.
 */
@Repository
public interface MapaRepo extends JpaRepository<Mapa, Long> {
    /**
     * Busca o mapa vigente de uma unidade.
     *
     * @param unidadeCodigo Código da unidade
     * @return Optional contendo o mapa vigente se existir
     */
    @Query("""
            SELECT um.mapaVigente FROM UnidadeMapa um WHERE um.unidadeCodigo = :unidadeCodigo
            """)
    Optional<Mapa> findMapaVigenteByUnidade(@Param("unidadeCodigo") Long unidadeCodigo);

    /**
     * Busca o mapa de um subprocesso.
     *
     * @param subprocessoCodigo Código do subprocesso
     * @return Optional contendo o mapa se existir
     */
    @Query("""
            SELECT m FROM Mapa m WHERE m.subprocesso.codigo = :subprocessoCodigo
            """)
    Optional<Mapa> findBySubprocessoCodigo(@Param("subprocessoCodigo") Long subprocessoCodigo);
}
