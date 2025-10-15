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

    @Transactional(readOnly = true)
    public List<Mapa> listar() {
        return repositorioMapa.findAll();
    }

    @Transactional(readOnly = true)
    public Mapa obterPorId(Long id) {
        return repositorioMapa.findById(id)
            .orElseThrow(() -> new ErroDominioNaoEncontrado("Mapa", id));
    }

    public Mapa criar(Mapa mapa) {
        return repositorioMapa.save(mapa);
    }

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

    public void excluir(Long id) {
        if (!repositorioMapa.existsById(id)) {
            throw new ErroDominioNaoEncontrado("Mapa", id);
        }
        repositorioMapa.deleteById(id);
    }
}