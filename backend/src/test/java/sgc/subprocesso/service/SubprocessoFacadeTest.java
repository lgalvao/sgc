package sgc.subprocesso.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.dto.SubprocessoDto;

import java.util.Collections;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoFacadeTest")
class SubprocessoFacadeTest {

    @Mock private SubprocessoFacade subprocessoFacade;
    @Mock private SubprocessoCadastroWorkflowService cadastroWorkflowService;
    @Mock private SubprocessoMapaWorkflowService mapaWorkflowService;
    @Mock private SubprocessoContextoService contextoService;
    @Mock private SubprocessoMapaService mapaFacade;
    @Mock private SubprocessoPermissaoCalculator permissaoCalculator;

    @InjectMocks
    private SubprocessoFacade facade;

    @Test
    void deveDelegarOperacoesBasicas() {
        facade.buscarSubprocesso(1L);
        verify(subprocessoFacade).buscarSubprocesso(1L);

        facade.listar();
        verify(subprocessoFacade).listar();

        SubprocessoDto dto = new SubprocessoDto();
        facade.criar(dto);
        verify(subprocessoFacade).criar(dto);

        facade.atualizar(1L, dto);
        verify(subprocessoFacade).atualizar(1L, dto);

        facade.excluir(1L);
        verify(subprocessoFacade).excluir(1L);
    }

    @Test
    void deveDelegarWorkflowCadastro() {
        Usuario user = new Usuario();
        facade.disponibilizarCadastro(1L, user);
        verify(cadastroWorkflowService).disponibilizarCadastro(1L, user);

        facade.homologarCadastro(1L, "obs", user);
        verify(cadastroWorkflowService).homologarCadastro(1L, "obs", user);
        
        facade.aceitarCadastroEmBloco(Collections.singletonList(1L), 2L, user);
        verify(cadastroWorkflowService).aceitarCadastroEmBloco(Collections.singletonList(1L), 2L, user);
    }

    @Test
    void deveDelegarWorkflowMapa() {
        Usuario user = new Usuario();
        SalvarMapaRequest req = new SalvarMapaRequest();
        facade.salvarMapaSubprocesso(1L, req);
        verify(mapaWorkflowService).salvarMapaSubprocesso(1L, req);

        facade.homologarValidacao(1L, user);
        verify(mapaWorkflowService).homologarValidacao(1L, user);
    }

    @Test
    void deveDelegarOutrosServicos() {
        facade.obterContextoEdicao(1L, Perfil.ADMIN);
        verify(contextoService).obterContextoEdicao(1L, Perfil.ADMIN);

        facade.importarAtividades(1L, 2L);
        verify(mapaFacade).importarAtividades(1L, 2L);
        
        facade.buscarSubprocessoComMapa(1L);
        verify(subprocessoFacade).buscarSubprocessoComMapa(1L);
        
        facade.obterPorProcessoEUnidade(1L, 2L);
        verify(subprocessoFacade).obterPorProcessoEUnidade(1L, 2L);
        
        facade.obterDetalhes(1L, Perfil.ADMIN);
        verify(subprocessoFacade).obterDetalhes(1L, Perfil.ADMIN);
        
        facade.obterSituacao(1L);
        verify(subprocessoFacade).obterSituacao(1L);
        
        facade.listarAtividadesSubprocesso(1L);
        verify(subprocessoFacade).listarAtividadesSubprocesso(1L);
        
        facade.obterAtividadesSemConhecimento(1L);
        verify(subprocessoFacade).obterAtividadesSemConhecimento(1L);
        
        facade.obterEntidadePorCodigoMapa(1L);
        verify(subprocessoFacade).obterEntidadePorCodigoMapa(1L);
        
        facade.verificarAcessoUnidadeAoProcesso(1L, Collections.singletonList(2L));
        verify(subprocessoFacade).verificarAcessoUnidadeAoProcesso(1L, Collections.singletonList(2L));
        
        facade.obterSugestoes(1L);
        verify(subprocessoFacade).obterSugestoes(1L);
        
        facade.obterMapaParaAjuste(1L);
        verify(subprocessoFacade).obterMapaParaAjuste(1L);
        
        facade.obterPermissoes(1L);
        verify(subprocessoFacade).obterPermissoes(1L);
        
        facade.validarCadastro(1L);
        verify(subprocessoFacade).validarCadastro(1L);
        
        facade.validarExistenciaAtividades(1L);
        verify(subprocessoFacade).validarExistenciaAtividades(1L);
        
        facade.validarAssociacoesMapa(1L);
        verify(subprocessoFacade).validarAssociacoesMapa(1L);
        
        facade.atualizarSituacaoParaEmAndamento(1L);
        verify(subprocessoFacade).atualizarSituacaoParaEmAndamento(1L);
        
        facade.listarSubprocessosHomologados();
        verify(subprocessoFacade).listarSubprocessosHomologados();
        
        facade.reabrirCadastro(1L, "j");
        verify(subprocessoFacade).reabrirCadastro(1L, "j");
        
        facade.reabrirRevisaoCadastro(1L, "j");
        verify(subprocessoFacade).reabrirRevisaoCadastro(1L, "j");
        
        facade.alterarDataLimite(1L, java.time.LocalDate.now());
        verify(subprocessoFacade).alterarDataLimite(1L, java.time.LocalDate.now());
        
        facade.obterCadastro(1L);
        verify(subprocessoFacade).obterCadastro(1L);
        
        facade.salvarAjustesMapa(1L, Collections.emptyList(), "t");
        verify(mapaFacade).salvarAjustesMapa(1L, Collections.emptyList(), "t");
    }
}
