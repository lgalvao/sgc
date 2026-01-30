package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import sgc.mapa.dto.*;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Mapa;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Usuario;
import sgc.seguranca.acesso.AccessControlService;
import sgc.seguranca.acesso.Acao;
import sgc.subprocesso.dto.AtividadeOperacaoResponse;
import sgc.subprocesso.dto.AtividadeVisualizacaoDto;
import sgc.subprocesso.dto.SubprocessoSituacaoDto;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoFacade;

import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import sgc.subprocesso.model.SituacaoSubprocesso;

@ExtendWith(MockitoExtension.class)
class AtividadeFacadeTest {

    @Mock private MapaManutencaoService mapaManutencaoService;
    @Mock private SubprocessoFacade subprocessoFacade;
    @Mock private AccessControlService accessControlService;
    @Mock private UsuarioFacade usuarioService;
    @Mock private MapaFacade mapaFacade;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AtividadeFacade atividadeFacade;

    @Test
    @DisplayName("Deve criar atividade com sucesso")
    void deveCriarAtividade() {
        Long mapaCodigo = 100L;
        Long subCodigo = 200L;
        Long atividadeCodigo = 300L;
        CriarAtividadeRequest request = new CriarAtividadeRequest(mapaCodigo, "Desc");
        
        Usuario usuario = new Usuario();
        Mapa mapa = new Mapa();
        mapa.setCodigo(mapaCodigo);
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(subCodigo);
        mapa.setSubprocesso(subprocesso);
        
        AtividadeResponse responseSalvo = new AtividadeResponse(atividadeCodigo, mapaCodigo, "Desc");
        
        when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);
        when(mapaFacade.obterPorCodigo(mapaCodigo)).thenReturn(mapa);
        when(mapaManutencaoService.criarAtividade(request)).thenReturn(responseSalvo);
        when(subprocessoFacade.obterEntidadePorCodigoMapa(mapaCodigo)).thenReturn(subprocesso);
        when(subprocessoFacade.obterSituacao(subCodigo)).thenReturn(new SubprocessoSituacaoDto(subCodigo, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, "Cadastro em Andamento"));
        when(subprocessoFacade.listarAtividadesSubprocesso(subCodigo)).thenReturn(Collections.singletonList(
                new AtividadeVisualizacaoDto(atividadeCodigo, "Desc", null)
        ));

        AtividadeOperacaoResponse result = atividadeFacade.criarAtividade(request);

        assertNotNull(result);
        verify(accessControlService).verificarPermissao(eq(usuario), eq(Acao.CRIAR_ATIVIDADE), any(Atividade.class));
    }

    @Test
    @DisplayName("Deve atualizar atividade com sucesso")
    void deveAtualizarAtividade() {
        Long atividadeCodigo = 300L;
        Long mapaCodigo = 100L;
        Long subCodigo = 200L;
        
        AtualizarAtividadeRequest request = new AtualizarAtividadeRequest("Nova Desc");
        
        Usuario usuario = new Usuario();
        Mapa mapa = new Mapa();
        mapa.setCodigo(mapaCodigo);
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(subCodigo);
        mapa.setSubprocesso(subprocesso);
        
        Atividade atividade = new Atividade();
        atividade.setCodigo(atividadeCodigo);
        atividade.setMapa(mapa);
        atividade.setDescricao("Antiga Desc");

        when(mapaManutencaoService.obterAtividadePorCodigo(atividadeCodigo)).thenReturn(atividade);
        when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);
        when(subprocessoFacade.obterEntidadePorCodigoMapa(mapaCodigo)).thenReturn(subprocesso);
        when(subprocessoFacade.obterSituacao(subCodigo)).thenReturn(new SubprocessoSituacaoDto(subCodigo, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, "Cadastro em Andamento"));

        AtividadeOperacaoResponse result = atividadeFacade.atualizarAtividade(atividadeCodigo, request);

        assertNotNull(result);
        verify(accessControlService).verificarPermissao(usuario, Acao.EDITAR_ATIVIDADE, atividade);
        verify(mapaManutencaoService).atualizarAtividade(atividadeCodigo, request);
    }

    @Test
    @DisplayName("Deve excluir atividade com sucesso")
    void deveExcluirAtividade() {
        Long atividadeCodigo = 300L;
        Long mapaCodigo = 100L;
        Long subCodigo = 200L;
        
        Usuario usuario = new Usuario();
        Mapa mapa = new Mapa();
        mapa.setCodigo(mapaCodigo);
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(subCodigo);
        mapa.setSubprocesso(subprocesso);
        
        Atividade atividade = new Atividade();
        atividade.setCodigo(atividadeCodigo);
        atividade.setMapa(mapa);
        
        when(mapaManutencaoService.obterAtividadePorCodigo(atividadeCodigo)).thenReturn(atividade);
        when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);
        when(subprocessoFacade.obterEntidadePorCodigoMapa(mapaCodigo)).thenReturn(subprocesso);
        when(subprocessoFacade.obterSituacao(subCodigo)).thenReturn(new SubprocessoSituacaoDto(subCodigo, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, "Cadastro em Andamento"));

        AtividadeOperacaoResponse result = atividadeFacade.excluirAtividade(atividadeCodigo);

        assertNotNull(result);
        verify(accessControlService).verificarPermissao(usuario, Acao.EXCLUIR_ATIVIDADE, atividade);
        verify(mapaManutencaoService).excluirAtividade(atividadeCodigo);
    }

    @Test
    @DisplayName("Deve criar conhecimento com sucesso")
    void deveCriarConhecimento() {
        Long atividadeCodigo = 300L;
        Long mapaCodigo = 100L;
        Long subCodigo = 200L;
        CriarConhecimentoRequest request = new CriarConhecimentoRequest(atividadeCodigo, "Conhecimento");
        
        Usuario usuario = new Usuario();
        Mapa mapa = new Mapa();
        mapa.setCodigo(mapaCodigo);
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(subCodigo);
        mapa.setSubprocesso(subprocesso);
        
        Atividade atividade = new Atividade();
        atividade.setCodigo(atividadeCodigo);
        atividade.setMapa(mapa);
        
        ConhecimentoResponse conhecimentoSalvo = new ConhecimentoResponse(500L, atividadeCodigo, "Conhecimento");
        
        when(mapaManutencaoService.obterAtividadePorCodigo(atividadeCodigo)).thenReturn(atividade);
        when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);
        when(mapaManutencaoService.criarConhecimento(atividadeCodigo, request)).thenReturn(conhecimentoSalvo);
        
        // Mocking for response creation
        when(subprocessoFacade.obterEntidadePorCodigoMapa(mapaCodigo)).thenReturn(subprocesso);
        when(subprocessoFacade.obterSituacao(subCodigo)).thenReturn(new SubprocessoSituacaoDto(subCodigo, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, "Cadastro em Andamento"));
        when(subprocessoFacade.listarAtividadesSubprocesso(subCodigo)).thenReturn(Collections.singletonList(
                new AtividadeVisualizacaoDto(atividadeCodigo, "Desc", null)
        ));

        ResultadoOperacaoConhecimento result = atividadeFacade.criarConhecimento(atividadeCodigo, request);

        assertNotNull(result);
        assertEquals(500L, result.novoConhecimentoId());
        verify(accessControlService).verificarPermissao(usuario, Acao.ASSOCIAR_CONHECIMENTOS, atividade);
    }
}
