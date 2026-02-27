package sgc.comum.model;

import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.*;
import sgc.comum.erros.*;

import java.util.*;

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
        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(classe);
        var root = cq.from(classe);
        cq.where(cb.equal(root.get(campo), valor));

        try {
            return em.createQuery(cq).getSingleResult();
        } catch (NoResultException e) {
            throw new ErroEntidadeNaoEncontrada(classe.getSimpleName(), valor);
        }
    }

    /**
     * Busca uma única entidade por múltiplos campos.
     */
    public <T> T buscar(Class<T> classe, Map<String, Object> filtros) {
        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(classe);
        var root = cq.from(classe);

        var predicates = filtros.entrySet().stream()
                .map(f -> cb.equal(root.get(f.getKey()), f.getValue()))
                .toArray(Predicate[]::new);

        cq.where(predicates);

        try {
            return em.createQuery(cq).getSingleResult();
        } catch (NoResultException e) {
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
