package sgc.diagnostico.model;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.*;
import org.springframework.stereotype.*;

import java.util.*;

@Repository
public interface AvaliacaoServidorRepo extends JpaRepository<AvaliacaoServidor, Integer> {

    @Query("""
        SELECT a FROM AvaliacaoServidor a 
        JOIN FETCH a.servidor 
        JOIN FETCH a.competencia 
        WHERE a.diagnostico.codigo = :diagnosticoCodigo
    """)
    List<AvaliacaoServidor> listarPorDiagnostico(@Param("diagnosticoCodigo") long diagnosticoCodigo);

    @Query("""
        SELECT a FROM AvaliacaoServidor a 
        JOIN FETCH a.competencia 
        WHERE a.diagnostico.codigo = :diagnosticoCodigo 
        AND a.servidor.tituloEleitoral = :servidorTitulo
    """)
    List<AvaliacaoServidor> buscarAvaliacoesDoServidor(@Param("diagnosticoCodigo") long diagnosticoCodigo,
                                                        @Param("servidorTitulo") String servidorTitulo);
}
