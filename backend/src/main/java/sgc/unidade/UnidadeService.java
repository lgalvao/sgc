package sgc.unidade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UnidadeService {

    private final UnidadeRepo unidadeRepo;

    public List<Unidade> listarTodas() {
        return unidadeRepo.findAll();
    }
}
