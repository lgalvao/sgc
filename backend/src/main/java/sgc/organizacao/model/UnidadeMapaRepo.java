package sgc.organizacao.model;

import org.springframework.data.repository.query.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;
import sgc.mapa.model.*;

import java.util.*;

@Repository
public interface UnidadeMapaRepo extends JpaRepository<UnidadeMapa, Long> {
    @Query("""
            SELECT um.unidadeCodigo
            FROM UnidadeMapa um
            JOIN um.mapaVigente mapa
            JOIN mapa.subprocesso subprocesso
            """)
    List<Long> listarTodosCodigosUnidadeComMapaVigente();

    @Query("""
            SELECT um
            FROM UnidadeMapa um
            JOIN FETCH um.mapaVigente mapa
            JOIN FETCH mapa.subprocesso subprocesso
            JOIN FETCH subprocesso.processo processo
            WHERE um.unidadeCodigo = :codigoUnidade
            """)
    Optional<UnidadeMapa> buscarMapaVigenteComProcesso(@Param("codigoUnidade") Long codigoUnidade);

    @Query("""
            SELECT um
            FROM UnidadeMapa um
            JOIN FETCH um.mapaVigente mapa
            JOIN FETCH mapa.subprocesso subprocesso
            WHERE um.unidadeCodigo IN :codigosUnidades
            """)
    List<UnidadeMapa> listarMapasVigentesPorUnidades(@Param("codigosUnidades") Collection<Long> codigosUnidades);
}
