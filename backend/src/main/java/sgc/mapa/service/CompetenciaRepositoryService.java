package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompetenciaRepositoryService {
    private final CompetenciaRepo competenciaRepo;

    @Transactional(readOnly = true)
    public List<Competencia> findByMapaCodigo(Long codMapa) {
        return competenciaRepo.findByMapaCodigo(codMapa);
    }
}
