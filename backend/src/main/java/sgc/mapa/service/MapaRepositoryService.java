package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MapaRepositoryService {
    private final MapaRepo mapaRepo;

    @Transactional(readOnly = true)
    public List<Mapa> findAll() {
        return mapaRepo.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Mapa> findById(Long id) {
        return mapaRepo.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Mapa> findMapaVigenteByUnidade(Long codigoUnidade) {
        return mapaRepo.findMapaVigenteByUnidade(codigoUnidade);
    }

    @Transactional(readOnly = true)
    public Optional<Mapa> findBySubprocessoCodigo(Long codSubprocesso) {
        return mapaRepo.findBySubprocessoCodigo(codSubprocesso);
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return mapaRepo.existsById(id);
    }

    @Transactional
    public Mapa salvar(Mapa mapa) {
        return mapaRepo.save(mapa);
    }

    @Transactional
    public void deleteById(Long id) {
        mapaRepo.deleteById(id);
    }
}
