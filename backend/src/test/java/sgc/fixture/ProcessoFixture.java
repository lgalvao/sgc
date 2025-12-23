package sgc.fixture;

import sgc.processo.internal.model.Processo;
import sgc.processo.internal.model.SituacaoProcesso;
import sgc.processo.internal.model.TipoProcesso;
import sgc.unidade.internal.model.Unidade;

import java.time.LocalDateTime;

public class ProcessoFixture {

    public static Processo processoPadrao() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setDescricao("Processo de Mapeamento 2024");
        processo.setSituacao(SituacaoProcesso.CRIADO);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setDataCriacao(LocalDateTime.now());
        processo.setDataLimite(LocalDateTime.now().plusMonths(1));
        return processo;
    }

    public static Processo processoEmAndamento() {
        Processo processo = processoPadrao();
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        return processo;
    }

    public static Processo processoComUnidade(Unidade unidade) {
        Processo processo = processoPadrao();
        processo.getParticipantes().add(unidade);
        return processo;
    }
}
