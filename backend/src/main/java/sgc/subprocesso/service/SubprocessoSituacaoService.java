package sgc.subprocesso.service;

import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.erros.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;

import static sgc.subprocesso.model.SituacaoSubprocesso.*;

/**
 * Serviço especializado na transição de situações de Subprocesso.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SubprocessoSituacaoService {
    private final SubprocessoRepo subprocessoRepo;

    @Transactional
    public void atualizarSituacaoPorMapa(Long mapaCodigo, boolean temAtividades) {
        Subprocesso subprocesso = subprocessoRepo.findByMapa_Codigo(mapaCodigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso", "Mapa ID: " + mapaCodigo));

        reconciliarSituacao(subprocesso, temAtividades);
    }

    @Transactional
    public void reconciliarSituacao(Subprocesso subprocesso, boolean temAtividades) {
        SituacaoSubprocesso situacaoAtual = subprocesso.getSituacao();
        TipoProcesso tipoProcesso = subprocesso.getProcesso().getTipo();

        if (tipoProcesso == TipoProcesso.REVISAO) {
            if (situacaoAtual == NAO_INICIADO) {
                subprocesso.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
                subprocessoRepo.save(subprocesso);
                log.info("Subprocesso {} (REVISAO) movido para em andamento", subprocesso.getCodigo());
            }
            return;
        }

        if (!temAtividades && situacaoAtual == MAPEAMENTO_CADASTRO_EM_ANDAMENTO) {
            subprocesso.setSituacaoForcada(NAO_INICIADO);
            subprocessoRepo.save(subprocesso);
            log.info("Subprocesso {} (MAPEAMENTO) retornou para não iniciado (vazio)", subprocesso.getCodigo());
            return;
        }

        if (temAtividades && situacaoAtual == NAO_INICIADO) {
            subprocesso.setSituacao(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            subprocessoRepo.save(subprocesso);
            log.info("Subprocesso {} (MAPEAMENTO) movido para em andamento", subprocesso.getCodigo());
        }
    }
}
