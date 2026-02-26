package sgc.mapa.model;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.*;
import org.springframework.stereotype.*;

import java.util.*;

@Repository
public interface ConhecimentoRepo extends JpaRepository<Conhecimento, Long> {
    List<Conhecimento> findByAtividade_Codigo(Long atividadeCodigo);

    @Query("""
            SELECT c FROM Conhecimento c
            JOIN c.atividade a
            WHERE a.mapa.codigo = :codMapa
            """)
    List<Conhecimento> findByMapaCodigo(@Param("codMapa") Long codMapa);
}
