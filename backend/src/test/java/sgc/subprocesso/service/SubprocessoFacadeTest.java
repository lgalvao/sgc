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
    @Mock private SubprocessoMapaService mapaService;
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
        verify(mapaService).importarAtividades(1L, 2L);
    }
}
