package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.comum.erros.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static sgc.processo.model.TipoProcesso.*;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoSituacaoService")
@SuppressWarnings("NullAway.Init")
class SubprocessoSituacaoServiceTest {

    @Mock
    private SubprocessoRepo subprocessoRepo;

    @InjectMocks
    private SubprocessoSituacaoService service;

    @Test
    @DisplayName("atualizarSituacaoPorMapa deve falhar quando nao existir subprocesso para o mapa")
    void atualizarSituacaoPorMapaDeveFalharQuandoNaoExistirSubprocesso() {
        when(subprocessoRepo.findByMapa_Codigo(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.atualizarSituacaoPorMapa(99L, true))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                .hasMessageContaining("Subprocesso")
                .hasMessageContaining("Mapa ID: 99");
    }

    @Test
    @DisplayName("atualizarSituacaoPorMapa deve iniciar revisao quando estiver nao iniciado")
    void atualizarSituacaoPorMapaDeveIniciarRevisaoQuandoEstiverNaoIniciado() {
        Subprocesso subprocesso = criarSubprocesso(REVISAO, NAO_INICIADO);
        when(subprocessoRepo.findByMapa_Codigo(10L)).thenReturn(Optional.of(subprocesso));

        service.atualizarSituacaoPorMapa(10L, false);

        assertThat(subprocesso.getSituacao()).isEqualTo(REVISAO_CADASTRO_EM_ANDAMENTO);
        verify(subprocessoRepo).findByMapa_Codigo(10L);
        verify(subprocessoRepo).save(subprocesso);
    }

    @Test
    @DisplayName("reconciliarSituacao deve retornar para nao iniciado quando mapeamento ficar vazio")
    void reconciliarSituacaoDeveRetornarParaNaoIniciadoQuandoMapeamentoFicarVazio() {
        Subprocesso subprocesso = criarSubprocesso(MAPEAMENTO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        service.reconciliarSituacao(subprocesso, false);

        assertThat(subprocesso.getSituacao()).isEqualTo(NAO_INICIADO);
        verify(subprocessoRepo).save(subprocesso);
    }

    @Test
    @DisplayName("reconciliarSituacao deve iniciar mapeamento quando houver atividades")
    void reconciliarSituacaoDeveIniciarMapeamentoQuandoHouverAtividades() {
        Subprocesso subprocesso = criarSubprocesso(MAPEAMENTO, NAO_INICIADO);

        service.reconciliarSituacao(subprocesso, true);

        assertThat(subprocesso.getSituacao()).isEqualTo(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        verify(subprocessoRepo).save(subprocesso);
    }

    @Test
    @DisplayName("reconciliarSituacao nao deve persistir quando situacao ja estiver coerente")
    void reconciliarSituacaoNaoDevePersistirQuandoSituacaoJaEstiverCoerente() {
        Subprocesso subprocesso = criarSubprocesso(REVISAO, REVISAO_CADASTRO_EM_ANDAMENTO);

        service.reconciliarSituacao(subprocesso, true);

        assertThat(subprocesso.getSituacao()).isEqualTo(REVISAO_CADASTRO_EM_ANDAMENTO);
        verify(subprocessoRepo, never()).save(any());
    }

    private Subprocesso criarSubprocesso(TipoProcesso tipoProcesso, SituacaoSubprocesso situacao) {
        Processo processo = new Processo();
        processo.setTipo(tipoProcesso);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(1L);
        subprocesso.setProcesso(processo);
        subprocesso.setSituacaoForcada(situacao);
        return subprocesso;
    }
}
