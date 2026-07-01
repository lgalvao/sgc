package sgc.processo.service;

import org.junit.jupiter.api.*;
import sgc.comum.*;
import sgc.comum.erros.*;
import sgc.processo.model.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ProcessoService - Exclusao")
class ProcessoServiceExclusaoTest extends ProcessoServiceTestBase {

    @Test
    @DisplayName("deve excluir o processo carregado quando estiver em criado")
    void deveExcluirOProcessoCarregadoQuandoEstiverEmCriado() {
        Processo processo = criarProcessoTeste(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.CRIADO);
        when(repo.buscar(Processo.class, 1L)).thenReturn(processo);

        processoService.apagar(1L);

        verify(processoRepo).delete(processo);
        verify(processoRepo, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("deve rejeitar exclusao quando processo nao estiver em criado")
    void deveRejeitarExclusaoQuandoProcessoNaoEstiverEmCriado() {
        Processo processo = criarProcessoTeste(TipoProcesso.MAPEAMENTO);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        when(repo.buscar(Processo.class, 1L)).thenReturn(processo);

        assertThatThrownBy(() -> processoService.apagar(1L))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining(Mensagens.PROCESSO_SO_REMOVIVEL_EM_CRIADO);

        verify(processoRepo, never()).delete(any());
        verify(processoRepo, never()).deleteById(anyLong());
    }
}
