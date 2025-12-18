package sgc.integracao;

import org.junit.jupiter.api.Test;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import java.time.LocalDateTime;

public class ReproducaoErroProcessoTest extends BaseIntegrationTest {

    @Test
    void tentarSalvarProcesso() {
        Processo processo = new Processo();
        processo.setDescricao("Teste de Reprodução");
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.CRIADO);
        processo.setDataCriacao(LocalDateTime.now());
        processo.setDataLimite(LocalDateTime.now().plusDays(10));
        
        processoRepo.saveAndFlush(processo);
    }
}
