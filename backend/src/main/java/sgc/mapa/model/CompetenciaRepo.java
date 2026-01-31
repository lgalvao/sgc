package sgc.mapa.model;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório JPA para a entidade Competencia.
 * 
 * <h2>Padrões de Consulta</h2>
 * Este repositório oferece 3 métodos para buscar competências por mapa,
 * cada um otimizado para um caso de uso específico:
 * 
 * <ul>
 * <li>{@link #findByMapaCodigo(Long)} - Carrega competências com atividades (EntityGraph)</li>
 * <li>{@link #findCompetenciaAndAtividadeIdsByMapaCodigo(Long)} - Projeção SQL otimizada</li>
 * <li>{@link #findByMapaCodigoSemFetch(Long)} - Sem relacionamentos (mais leve)</li>
 * </ul>
 * 
 */
@Repository
public interface CompetenciaRepo extends JpaRepository<Competencia, Long> {
    /**
     * Busca todas as competências de um mapa, incluindo suas atividades associadas.
     *
     * <p><b>Quando usar:</b> Quando precisar das entidades Atividade completas para manipulação.
     *
     * <p><b>Performance:</b> EntityGraph evita N+1 queries mas pode duplicar dados em memória
     * se múltiplas competências compartilham atividades.
     *
     * @param mapaCodigo Código do mapa
     */
    @EntityGraph(attributePaths = {"atividades"})
    List<Competencia> findByMapaCodigo(@Param("mapaCodigo") Long mapaCodigo);

    /**
     * Busca dados projetados (id, descricao, id_atividade) das competências de um mapa.
     * 
     * <p><b>Quando usar:</b> Para visualização/montagem de DTOs onde apenas IDs e descrições são necessários.
     * Esta é a abordagem mais eficiente quando múltiplas competências compartilham atividades.
     * 
     * <p><b>Performance:</b> Minimiza transferência de dados e uso de memória ao retornar apenas
     * campos necessários. Ideal para {@link sgc.mapa.service.MapaVisualizacaoService}.
     * 
     * <p><b>Trade-off:</b> Requer parsing manual de Object[] no código cliente.
     *
     * @param mapaCodigo Código do mapa
     * @return Lista de arrays de objetos [Long competenciaId, String descricao, Long atividadeId]
     * @see sgc.mapa.service.MapaVisualizacaoService#obterMapaParaVisualizacao(sgc.subprocesso.model.Subprocesso)
     */
    @Query("""
            SELECT c.codigo, c.descricao, a.codigo
            FROM Competencia c
            LEFT JOIN c.atividades a
            WHERE c.mapa.codigo = :mapaCodigo
            """)
    List<Object[]> findCompetenciaAndAtividadeIdsByMapaCodigo(@Param("mapaCodigo") Long mapaCodigo);

    /**
     * Busca competências de um mapa sem carregar relacionamentos.
     * 
     * <p><b>Quando usar:</b> Para operações que precisam apenas de metadados das competências
     * (código, descrição) sem atividades associadas. Exemplo: ajuste interno de mapa.
     * 
     * <p><b>Performance:</b> Mais leve que EntityGraph, evita carregar dados desnecessários.
     * 
     * <p><b>Atenção:</b> Acessar {@code competencia.getAtividades()} causará lazy loading
     * (N+1 queries) se fora de transação.
     *
     * @param mapaCodigo Código do mapa
     * @return Lista de competências sem relacionamentos carregados
     * @see sgc.mapa.service.MapaManutencaoService#buscarCompetenciasPorCodMapaSemRelacionamentos(Long)
     */
    @Query("SELECT c FROM Competencia c WHERE c.mapa.codigo = :mapaCodigo")
    List<Competencia> findByMapaCodigoSemFetch(@Param("mapaCodigo") Long mapaCodigo);
}
