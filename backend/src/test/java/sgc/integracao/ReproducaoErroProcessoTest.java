package sgc.integracao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sgc.processo.internal.model.Processo;
import sgc.processo.internal.model.SituacaoProcesso;
import sgc.processo.internal.model.TipoProcesso;
import java.time.LocalDateTime;

@DisplayName("Testes de Reprodução de Erros de Processo")
public class ReproducaoErroProcessoTest extends BaseIntegrationTest {

    @Test
    @DisplayName("Deve salvar processo para reprodução de erro")
    void deveSalvarProcessoParaReproducao() {
        Processo processo = new Processo();
        processo.setDescricao("Teste de Reprodução");
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.CRIADO);
        processo.setDataCriacao(LocalDateTime.now());
        processo.setDataLimite(LocalDateTime.now().plusDays(10));
        
        processoRepo.saveAndFlush(processo);
    }
}
