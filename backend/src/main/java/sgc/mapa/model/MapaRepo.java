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
    @Query("SELECT u.mapaVigente FROM Unidade u WHERE u.codigo = :unidadeCodigo")
    Optional<Mapa> findMapaVigenteByUnidade(@Param("unidadeCodigo") Long unidadeCodigo);

    /**
     * Busca o mapa de um subprocesso.
     *
     * @param subprocessoCodigo Código do subprocesso
     * @return Optional contendo o mapa se existir
     */
    @Query("SELECT s.mapa FROM Subprocesso s WHERE s.codigo = :subprocessoCodigo")
    Optional<Mapa> findBySubprocessoCodigo(@Param("subprocessoCodigo") Long subprocessoCodigo);
}
