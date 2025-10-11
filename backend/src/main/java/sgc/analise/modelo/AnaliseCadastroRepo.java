package sgc.analise.modelo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório para registros de análise do cadastro (ANALISE_CADASTRO).
 * Fornece metodo para remover histórico relacionado a um subprocesso.
 */
@Repository
public interface AnaliseCadastroRepo extends JpaRepository<AnaliseCadastro, Long> {

    /**
     * Remove todas as análises relacionadas ao subprocesso informado.
     *
     * @param subprocessoCodigo id do subprocesso
     */
    void deleteBySubprocessoCodigo(Long subprocessoCodigo);

    /**
     * Recupera análises vinculadas a um subprocesso, ordenadas da mais recente para a mais antiga.
     *
     * @param subprocessoCodigo id do subprocesso
     * @return lista de AnaliseCadastro
     */
    List<AnaliseCadastro> findBySubprocessoCodigoOrderByDataHoraDesc(Long subprocessoCodigo);
}