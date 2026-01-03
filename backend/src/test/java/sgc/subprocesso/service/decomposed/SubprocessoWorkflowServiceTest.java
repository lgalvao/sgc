package sgc.subprocesso.service.decomposed;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.alerta.AlertaService;
import sgc.comum.erros.ErroValidacao;
import sgc.organizacao.UnidadeService;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para SubprocessoWorkflowService")
class SubprocessoWorkflowServiceTest {

    @Mock
    private SubprocessoRepo repositorioSubprocesso;
    @Mock
    private SubprocessoCrudService crudService;
    @Mock
    private AlertaService alertaService;
    @Mock
    private UnidadeService unidadeService;
    @Mock
    private MovimentacaoRepo repositorioMovimentacao;

    @InjectMocks
    private SubprocessoWorkflowService service;

    @Test
    @DisplayName("Deve atualizar situação para EM ANDAMENTO (Mapeamento)")
    void deveAtualizarParaEmAndamentoMapeamento() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        sp.setProcesso(p);

        when(repositorioSubprocesso.findByMapaCodigo(100L)).thenReturn(Optional.of(sp));

        service.atualizarSituacaoParaEmAndamento(100L);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        verify(repositorioSubprocesso).save(sp);
    }

    @Test
    @DisplayName("Deve atualizar situação para EM ANDAMENTO (Revisão)")
    void deveAtualizarParaEmAndamentoRevisao() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
        Processo p = new Processo();
        p.setTipo(TipoProcesso.REVISAO);
        sp.setProcesso(p);

        when(repositorioSubprocesso.findByMapaCodigo(100L)).thenReturn(Optional.of(sp));

        service.atualizarSituacaoParaEmAndamento(100L);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
        verify(repositorioSubprocesso).save(sp);
    }

    @Test
    @DisplayName("alterarDataLimite: deve capturar exceção do AlertaService e não falhar")
    void alterarDataLimite_DeveCapturarExceptionAlerta() {
        Long codSubprocesso = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        sp.setUnidade(new Unidade());

        when(crudService.buscarSubprocesso(codSubprocesso)).thenReturn(sp);
        doThrow(new RuntimeException("Erro envio alerta")).when(alertaService).criarAlertaAlteracaoDataLimite(any(), any(), any(), anyInt());

        assertThatCode(() -> service.alterarDataLimite(codSubprocesso, LocalDate.now()))
                .doesNotThrowAnyException();

        verify(repositorioSubprocesso).save(sp);
    }

    @Test
    @DisplayName("alterarDataLimite: deve atualizar data etapa 2 quando situacao contem MAPA")
    void alterarDataLimite_DeveAtualizarEtapa2() {
        Long codSubprocesso = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
        sp.setUnidade(new Unidade());

        when(crudService.buscarSubprocesso(codSubprocesso)).thenReturn(sp);

        service.alterarDataLimite(codSubprocesso, LocalDate.now());

        assertThat(sp.getDataLimiteEtapa2()).isEqualTo(LocalDate.now().atStartOfDay());
        verify(repositorioSubprocesso).save(sp);
    }

    @Test
    @DisplayName("reabrirCadastro: deve falhar se tipo processo incorreto")
    void reabrirCadastro_ErroTipoProcesso() {
        Long codigo = 1L;
        Subprocesso sp = new Subprocesso();
        Processo p = new Processo();
        p.setTipo(TipoProcesso.REVISAO);
        sp.setProcesso(p);

        when(crudService.buscarSubprocesso(codigo)).thenReturn(sp);

        assertThatThrownBy(() -> service.reabrirCadastro(codigo, "justificativa"))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("apenas para processos de Mapeamento");
    }

    @Test
    @DisplayName("reabrirCadastro: deve falhar se situação inicial")
    void reabrirCadastro_ErroSituacaoInicial() {
        Long codigo = 1L;
        Subprocesso sp = new Subprocesso();
        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        sp.setProcesso(p);
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        when(crudService.buscarSubprocesso(codigo)).thenReturn(sp);

        assertThatThrownBy(() -> service.reabrirCadastro(codigo, "justificativa"))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("ainda está em fase de cadastro");
    }

    @Test
    @DisplayName("reabrirCadastro: sucesso com captura de erro na notificação")
    void reabrirCadastro_SucessoComErroNotificacao() {
        Long codigo = 1L;
        Subprocesso sp = new Subprocesso();
        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        sp.setProcesso(p);
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        sp.setUnidade(new Unidade());

        when(crudService.buscarSubprocesso(codigo)).thenReturn(sp);
        when(unidadeService.buscarEntidadePorSigla("SEDOC")).thenReturn(new Unidade());
        doThrow(new RuntimeException("Erro alerta")).when(alertaService).criarAlertaReaberturaCadastro(any(), any(), any());

        assertThatCode(() -> service.reabrirCadastro(codigo, "justif"))
                .doesNotThrowAnyException();

        verify(repositorioSubprocesso).save(sp);
        verify(repositorioMovimentacao).save(any(Movimentacao.class));
    }

    @Test
    @DisplayName("reabrirRevisaoCadastro: deve falhar se tipo processo incorreto")
    void reabrirRevisaoCadastro_ErroTipoProcesso() {
        Long codigo = 1L;
        Subprocesso sp = new Subprocesso();
        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        sp.setProcesso(p);

        when(crudService.buscarSubprocesso(codigo)).thenReturn(sp);

        assertThatThrownBy(() -> service.reabrirRevisaoCadastro(codigo, "justificativa"))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("apenas para processos de Revisão");
    }

    @Test
    @DisplayName("reabrirRevisaoCadastro: deve falhar se situação inicial")
    void reabrirRevisaoCadastro_ErroSituacaoInicial() {
        Long codigo = 1L;
        Subprocesso sp = new Subprocesso();
        Processo p = new Processo();
        p.setTipo(TipoProcesso.REVISAO);
        sp.setProcesso(p);
        sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);

        when(crudService.buscarSubprocesso(codigo)).thenReturn(sp);

        assertThatThrownBy(() -> service.reabrirRevisaoCadastro(codigo, "justificativa"))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("ainda está em fase de revisão");
    }

    @Test
    @DisplayName("reabrirRevisaoCadastro: sucesso com captura de erro na notificação")
    void reabrirRevisaoCadastro_SucessoComErroNotificacao() {
        Long codigo = 1L;
        Subprocesso sp = new Subprocesso();
        Processo p = new Processo();
        p.setTipo(TipoProcesso.REVISAO);
        sp.setProcesso(p);
        sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
        sp.setUnidade(new Unidade());

        when(crudService.buscarSubprocesso(codigo)).thenReturn(sp);
        when(unidadeService.buscarEntidadePorSigla("SEDOC")).thenReturn(new Unidade());
        doThrow(new RuntimeException("Erro alerta")).when(alertaService).criarAlertaReaberturaRevisao(any(), any(), any());

        assertThatCode(() -> service.reabrirRevisaoCadastro(codigo, "justif"))
                .doesNotThrowAnyException();

        verify(repositorioSubprocesso).save(sp);
        verify(repositorioMovimentacao).save(any(Movimentacao.class));
    }
}
