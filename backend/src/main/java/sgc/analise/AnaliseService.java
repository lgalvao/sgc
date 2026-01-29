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

    /**
     * Lista análises de um subprocesso ordenadas por data/hora descendente.
     *
     * @param codSubprocesso código do subprocesso
     * @return lista de análises ordenadas
     */
    public List<Analise> listarPorSubprocesso(Long codSubprocesso) {
        return analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(codSubprocesso);
    }

    /**
     * Salva uma análise.
     *
     * @param analise análise a salvar
     * @return análise salva
     */
    @Transactional
    public Analise salvar(Analise analise) {
        return analiseRepo.save(analise);
    }

    /**
     * Remove todas as análises de um subprocesso.
     *
     * @param codSubprocesso código do subprocesso
     */
    @Transactional
    public void removerPorSubprocesso(Long codSubprocesso) {
        List<Analise> analises = analiseRepo.findBySubprocessoCodigo(codSubprocesso);
        if (!analises.isEmpty()) {
            analiseRepo.deleteAll(analises);
        }
    }
}
