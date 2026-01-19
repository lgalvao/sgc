package sgc.subprocesso.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.alerta.AlertaFacade;
import sgc.comum.erros.ErroValidacao;
import sgc.comum.repo.RepositorioComum;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.service.crud.SubprocessoCrudService;
import sgc.subprocesso.service.workflow.SubprocessoWorkflowService;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("SubprocessoWorkflowService Test (Administrative Operations)")
class SubprocessoWorkflowServiceTest {

    @Mock
    private SubprocessoRepo repositorioSubprocesso;
    @Mock
    private SubprocessoCrudService crudService;
    @Mock
    private AlertaFacade alertaService;
    @Mock
    private UnidadeFacade unidadeService;
    @Mock
    private RepositorioComum repo;
    @Mock
    private MovimentacaoRepo repositorioMovimentacao;
    @Mock
    private sgc.subprocesso.service.workflow.SubprocessoTransicaoService transicaoService;
    @Mock
    private sgc.analise.AnaliseFacade analiseFacade;
    @Mock
    private sgc.subprocesso.service.crud.SubprocessoValidacaoService validacaoService;
    @Mock
    private sgc.mapa.service.ImpactoMapaService impactoMapaService;
    @Mock
    private sgc.seguranca.acesso.AccessControlService accessControlService;
    @Mock
    private sgc.mapa.service.CompetenciaService competenciaService;
    @Mock
    private sgc.mapa.service.AtividadeService atividadeService;
    @Mock
    private sgc.mapa.service.MapaFacade mapaFacade;

    @InjectMocks
    private SubprocessoWorkflowService service;

