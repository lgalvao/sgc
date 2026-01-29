package sgc.subprocesso.service.workflow;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sgc.alerta.AlertaFacade;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.service.crud.SubprocessoCrudService;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoAdminWorkflowService")
class SubprocessoAdminWorkflowServiceTest {
    @InjectMocks
    private SubprocessoAdminWorkflowService service;

    @Mock
    private SubprocessoRepo repositorioSubprocesso;
    @Mock
    private SubprocessoCrudService crudService;
    @Mock
    private AlertaFacade alertaService;

    @Test
    @DisplayName("alterarDataLimite - Etapa 1")
    void alterarDataLimite_Etapa1() {
        Long codigo = 1L;
        LocalDate novaData = LocalDate.of(2023, 10, 1);
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        sp.setUnidade(new Unidade());

        when(crudService.buscarSubprocesso(codigo)).thenReturn(sp);

        service.alterarDataLimite(codigo, novaData);

        verify(repositorioSubprocesso).save(sp);
        assertEquals(novaData.atStartOfDay(), sp.getDataLimiteEtapa1());
        verify(alertaService).criarAlertaAlteracaoDataLimite(any(), any(), any(), eq(1));
    }

    @Test
    @DisplayName("alterarDataLimite - Etapa 2")
    void alterarDataLimite_Etapa2() {
        Long codigo = 1L;
        LocalDate novaData = LocalDate.of(2023, 10, 1);
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
        sp.setUnidade(new Unidade());

        when(crudService.buscarSubprocesso(codigo)).thenReturn(sp);

        service.alterarDataLimite(codigo, novaData);

        verify(repositorioSubprocesso).save(sp);
        assertEquals(novaData.atStartOfDay(), sp.getDataLimiteEtapa2());
        verify(alertaService).criarAlertaAlteracaoDataLimite(any(), any(), any(), eq(2));
    }

    @Test
    @DisplayName("atualizarSituacaoParaEmAndamento - Mapeamento")
    void atualizarSituacaoParaEmAndamento_Mapeamento() {
        Long codMapa = 100L;
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        sp.setProcesso(p);

        when(repositorioSubprocesso.findByMapaCodigo(codMapa)).thenReturn(Optional.of(sp));

        service.atualizarSituacaoParaEmAndamento(codMapa);

        assertEquals(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, sp.getSituacao());
        verify(repositorioSubprocesso).save(sp);
    }

    @Test
    @DisplayName("atualizarSituacaoParaEmAndamento - Outro Tipo (Inalterado/Branch Gap)")
    void atualizarSituacaoParaEmAndamento_OutroTipo() {
        Long codMapa = 100L;
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
        Processo p = new Processo();
        p.setTipo(TipoProcesso.DIAGNOSTICO); // Caso não tratado nos ifs
        sp.setProcesso(p);

        when(repositorioSubprocesso.findByMapaCodigo(codMapa)).thenReturn(Optional.of(sp));

        service.atualizarSituacaoParaEmAndamento(codMapa);

        // Situação não deve mudar
        assertEquals(SituacaoSubprocesso.NAO_INICIADO, sp.getSituacao());
        // Não deve salvar
        verify(repositorioSubprocesso, never()).save(sp);
    }

    @Test
    @DisplayName("atualizarSituacaoParaEmAndamento - Revisao")
    void atualizarSituacaoParaEmAndamento_Revisao() {
        Long codMapa = 100L;
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
        Processo p = new Processo();
        p.setTipo(TipoProcesso.REVISAO);
        sp.setProcesso(p);

        when(repositorioSubprocesso.findByMapaCodigo(codMapa)).thenReturn(Optional.of(sp));

        service.atualizarSituacaoParaEmAndamento(codMapa);

        assertEquals(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO, sp.getSituacao());
        verify(repositorioSubprocesso).save(sp);
    }

    @Test
    @DisplayName("listarSubprocessosHomologados")
    void listarSubprocessosHomologados() {
        service.listarSubprocessosHomologados();
        verify(repositorioSubprocesso).findBySituacao(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
    }

    @Test
    @DisplayName("alterarDataLimite - Erro na notificacao nao deve falhar operacao")
    void alterarDataLimite_ErroNotificacao() {
        Long codigo = 1L;
        LocalDate novaData = LocalDate.of(2023, 10, 1);
        Subprocesso sp = new Subprocesso();
        sp.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        sp.setUnidade(new Unidade());

        when(crudService.buscarSubprocesso(codigo)).thenReturn(sp);
        doThrow(new RuntimeException("Error sending alert")).when(alertaService).criarAlertaAlteracaoDataLimite(any(), any(), any(), anyInt());

        service.alterarDataLimite(codigo, novaData);

        verify(repositorioSubprocesso).save(sp);
        assertEquals(novaData.atStartOfDay(), sp.getDataLimiteEtapa1());
    }
}