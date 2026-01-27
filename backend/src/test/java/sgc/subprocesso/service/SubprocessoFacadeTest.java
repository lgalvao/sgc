package sgc.subprocesso.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.analise.AnaliseFacade;
import sgc.mapa.mapper.ConhecimentoMapper;
import sgc.mapa.service.AtividadeService;
import sgc.mapa.service.CompetenciaService;
import sgc.mapa.service.ConhecimentoService;
import sgc.mapa.service.MapaFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.subprocesso.dto.AtualizarSubprocessoRequest;
import sgc.subprocesso.dto.CriarSubprocessoRequest;
import sgc.subprocesso.mapper.MapaAjusteMapper;
import sgc.subprocesso.mapper.SubprocessoDetalheMapper;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.service.crud.SubprocessoCrudService;
import sgc.subprocesso.service.crud.SubprocessoValidacaoService;
import sgc.subprocesso.service.workflow.SubprocessoWorkflowService;

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
    private SubprocessoWorkflowService workflowService;
    @Mock
    private UsuarioFacade usuarioService;
    @Mock
    private MapaFacade mapaFacade;
    @Mock
    private AtividadeService atividadeService;
    @Mock
    private MovimentacaoRepo repositorioMovimentacao;
    @Mock
    private SubprocessoDetalheMapper subprocessoDetalheMapper;
    @Mock
    private ConhecimentoMapper conhecimentoMapper;
    @Mock
    private AnaliseFacade analiseFacade;
    @Mock
    private CompetenciaService competenciaService;
    @Mock
    private ConhecimentoService conhecimentoService;
    @Mock
    private MapaAjusteMapper mapaAjusteMapper;
    @Mock
    private sgc.seguranca.acesso.AccessControlService accessControlService;
    @Mock
    private sgc.subprocesso.model.SubprocessoRepo subprocessoRepo;
    @Mock
    private sgc.subprocesso.model.SubprocessoMovimentacaoRepo movimentacaoRepo;
    @Mock
    private sgc.mapa.service.CopiaMapaService copiaMapaService;
    @Mock
    private sgc.mapa.mapper.AtividadeMapper atividadeMapper;

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
        verify(workflowService).atualizarSituacaoParaEmAndamento(1L);

        facade.listarSubprocessosHomologados();
        verify(workflowService).listarSubprocessosHomologados();

        facade.reabrirCadastro(1L, "j");
        verify(workflowService).reabrirCadastro(1L, "j");

        facade.reabrirRevisaoCadastro(1L, "j");
        verify(workflowService).reabrirRevisaoCadastro(1L, "j");

        facade.alterarDataLimite(1L, java.time.LocalDate.now());
        verify(workflowService).alterarDataLimite(1L, java.time.LocalDate.now());
    }

    @Test
    void deveVerificarPermissaoAoObterCadastro() {
        Long codSubprocesso = 1L;
        sgc.subprocesso.model.Subprocesso subprocesso = new sgc.subprocesso.model.Subprocesso();
        sgc.mapa.model.Mapa mapa = new sgc.mapa.model.Mapa();
        mapa.setCodigo(10L);
        subprocesso.setMapa(mapa);
        sgc.organizacao.model.Unidade unidade = new sgc.organizacao.model.Unidade();
        unidade.setSigla("TEST");
        subprocesso.setUnidade(unidade);

        sgc.organizacao.model.Usuario usuario = new sgc.organizacao.model.Usuario();

        org.mockito.Mockito.when(crudService.buscarSubprocesso(codSubprocesso)).thenReturn(subprocesso);
        org.mockito.Mockito.when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);
        org.mockito.Mockito.when(atividadeService.buscarPorMapaCodigoComConhecimentos(10L)).thenReturn(Collections.emptyList());

        facade.obterCadastro(codSubprocesso);

        verify(accessControlService).verificarPermissao(usuario, sgc.seguranca.acesso.Acao.VISUALIZAR_SUBPROCESSO, subprocesso);
    }
}
