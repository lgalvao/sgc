package sgc.fixture;

import sgc.organizacao.model.*;
import sgc.processo.model.*;

import java.time.*;
import java.util.*;

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
        processo.adicionarParticipantes(Set.of(unidade));
        return processo;
    }

    public static Processo novoProcesso() {
        Processo processo = processoPadrao();
        processo.setCodigo(null);
        return processo;
    }

    /**
     * Cria um snapshot de UnidadeProcesso a partir de uma Unidade para uso em testes.
     * Este método permite criar snapshots sem ter um Processo persistido.
     */
    public static UnidadeProcesso criarSnapshotParaTeste(Processo processo, Unidade unidade) {
        return UnidadeProcesso.criarSnapshot(processo, unidade);
    }
}
