package sgc.subprocesso.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.service.MapaManutencaoService;
import sgc.organizacao.UsuarioFacade;
import sgc.subprocesso.dto.AtualizarSubprocessoRequest;
import sgc.subprocesso.dto.CriarSubprocessoRequest;
import sgc.subprocesso.service.crud.SubprocessoCrudService;
import sgc.subprocesso.service.crud.SubprocessoValidacaoService;
import sgc.subprocesso.service.workflow.SubprocessoAdminWorkflowService;
import sgc.subprocesso.service.workflow.SubprocessoCadastroWorkflowService;
import sgc.subprocesso.service.workflow.SubprocessoMapaWorkflowService;

import java.util.Collections;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("SubprocessoFacadeTest")
class SubprocessoFacadeTest {

    @Mock
    private SubprocessoCrudService crudService;
    @Mock
    private SubprocessoValidacaoService validacaoService;
    @Mock
    private SubprocessoCadastroWorkflowService cadastroWorkflowService;
    @Mock
    private SubprocessoMapaWorkflowService mapaWorkflowService;
    @Mock
    private SubprocessoAdminWorkflowService adminWorkflowService;
    @Mock
    private SubprocessoAjusteMapaService ajusteMapaService;
    @Mock
    private SubprocessoAtividadeService atividadeService;
    @Mock
    private SubprocessoContextoService contextoService;
    @Mock
    private SubprocessoPermissaoCalculator permissaoCalculator;
    @Mock
    private UsuarioFacade usuarioService;
    @Mock
    private MapaManutencaoService mapaManutencaoService;
    @Mock
    private sgc.seguranca.acesso.AccessControlService accessControlService;

    @InjectMocks
    private SubprocessoFacade facade;

    @Test
    void deveDelegarOperacoesBasicas() {
        facade.buscarSubprocesso(1L);
        verify(crudService).buscarSubprocesso(1L);

        facade.listar();
        verify(crudService).listar();

        CriarSubprocessoRequest criarRequest = CriarSubprocessoRequest.builder().build();
        facade.criar(criarRequest);
        verify(crudService).criar(criarRequest);

        AtualizarSubprocessoRequest atualizarRequest = AtualizarSubprocessoRequest.builder().build();
        facade.atualizar(1L, atualizarRequest);
        verify(crudService).atualizar(1L, atualizarRequest);

        facade.excluir(1L);
        verify(crudService).excluir(1L);
    }

    @Test
    void deveDelegarOutrosServicos() {
        facade.buscarSubprocessoComMapa(1L);
        verify(crudService).buscarSubprocessoComMapa(1L);

        facade.obterPorProcessoEUnidade(1L, 2L);
        verify(crudService).obterPorProcessoEUnidade(1L, 2L);

        facade.obterSituacao(1L);
        verify(crudService).obterStatus(1L);

        facade.obterAtividadesSemConhecimento(1L);
        verify(validacaoService).obterAtividadesSemConhecimento(1L);

        facade.obterEntidadePorCodigoMapa(1L);
        verify(crudService).obterEntidadePorCodigoMapa(1L);

        facade.verificarAcessoUnidadeAoProcesso(1L, Collections.singletonList(2L));
        verify(crudService).verificarAcessoUnidadeAoProcesso(1L, Collections.singletonList(2L));

        facade.validarCadastro(1L);
        verify(validacaoService).validarCadastro(1L);

        facade.validarExistenciaAtividades(1L);
        verify(validacaoService).validarExistenciaAtividades(1L);

        facade.validarAssociacoesMapa(1L);
        verify(validacaoService).validarAssociacoesMapa(1L);

        facade.atualizarSituacaoParaEmAndamento(1L);
        verify(adminWorkflowService).atualizarSituacaoParaEmAndamento(1L);

        facade.listarSubprocessosHomologados();
        verify(adminWorkflowService).listarSubprocessosHomologados();

        facade.reabrirCadastro(1L, "j");
        verify(cadastroWorkflowService).reabrirCadastro(1L, "j");

        facade.reabrirRevisaoCadastro(1L, "j");
        verify(cadastroWorkflowService).reabrirRevisaoCadastro(1L, "j");

        facade.alterarDataLimite(1L, java.time.LocalDate.now());
        verify(adminWorkflowService).alterarDataLimite(1L, java.time.LocalDate.now());
    }

    @Test
    void deveDelegarObterCadastroParaContextoService() {
        Long codSubprocesso = 1L;

        facade.obterCadastro(codSubprocesso);

        verify(contextoService).obterCadastro(codSubprocesso);
    }
}
