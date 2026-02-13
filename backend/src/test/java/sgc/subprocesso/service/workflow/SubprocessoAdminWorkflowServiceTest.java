package sgc.subprocesso.service.workflow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.alerta.AlertaFacade;
import sgc.comum.repo.ComumRepo;
import sgc.notificacao.NotificacaoEmailService;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.service.crud.SubprocessoCrudService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoAdminWorkflowService")
class SubprocessoAdminWorkflowServiceTest {
    @InjectMocks
    private SubprocessoAdminWorkflowService service;

    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private SubprocessoCrudService crudService;
    @Mock
    private AlertaFacade alertaService;
    @Mock
    private NotificacaoEmailService notificacaoEmailService;
    @Mock
    private ComumRepo repo;

    @Test
    @DisplayName("alterarDataLimite - Etapa 1")
    void alterarDataLimite_Etapa1() {
        Long codigo = 1L;
        LocalDate novaData = LocalDate.of(2023, 10, 1);
        Subprocesso sp = new Subprocesso();
        sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        Unidade unidade = new Unidade();
        unidade.setSigla("SECAO_211");
        sp.setUnidade(unidade);
        Processo processo = new Processo();
        processo.setDescricao("Processo teste");
        sp.setProcesso(processo);

        when(crudService.buscarSubprocesso(codigo)).thenReturn(sp);

        service.alterarDataLimite(codigo, novaData);

        verify(subprocessoRepo).save(sp);
        assertEquals(novaData.atStartOfDay(), sp.getDataLimiteEtapa1());
        verify(notificacaoEmailService).enviarEmail(anyString(), eq("SGC: Data limite alterada"), contains("foi alterada para"));
        verify(alertaService).criarAlertaAlteracaoDataLimite(any(), any(), any(), eq(1));
    }

    @Test
    @DisplayName("alterarDataLimite - Situação Genérica (Else)")
    void alterarDataLimite_SituacaoGenerica() {
        Long codigo = 1L;
        LocalDate novaData = LocalDate.of(2023, 10, 1);
        Subprocesso sp = new Subprocesso();
        sp.setSituacaoForcada(SituacaoSubprocesso.NAO_INICIADO);
        Unidade unidade = new Unidade();
        unidade.setSigla("SECAO_211");
        sp.setUnidade(unidade);
        Processo processo = new Processo();
        processo.setDescricao("Processo teste");
        sp.setProcesso(processo);

        when(crudService.buscarSubprocesso(codigo)).thenReturn(sp);

        service.alterarDataLimite(codigo, novaData);

        verify(subprocessoRepo).save(sp);
        assertEquals(novaData.atStartOfDay(), sp.getDataLimiteEtapa1());
        verify(notificacaoEmailService).enviarEmail(anyString(), eq("SGC: Data limite alterada"), contains("foi alterada para"));
        verify(alertaService).criarAlertaAlteracaoDataLimite(any(), any(), any(), eq(1));
    }

    @Test
    @DisplayName("alterarDataLimite - Etapa 2")
    void alterarDataLimite_Etapa2() {
        Long codigo = 1L;
        LocalDate novaData = LocalDate.of(2023, 10, 1);
        Subprocesso sp = new Subprocesso();
        sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
        Unidade unidade = new Unidade();
        unidade.setSigla("SECAO_211");
        sp.setUnidade(unidade);
        Processo processo = new Processo();
        processo.setDescricao("Processo teste");
        sp.setProcesso(processo);

        when(crudService.buscarSubprocesso(codigo)).thenReturn(sp);

        service.alterarDataLimite(codigo, novaData);

        verify(subprocessoRepo).save(sp);
        assertEquals(novaData.atStartOfDay(), sp.getDataLimiteEtapa2());
        verify(notificacaoEmailService).enviarEmail(anyString(), eq("SGC: Data limite alterada"), contains("foi alterada para"));
        verify(alertaService).criarAlertaAlteracaoDataLimite(any(), any(), any(), eq(2));
    }

    @Test
    @DisplayName("atualizarSituacaoParaEmAndamento - Mapeamento")
    void atualizarParaEmAndamento_Mapeamento() {
        Long codMapa = 100L;
        Subprocesso sp = new Subprocesso();
        sp.setSituacaoForcada(SituacaoSubprocesso.NAO_INICIADO);
        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        sp.setProcesso(p);

        when(repo.buscar(Subprocesso.class, "mapa.codigo", codMapa)).thenReturn(sp);

        service.atualizarParaEmAndamento(codMapa);

        assertEquals(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, sp.getSituacao());
        verify(subprocessoRepo).save(sp);
    }

    @Test
    @DisplayName("atualizarSituacaoParaEmAndamento - Outro Tipo (Inalterado/Branch Gap)")
    void atualizarParaEmAndamento_OutroTipo() {
        Long codMapa = 100L;
        Subprocesso sp = new Subprocesso();
        sp.setSituacaoForcada(SituacaoSubprocesso.NAO_INICIADO);
        Processo p = new Processo();
        p.setTipo(TipoProcesso.DIAGNOSTICO); // Caso não tratado nos ifs
        sp.setProcesso(p);

        when(repo.buscar(Subprocesso.class, "mapa.codigo", codMapa)).thenReturn(sp);

        service.atualizarParaEmAndamento(codMapa);

        // Situação não deve mudar
        assertEquals(SituacaoSubprocesso.NAO_INICIADO, sp.getSituacao());
        // Não deve salvar
        verify(subprocessoRepo, never()).save(sp);
    }

    @Test
    @DisplayName("atualizarSituacaoParaEmAndamento - Revisao")
    void atualizarParaEmAndamento_Revisao() {
        Long codMapa = 100L;
        Subprocesso sp = new Subprocesso();
        sp.setSituacaoForcada(SituacaoSubprocesso.NAO_INICIADO);
        Processo p = new Processo();
        p.setTipo(TipoProcesso.REVISAO);
        sp.setProcesso(p);

        when(repo.buscar(Subprocesso.class, "mapa.codigo", codMapa)).thenReturn(sp);

        service.atualizarParaEmAndamento(codMapa);

        assertEquals(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO, sp.getSituacao());
        verify(subprocessoRepo).save(sp);
    }

    @Test
    @DisplayName("listarSubprocessosHomologados")
    void listarSubprocessosHomologados() {
        service.listarSubprocessosHomologados();
        verify(subprocessoRepo).findBySituacao(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
    }

    @Test
    @DisplayName("alterarDataLimite - Erro na notificacao nao deve falhar operacao")
    void alterarDataLimite_ErroNotificacao() {
        Long codigo = 1L;
        LocalDate novaData = LocalDate.of(2023, 10, 1);
        Subprocesso sp = new Subprocesso();
        sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        Unidade unidade = new Unidade();
        unidade.setSigla("SECAO_211");
        sp.setUnidade(unidade);
        Processo processo = new Processo();
        processo.setDescricao("Processo teste");
        sp.setProcesso(processo);

        when(crudService.buscarSubprocesso(codigo)).thenReturn(sp);
        doThrow(new RuntimeException("Error sending alert")).when(alertaService).criarAlertaAlteracaoDataLimite(any(), any(), any(), anyInt());

        service.alterarDataLimite(codigo, novaData);

        verify(subprocessoRepo).save(sp);
        assertEquals(novaData.atStartOfDay(), sp.getDataLimiteEtapa1());
    }
}
