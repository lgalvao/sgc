package sgc.subprocesso.modelo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório JPA para a entidade Movimentacao.
 * Inclui query para recuperar movimentações de um subprocesso em ordem decrescente de data/hora.
 */
@Repository
public interface MovimentacaoRepo extends JpaRepository<Movimentacao, Long> {

    /**
     * Recupera movimentações vinculadas a um subprocesso, ordenadas por dataHora desc (mais recente primeiro).
     *
     * @param subprocessoCodigo id do subprocesso
     * @return lista de Movimentacao ordenada por dataHora desc
     */
    List<Movimentacao> findBySubprocessoCodigoOrderByDataHoraDesc(Long subprocessoCodigo);
}