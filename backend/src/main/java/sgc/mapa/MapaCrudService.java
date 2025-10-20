package sgc.mapa;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MapaCrudService {
    private final MapaRepo repositorioMapa;

    /**
     * Retorna uma lista com todos os mapas.
     *
     * @return Uma {@link List} de {@link Mapa}.
     */
    @Transactional(readOnly = true)
    public List<Mapa> listar() {
        return repositorioMapa.findAll();
    }

    /**
     * Busca um mapa pelo seu ID.
     *
     * @param id O ID do mapa.
     * @return A entidade {@link Mapa} correspondente.
     * @throws ErroDominioNaoEncontrado se o mapa não for encontrado.
     */
    @Transactional(readOnly = true)
    public Mapa obterPorId(Long id) {
        return repositorioMapa.findById(id)
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Mapa", id));
    }

    /**
     * Persiste um novo mapa no banco de dados.
     *
     * @param mapa A entidade {@link Mapa} a ser criada.
     * @return A entidade {@link Mapa} salva com o ID atribuído.
     */
    public Mapa criar(Mapa mapa) {
        return repositorioMapa.save(mapa);
    }

    /**
     * Atualiza um mapa existente no banco de dados.
     *
     * @param id   O ID do mapa a ser atualizado.
     * @param mapa A entidade {@link Mapa} com os novos dados.
     * @return A entidade {@link Mapa} atualizada.
     * @throws ErroDominioNaoEncontrado if the map is not found.
     */
    public Mapa atualizar(Long id, Mapa mapa) {
        return repositorioMapa.findById(id)
            .map(existente -> {
                existente.setDataHoraDisponibilizado(mapa.getDataHoraDisponibilizado());
                existente.setObservacoesDisponibilizacao(mapa.getObservacoesDisponibilizacao());
                existente.setSugestoesApresentadas(mapa.getSugestoesApresentadas());
                existente.setDataHoraHomologado(mapa.getDataHoraHomologado());
                return repositorioMapa.save(existente);
            })
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Mapa", id));
    }

    /**
     * Exclui um mapa do banco de dados.
     *
     * @param id O ID do mapa a ser excluído.
     * @throws ErroDominioNaoEncontrado se o mapa não for encontrado.
     */
    public void excluir(Long id) {
        if (!repositorioMapa.existsById(id)) {
            throw new ErroDominioNaoEncontrado("Mapa", id);
        }
        repositorioMapa.deleteById(id);
    }
}