package sgc.subprocesso.service.workflow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.alerta.AlertaFacade;
import sgc.analise.AnaliseFacade;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.seguranca.acesso.AccessControlService;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.service.crud.SubprocessoCrudService;
import sgc.subprocesso.service.crud.SubprocessoValidacaoService;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class SubprocessoCadastroWorkflowServiceCoverageTest {

    @Mock private SubprocessoRepo subprocessoRepo;
    @Mock private SubprocessoCrudService crudService;
    @Mock private AlertaFacade alertaService;
    @Mock private UnidadeFacade unidadeService;
    @Mock private SubprocessoTransicaoService transicaoService;
    @Mock private AnaliseFacade analiseFacade;
    @Mock private UsuarioFacade usuarioServiceFacade;
    @Mock private SubprocessoValidacaoService validacaoService;
    @Mock private AccessControlService accessControlService;

    @InjectMocks
    private SubprocessoCadastroWorkflowService service;

    @Test
    @DisplayName("reabrirRevisaoCadastro com unidade superior deve gerar alertas para superiores")
    void reabrirRevisaoCadastro_ComSuperior() {
        Long codigo = 1L;
        Unidade sup = new Unidade();
        sup.setSigla("SUP");
        Unidade u = new Unidade();
        u.setSigla("U");
        u.setUnidadeSuperior(sup);

        Subprocesso sp = new Subprocesso();
        sp.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
        sp.setUnidade(u);

        when(crudService.buscarSubprocesso(codigo)).thenReturn(sp);
        when(unidadeService.buscarEntidadePorSigla("ADMIN")).thenReturn(new Unidade());

        service.reabrirRevisaoCadastro(codigo, "Justificativa");

        verify(alertaService).criarAlertaReaberturaRevisao(any(), eq(u), eq("Justificativa"));
        verify(alertaService).criarAlertaReaberturaRevisaoSuperior(any(), eq(sup), eq(u));
    }

    @Test
    @DisplayName("disponibilizarCadastro com unidade sem superior deve logar warning")
    void disponibilizarCadastro_SemSuperior() {
        Long codigo = 1L;
        Unidade u = new Unidade();
        u.setSigla("U");
        u.setUnidadeSuperior(null);
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(codigo);
        sp.setUnidade(u);

        when(crudService.buscarSubprocesso(codigo)).thenReturn(sp);
        when(validacaoService.obterAtividadesSemConhecimento(codigo)).thenReturn(Collections.emptyList());

        service.disponibilizarCadastro(codigo, new Usuario());

        // Assert that logic continued (transition registered)
        verify(transicaoService).registrar(any());
    }
}