    @Test
    @DisplayName("Alterar data limite para cadastro")
    void alterarDataLimiteCadastro() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        sp.setProcesso(new Processo());
        sp.setUnidade(new Unidade());

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);

        service.alterarDataLimite(1L, LocalDate.now().plusDays(5));

        assertThat(sp.getDataLimiteEtapa1()).isEqualTo(LocalDate.now().plusDays(5).atStartOfDay());
        verify(repositorioSubprocesso).save(sp);
        verify(alertaService).criarAlertaAlteracaoDataLimite(any(), any(), anyString(), eq(1));
    }

    @Test
    @DisplayName("Alterar data limite para mapa")
    void alterarDataLimiteMapa() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO);
        sp.setProcesso(new Processo());
        sp.setUnidade(new Unidade());

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);

        service.alterarDataLimite(1L, LocalDate.now().plusDays(5));

        assertThat(sp.getDataLimiteEtapa2()).isEqualTo(LocalDate.now().plusDays(5).atStartOfDay());
        verify(repositorioSubprocesso).save(sp);
        verify(alertaService).criarAlertaAlteracaoDataLimite(any(), any(), anyString(), eq(2));
    }

    @Test
    @DisplayName("Alterar data limite outros")
    void alterarDataLimiteOutros() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
        sp.setProcesso(new Processo());

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);

        service.alterarDataLimite(1L, LocalDate.now().plusDays(5));

        assertThat(sp.getDataLimiteEtapa1()).isEqualTo(LocalDate.now().plusDays(5).atStartOfDay());
    }

    @Test
    @DisplayName("Atualizar situacao para em andamento (Mapeamento)")
    void atualizarSituacaoMapeamento() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
        sp.setProcesso(new Processo());
        sp.getProcesso().setTipo(TipoProcesso.MAPEAMENTO);

        when(repositorioSubprocesso.findByMapaCodigo(10L)).thenReturn(Optional.of(sp));

        service.atualizarSituacaoParaEmAndamento(10L);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        verify(repositorioSubprocesso).save(sp);
    }

    @Test
    @DisplayName("Atualizar situacao para em andamento (Revisao)")
    void atualizarSituacaoRevisao() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
        sp.setProcesso(new Processo());
        sp.getProcesso().setTipo(TipoProcesso.REVISAO);

        when(repositorioSubprocesso.findByMapaCodigo(10L)).thenReturn(Optional.of(sp));

        service.atualizarSituacaoParaEmAndamento(10L);

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
        verify(repositorioSubprocesso).save(sp);
    }

    @Test
    @DisplayName("Reabrir cadastro sucesso")
    void reabrirCadastroSucesso() {
        Subprocesso sp = new Subprocesso();
        sp.setProcesso(new Processo());
        sp.getProcesso().setTipo(TipoProcesso.MAPEAMENTO);
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        sp.setUnidade(new Unidade());
        sp.getUnidade().setUnidadeSuperior(new Unidade());

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        when(unidadeService.buscarEntidadePorSigla("SEDOC")).thenReturn(new Unidade());

        service.reabrirCadastro(1L, "Just");

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        verify(alertaService).criarAlertaReaberturaCadastro(any(), any(), any());
        verify(alertaService).criarAlertaReaberturaCadastroSuperior(any(), any(), any());
    }

    @Test
    @DisplayName("Reabrir cadastro falha tipo invalido")
    void reabrirCadastroFalhaTipo() {
        Subprocesso sp = new Subprocesso();
        sp.setProcesso(new Processo());
        sp.getProcesso().setTipo(TipoProcesso.REVISAO);

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);

        assertThatThrownBy(() -> service.reabrirCadastro(1L, "Just"))
            .isInstanceOf(ErroValidacao.class);
    }

    @Test
    @DisplayName("Reabrir cadastro falha estado invalido")
    void reabrirCadastroFalhaEstado() {
        Subprocesso sp = new Subprocesso();
        sp.setProcesso(new Processo());
        sp.getProcesso().setTipo(TipoProcesso.MAPEAMENTO);
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);

        assertThatThrownBy(() -> service.reabrirCadastro(1L, "Just"))
            .isInstanceOf(ErroValidacao.class);
    }

    @Test
    @DisplayName("Reabrir revisao sucesso")
    void reabrirRevisaoSucesso() {
        Subprocesso sp = new Subprocesso();
        sp.setProcesso(new Processo());
        sp.getProcesso().setTipo(TipoProcesso.REVISAO);
        sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
        sp.setUnidade(new Unidade());
        sp.getUnidade().setUnidadeSuperior(new Unidade());

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        when(unidadeService.buscarEntidadePorSigla("SEDOC")).thenReturn(new Unidade());

        service.reabrirRevisaoCadastro(1L, "Just");

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
        verify(alertaService).criarAlertaReaberturaRevisao(any(), any(), any());
        verify(alertaService).criarAlertaReaberturaRevisaoSuperior(any(), any(), any());
    }

    @Test
    @DisplayName("Reabrir revisao falha tipo invalido")
    void reabrirRevisaoFalhaTipo() {
        Subprocesso sp = new Subprocesso();
        sp.setProcesso(new Processo());
        sp.getProcesso().setTipo(TipoProcesso.MAPEAMENTO);

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);

        assertThatThrownBy(() -> service.reabrirRevisaoCadastro(1L, "Just"))
            .isInstanceOf(ErroValidacao.class);
    }

    @Test
    @DisplayName("Reabrir revisao falha se ja esta em andamento")
    void reabrirRevisaoFalhaAndamento() {
        Subprocesso sp = new Subprocesso();
        sp.setProcesso(new Processo());
        sp.getProcesso().setTipo(TipoProcesso.REVISAO);
        sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);

        assertThatThrownBy(() -> service.reabrirRevisaoCadastro(1L, "Just"))
            .isInstanceOf(ErroValidacao.class)
            .hasMessageContaining("em fase de revisão");
    }

    @Test
    @DisplayName("Buscar subprocessos homologados")
    void buscarSubprocessosHomologados() {
        service.listarSubprocessosHomologados();
        verify(repositorioSubprocesso).findBySituacao(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
    }

    @Test
    @DisplayName("Deve logar erro mas não falhar se erro ao enviar notificação na alteração de data")
    void erroNotificacaoAlteracaoData() {
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        sp.setProcesso(new Processo());
        sp.setUnidade(new Unidade());

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        doThrow(new RuntimeException("Simulated error")).when(alertaService).criarAlertaAlteracaoDataLimite(any(), any(), any(), anyInt());

        // Não deve lançar exceção
        service.alterarDataLimite(1L, LocalDate.now());
        
        verify(repositorioSubprocesso).save(sp);
    }

    @Test
    @DisplayName("Deve logar erro mas não falhar se erro ao enviar notificação na reabertura")
    void erroNotificacaoReabertura() {
        Subprocesso sp = new Subprocesso();
        sp.setProcesso(new Processo());
        sp.getProcesso().setTipo(TipoProcesso.MAPEAMENTO);
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        sp.setUnidade(new Unidade());

        when(crudService.buscarSubprocesso(1L)).thenReturn(sp);
        doThrow(new RuntimeException("Simulated error")).when(alertaService).criarAlertaReaberturaCadastro(any(), any(), any());

        // Não deve lançar exceção
        service.reabrirCadastro(1L, "Just");
        
        verify(repositorioSubprocesso).save(sp);
    }
}
