package sgc.e2e.data;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class E2eDataService {

    private final UnidadeRepo unidadeRepo;
    private final MapaRepo mapaRepo;

    @Transactional
    public void criarMapaVigenteParaUnidade(Long unidadeId) {
        Unidade unidade = unidadeRepo.findById(unidadeId)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Unidade", unidadeId));

        // Se a unidade já tiver um mapa, não faz nada.
        if (unidade.getMapaVigente() != null) {
            return;
        }

        Mapa novoMapa = new Mapa();
        mapaRepo.save(novoMapa);

        unidade.setMapaVigente(novoMapa);
        unidade.setDataVigenciaMapaAtual(LocalDateTime.now());
        unidadeRepo.save(unidade);
    }
}
