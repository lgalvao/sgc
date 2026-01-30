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
}
