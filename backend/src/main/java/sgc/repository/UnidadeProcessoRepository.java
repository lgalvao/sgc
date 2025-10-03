package sgc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sgc.model.UnidadeProcesso;

import java.util.List;

@Repository
public interface UnidadeProcessoRepository extends JpaRepository<UnidadeProcesso, Long> {
    List<UnidadeProcesso> findByProcessoCodigo(Long processoCodigo);
    List<UnidadeProcesso> findBySigla(String sigla);
}