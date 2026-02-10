package sgc.comum.repo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;

/**
 * Componente centralizado para busca de entidades usando Hibernate/EntityManager.
 * Evita a poluição de orElseThrow(ErroEntidadeNaoEncontrada) em toda a aplicação.
 */
@Component
public class ComumRepo {

    @PersistenceContext
    private EntityManager em;

    /**
     * Busca uma entidade pelo seu ID.
     *
     * @param <T>    Tipo da entidade
     * @param classe Classe da entidade
     * @param id     Identificador único
     * @return A entidade encontrada
     * @throws ErroEntidadeNaoEncontrada se a entidade não existir
     */
    public <T> T buscar(Class<T> classe, Object id) {
        T entidade = em.find(classe, id);
        if (entidade == null) {
            throw new ErroEntidadeNaoEncontrada(classe.getSimpleName(), id);
        }
        return entidade;
    }

    /**
     * Busca uma única entidade por um campo específico.
     *
     * @param <T>    Tipo da entidade
     * @param classe Classe da entidade
     * @param campo  Nome do campo
     * @param valor  Valor do campo
     * @return A entidade encontrada
     * @throws ErroEntidadeNaoEncontrada se a entidade não existir
     */
    public <T> T buscar(Class<T> classe, String campo, Object valor) {
        try {
            return em.createQuery("SELECT e FROM %s e WHERE e.%s = :valor".formatted(classe.getSimpleName(), campo), classe)
                    .setParameter("valor", valor)
                    .getSingleResult();
        } catch (Exception e) {
            throw new ErroEntidadeNaoEncontrada(classe.getSimpleName(), valor);
        }
    }

    /**
     * Busca uma única entidade por múltiplos campos.
     *
     * @param <T>     Tipo da entidade
     * @param classe  Classe da entidade
     * @param filtros Mapa de campo -> valor
     * @return A entidade encontrada
     * @throws ErroEntidadeNaoEncontrada se a entidade não existir ou não atender aos critérios
     */
    public <T> T buscar(Class<T> classe, java.util.Map<String, Object> filtros) {
        StringBuilder jpql = new StringBuilder("SELECT e FROM " + classe.getSimpleName() + " e WHERE 1=1");
        filtros.keySet().forEach(campo -> jpql.append(" AND e.").append(campo).append(" = :").append(campo.replace(".", "_")));
        
        try {
            var query = em.createQuery(jpql.toString(), classe);
            filtros.forEach((campo, valor) -> query.setParameter(campo.replace(".", "_"), valor));
            return query.getSingleResult();
        } catch (Exception e) {
            throw new ErroEntidadeNaoEncontrada(classe.getSimpleName(), filtros.values().toString());
        }
    }

    /**
     * Busca uma entidade pela sua sigla.
     *
     * @param <T>    Tipo da entidade
     * @param classe Classe da entidade
     * @param sigla  Sigla da entidade
     * @return A entidade encontrada
     * @throws ErroEntidadeNaoEncontrada se a entidade não existir
     */
    public <T> T buscarPorSigla(Class<T> classe, String sigla) {
        return buscar(classe, "sigla", sigla);
    }
}
