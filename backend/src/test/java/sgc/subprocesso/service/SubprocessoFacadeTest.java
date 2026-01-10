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

    @Mock private SubprocessoService subprocessoService;
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
        verify(subprocessoService).buscarSubprocesso(1L);

        facade.listar();
        verify(subprocessoService).listar();

        SubprocessoDto dto = new SubprocessoDto();
        facade.criar(dto);
        verify(subprocessoService).criar(dto);

        facade.atualizar(1L, dto);
        verify(subprocessoService).atualizar(1L, dto);

        facade.excluir(1L);
        verify(subprocessoService).excluir(1L);
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
        verify(subprocessoService).buscarSubprocessoComMapa(1L);
        
        facade.obterPorProcessoEUnidade(1L, 2L);
        verify(subprocessoService).obterPorProcessoEUnidade(1L, 2L);
        
        facade.obterDetalhes(1L, Perfil.ADMIN);
        verify(subprocessoService).obterDetalhes(1L, Perfil.ADMIN);
        
        facade.obterSituacao(1L);
        verify(subprocessoService).obterSituacao(1L);
        
        facade.listarAtividadesSubprocesso(1L);
        verify(subprocessoService).listarAtividadesSubprocesso(1L);
        
        facade.obterAtividadesSemConhecimento(1L);
        verify(subprocessoService).obterAtividadesSemConhecimento(1L);
        
        facade.obterEntidadePorCodigoMapa(1L);
        verify(subprocessoService).obterEntidadePorCodigoMapa(1L);
        
        facade.verificarAcessoUnidadeAoProcesso(1L, Collections.singletonList(2L));
        verify(subprocessoService).verificarAcessoUnidadeAoProcesso(1L, Collections.singletonList(2L));
        
        facade.obterSugestoes(1L);
        verify(subprocessoService).obterSugestoes(1L);
        
        facade.obterMapaParaAjuste(1L);
        verify(subprocessoService).obterMapaParaAjuste(1L);
        
        facade.obterPermissoes(1L);
        verify(subprocessoService).obterPermissoes(1L);
        
        facade.validarCadastro(1L);
        verify(subprocessoService).validarCadastro(1L);
        
        facade.validarExistenciaAtividades(1L);
        verify(subprocessoService).validarExistenciaAtividades(1L);
        
        facade.validarAssociacoesMapa(1L);
        verify(subprocessoService).validarAssociacoesMapa(1L);
        
        facade.atualizarSituacaoParaEmAndamento(1L);
        verify(subprocessoService).atualizarSituacaoParaEmAndamento(1L);
        
        facade.listarSubprocessosHomologados();
        verify(subprocessoService).listarSubprocessosHomologados();
        
        facade.reabrirCadastro(1L, "j");
        verify(subprocessoService).reabrirCadastro(1L, "j");
        
        facade.reabrirRevisaoCadastro(1L, "j");
        verify(subprocessoService).reabrirRevisaoCadastro(1L, "j");
        
        facade.alterarDataLimite(1L, java.time.LocalDate.now());
        verify(subprocessoService).alterarDataLimite(1L, java.time.LocalDate.now());
        
        facade.obterCadastro(1L);
        verify(subprocessoService).obterCadastro(1L);
        
        facade.salvarAjustesMapa(1L, Collections.emptyList(), "t");
        verify(mapaFacade).salvarAjustesMapa(1L, Collections.emptyList(), "t");
    }
}
