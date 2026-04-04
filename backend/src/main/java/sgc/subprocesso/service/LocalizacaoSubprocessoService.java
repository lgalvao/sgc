package sgc.subprocesso.service;

import lombok.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.erros.*;
import sgc.organizacao.model.*;
import sgc.subprocesso.model.*;

import static sgc.subprocesso.model.SituacaoSubprocesso.*;

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
                .orElseGet(() -> obterLocalizacaoSemMovimentacao(subprocesso, unidadeBase));
    }

    private Unidade obterLocalizacaoSemMovimentacao(Subprocesso subprocesso, Unidade unidadeBase) {
        if (subprocesso.getSituacao() == NAO_INICIADO) {
            return unidadeBase;
        }
        throw new ErroValidacao("Subprocesso persistido sem movimentação em situação inválida: %s".formatted(subprocesso.getSituacao()));
    }
}
