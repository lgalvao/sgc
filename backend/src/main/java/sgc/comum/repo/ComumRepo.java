package sgc.comum.repo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;

import java.util.Map;

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
     */
    public <T> T buscar(Class<T> classe, Map<String, Object> filtros) {
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
     */
    public <T> T buscarPorSigla(Class<T> classe, String sigla) {
        return buscar(classe, "sigla", sigla);
    }
}
