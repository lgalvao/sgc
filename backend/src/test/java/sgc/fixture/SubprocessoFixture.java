package sgc.fixture;

import sgc.processo.model.Processo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.unidade.model.Unidade;

import java.time.LocalDateTime;

public class SubprocessoFixture {

    public static Subprocesso subprocessoPadrao(Processo processo, Unidade unidade) {
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(1L);
        subprocesso.setProcesso(processo);
        subprocesso.setUnidade(unidade);
        subprocesso.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
        subprocesso.setDataLimiteEtapa1(LocalDateTime.now().plusWeeks(2));
        return subprocesso;
    }

    public static Subprocesso novoSubprocesso(Processo processo, Unidade unidade) {
        // Without ID, for saving
        Subprocesso subprocesso = subprocessoPadrao(processo, unidade);
        subprocesso.setCodigo(null);
        return subprocesso;
    }
}
