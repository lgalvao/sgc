package sgc.subprocesso.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static sgc.processo.model.TipoProcesso.MAPEAMENTO;
import static sgc.processo.model.TipoProcesso.REVISAO;
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
