package sgc.analise;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.analise.model.Analise;
import sgc.analise.model.AnaliseRepo;

import java.util.List;

/**
 * Service responsável pelas operações de persistência de análises.
 *
 * <p>Este service encapsula o acesso ao {@link AnaliseRepo}, permitindo que
 * {@link AnaliseFacade} delegue operações de dados sem acessar repositórios diretamente.
 *
 * @see AnaliseFacade
 * @see AnaliseRepo
 */
@Service
@RequiredArgsConstructor
public class AnaliseService {
    private final AnaliseRepo analiseRepo;

    @Transactional(readOnly = true)
    public List<Analise> listarPorSubprocesso(Long codSubprocesso) {
        return analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(codSubprocesso);
    }

    @Transactional
    public Analise salvar(Analise analise) {
        return analiseRepo.save(analise);
    }

    @Transactional
    public void removerPorSubprocesso(Long codSubprocesso) {
        List<Analise> analises = analiseRepo.findBySubprocessoCodigo(codSubprocesso);
        if (!analises.isEmpty()) {
            analiseRepo.deleteAll(analises);
        }
    }
}
