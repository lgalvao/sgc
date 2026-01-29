package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;

import java.util.List;
import java.util.Optional;

/**
 * Serviço de acesso a dados para Mapas de Competências.
 */
@Service
@RequiredArgsConstructor
public class MapaRepositoryService {
    private final MapaRepo mapaRepo;

    @Transactional(readOnly = true)
    public List<Mapa> listarTodos() {
        return mapaRepo.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Mapa> buscarMapaVigentePorUnidade(Long unidadeCodigo) {
        return mapaRepo.findMapaVigenteByUnidade(unidadeCodigo);
    }

    @Transactional(readOnly = true)
    public Optional<Mapa> buscarPorSubprocessoCodigo(Long subprocessoCodigo) {
        return mapaRepo.findBySubprocessoCodigo(subprocessoCodigo);
    }

    @Transactional
    public Mapa salvar(Mapa mapa) {
        return mapaRepo.save(mapa);
    }

    @Transactional(readOnly = true)
    public boolean existe(Long codigo) {
        return mapaRepo.existsById(codigo);
    }

    @Transactional
    public void excluir(Long codigo) {
        mapaRepo.deleteById(codigo);
    }
}
