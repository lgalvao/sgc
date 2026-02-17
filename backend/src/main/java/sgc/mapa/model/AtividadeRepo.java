package sgc.mapa.model;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AtividadeRepo extends JpaRepository<Atividade, Long> {
    /**
     * Busca todas as atividades com o mapa carregado.
     * 
     * <p><b>Quando usar:</b> Para listagens gerais onde o contexto do mapa é necessário.
     * 
     * <p><b>Performance:</b> EntityGraph evita lazy loading de mapa (N+1).
     */
    @EntityGraph(attributePaths = {"mapa"})
    @Override
    List<Atividade> findAll();

    /**
     * Busca atividades por código de mapa com competências carregadas.
     * 
     * <p><b>Quando usar:</b> Para operações que precisam saber quais competências
     * estão associadas a cada atividade.
     * 
     * <p><b>Performance:</b> Carrega relacionamento ManyToMany competências de forma eficiente.
     */
    @EntityGraph(attributePaths = {"competencias"})
    List<Atividade> findByMapa_Codigo(@Param("mapaCodigo") Long mapaCodigo);

    /**
     * Busca atividades por código de mapa sem carregar relacionamentos.
     * 
     * <p><b>Quando usar:</b> Para operações que precisam apenas de metadados das atividades
     * (código, descrição) sem competências ou conhecimentos associados.
     * 
     * <p><b>Performance:</b> Mais leve, ideal para ajustes internos de mapa.
     * 
     * <p><b>Atenção:</b> Acessar relacionamentos causará lazy loading se fora de transação.
     * 
     * @see sgc.mapa.service.MapaManutencaoService#buscarAtividadesPorMapaCodigoSemRelacionamentos(Long)
     */
    @Query("SELECT a FROM Atividade a WHERE a.mapa.codigo = :mapaCodigo")
    List<Atividade> findByMapaCodigoSemFetch(@Param("mapaCodigo") Long mapaCodigo);

    /**
     * Busca atividades por código de mapa com conhecimentos carregados.
     * 
     * <p><b>Quando usar:</b> Para visualização de mapa completo onde conhecimentos
     * de cada atividade são necessários.
     * 
     * <p><b>Performance:</b> Carrega relacionamento OneToMany conhecimentos de forma eficiente.
     * 
     * @see sgc.mapa.service.MapaVisualizacaoService#obterMapaParaVisualizacao(sgc.subprocesso.model.Subprocesso)
     */
    @EntityGraph(attributePaths = {"conhecimentos"})
    List<Atividade> findWithConhecimentosByMapa_Codigo(@Param("mapaCodigo") Long mapaCodigo);

    @Query("""
            SELECT a FROM Atividade a
            JOIN Subprocesso s ON a.mapa.codigo = s.mapa.codigo
            WHERE s.codigo = :subprocessoCodigo
            """)
    List<Atividade> findBySubprocessoCodigo(@Param("subprocessoCodigo") Long subprocessoCodigo);

    @Query("""
            SELECT DISTINCT a FROM Atividade a
            JOIN FETCH a.competencias
            WHERE :competencia MEMBER OF a.competencias
            """)
    List<Atividade> listarPorCompetencia(@Param("competencia") Competencia competencia);
}
