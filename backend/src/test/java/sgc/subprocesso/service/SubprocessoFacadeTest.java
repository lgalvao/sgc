package sgc.subprocesso.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.organizacao.UsuarioService;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.dto.SubprocessoDto;
import sgc.subprocesso.service.decomposed.SubprocessoCrudService;
import sgc.subprocesso.service.decomposed.SubprocessoDetalheService;
import sgc.subprocesso.service.decomposed.SubprocessoValidacaoService;
import sgc.subprocesso.service.decomposed.SubprocessoWorkflowService;

import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("SubprocessoFacadeTest")
class SubprocessoFacadeTest {

    @Mock private SubprocessoCrudService crudService;
    @Mock private SubprocessoDetalheService detalheService;
    @Mock private SubprocessoValidacaoService validacaoService;
    @Mock private SubprocessoWorkflowService workflowService;
    @Mock private SubprocessoCadastroWorkflowService cadastroWorkflowService;
    @Mock private SubprocessoMapaWorkflowService mapaWorkflowService;
    @Mock private SubprocessoContextoService contextoService;
    @Mock private SubprocessoMapaService mapaService;
    @Mock private SubprocessoPermissaoCalculator permissaoCalculator;
    @Mock private UsuarioService usuarioService;

    @InjectMocks
    private SubprocessoFacade facade;

    @Test
    void deveDelegarOperacoesBasicas() {
        facade.buscarSubprocesso(1L);
        verify(crudService).buscarSubprocesso(1L);

        facade.listar();
        verify(crudService).listar();

        SubprocessoDto dto = new SubprocessoDto();
        facade.criar(dto);
        verify(crudService).criar(dto);

        facade.atualizar(1L, dto);
        verify(crudService).atualizar(1L, dto);

        facade.excluir(1L);
        verify(crudService).excluir(1L);
    }

    @Test
    void deveDelegarOutrosServicos() {
        facade.obterContextoEdicao(1L, Perfil.ADMIN);
        verify(contextoService).obterContextoEdicao(1L, Perfil.ADMIN);

        facade.importarAtividades(1L, 2L);
        verify(mapaService).importarAtividades(1L, 2L);
        
        facade.buscarSubprocessoComMapa(1L);
        verify(crudService).buscarSubprocessoComMapa(1L);
        
        facade.obterPorProcessoEUnidade(1L, 2L);
        verify(crudService).obterPorProcessoEUnidade(1L, 2L);
        
        Usuario mockUser = new Usuario();
        when(usuarioService.obterUsuarioAutenticado()).thenReturn(mockUser);
        facade.obterDetalhes(1L, Perfil.ADMIN);
        verify(detalheService).obterDetalhes(1L, Perfil.ADMIN, mockUser);
        
        facade.obterSituacao(1L);
        verify(crudService).obterStatus(1L);
        
        facade.listarAtividadesSubprocesso(1L);
        verify(detalheService).listarAtividadesSubprocesso(1L);
        
        facade.obterAtividadesSemConhecimento(1L);
        verify(validacaoService).obterAtividadesSemConhecimento(1L);
        
        facade.obterEntidadePorCodigoMapa(1L);
        verify(crudService).obterEntidadePorCodigoMapa(1L);
        
        facade.verificarAcessoUnidadeAoProcesso(1L, Collections.singletonList(2L));
        verify(crudService).verificarAcessoUnidadeAoProcesso(1L, Collections.singletonList(2L));
        
        facade.obterSugestoes(1L);
        verify(detalheService).obterSugestoes(1L);
        
        facade.obterMapaParaAjuste(1L);
        verify(detalheService).obterMapaParaAjuste(1L);
        
        facade.obterPermissoes(1L);
        verify(detalheService).obterPermissoes(1L, mockUser);
        
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
        
        facade.obterCadastro(1L);
        verify(detalheService).obterCadastro(1L);
        
        facade.salvarAjustesMapa(1L, Collections.emptyList(), "t");
        verify(mapaService).salvarAjustesMapa(1L, Collections.emptyList(), "t");
    }
}
