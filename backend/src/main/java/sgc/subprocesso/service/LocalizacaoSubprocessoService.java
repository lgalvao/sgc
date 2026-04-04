package sgc.subprocesso.service;

import lombok.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.organizacao.model.*;
import sgc.subprocesso.model.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocalizacaoSubprocessoService {

    private final MovimentacaoRepo movimentacaoRepo;

    public Unidade obterLocalizacaoAtual(Subprocesso subprocesso) {
        Unidade unidadeBase = subprocesso.getUnidade();
        if (subprocesso.getCodigo() == null) {
            return unidadeBase;
        }

        return movimentacaoRepo.buscarUltimaPorSubprocesso(subprocesso.getCodigo())
                .map(Movimentacao::getUnidadeDestino)
                .orElse(unidadeBase);
    }
}
