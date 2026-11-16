package sesel.sgc.atribuicao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AtribuicaoRepository extends JpaRepository<Atribuicao, UUID> {
    List<Atribuicao> findByUnidadeId(UUID unidadeId);

    List<Atribuicao> findByServidorId(UUID servidorId);
}