package sgc.processo.modelo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UnidadeProcessoRepo extends JpaRepository<UnidadeProcesso, Long> {
    List<UnidadeProcesso> findByProcessoCodigo(Long processoCodigo);
    List<UnidadeProcesso> findBySigla(String sigla);

    /**
     * Busca, dentre uma lista de códigos de unidade, quais já estão participando de algum processo ativo ('EM_ANDAMENTO').
     * @param codigosUnidades A lista de códigos de unidade a serem verificados.
     * @return Uma lista de códigos de unidade que já estão em um processo ativo.
     */
    @Query("SELECT up.unidadeCodigo FROM UnidadeProcesso up JOIN Processo p ON up.processoCodigo = p.codigo " +
           "WHERE p.situacao = 'EM_ANDAMENTO' " +
           "AND up.unidadeCodigo IN :codigosUnidades")
    List<Long> findUnidadesInProcessosAtivos(@Param("codigosUnidades") List<Long> codigosUnidades);

    List<UnidadeProcesso> findByUnidadeCodigoIn(List<Long> unidadeCodigos);
}