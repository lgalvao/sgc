package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.AtividadeDto;
import sgc.mapa.dto.ConhecimentoDto;
import sgc.mapa.dto.ResultadoOperacaoConhecimento;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Mapa;
import sgc.organizacao.UsuarioService;
import sgc.organizacao.model.Usuario;
import sgc.seguranca.acesso.AccessControlService;
import sgc.subprocesso.dto.AtividadeOperacaoResp;
import sgc.subprocesso.dto.AtividadeVisualizacaoDto;
import sgc.subprocesso.dto.SubprocessoSituacaoDto;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para AtividadeFacade")
class AtividadeFacadeTest {

    @InjectMocks
    private AtividadeFacade facade;

    @Mock
    private AtividadeService atividadeService;

    @Mock
    private ConhecimentoService conhecimentoService;

    @Mock
    private SubprocessoService subprocessoService;

    @Mock
    private AccessControlService accessControlService;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private MapaFacade mapaFacade;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    @DisplayName("Deve criar atividade e retornar status")
    void deveCriarAtividade() {
        AtividadeDto request = new AtividadeDto();
        request.setMapaCodigo(1L);
        // Facade calls: criar -> (returns dto)
        AtividadeDto created = new AtividadeDto();
        created.setCodigo(100L);
        created.setMapaCodigo(1L);

        // Mock usuario autenticado
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("user");
        when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);
        
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);
        when(mapaFacade.obterPorCodigo(1L)).thenReturn(mapa);
        
        // Mock accessControlService to allow access
        doNothing().when(accessControlService).verificarPermissao(eq(usuario), any(), any());

        // Facade then gets Subprocesso code by Map code
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(10L);
        when(subprocessoService.obterEntidadePorCodigoMapa(1L)).thenReturn(subprocesso);

        when(atividadeService.criar(request)).thenReturn(created);

        // Facade gets status
        SubprocessoSituacaoDto status = SubprocessoSituacaoDto.builder().situacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO).build();
        when(subprocessoService.obterSituacao(10L)).thenReturn(status);

        // Facade searches for activity in list to return visualization
        AtividadeVisualizacaoDto vis = new AtividadeVisualizacaoDto();
        vis.setCodigo(100L);
        when(subprocessoService.listarAtividadesSubprocesso(10L)).thenReturn(java.util.List.of(vis));

        AtividadeOperacaoResp response = facade.criarAtividade(request);

        verify(accessControlService).verificarPermissao(eq(usuario), any(), any());
        assertThat(response.getAtividade().getCodigo()).isEqualTo(100L);
        assertThat(response.getSubprocesso()).isNotNull();
        assertThat(response.getSubprocesso().getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
    }

    @Test
    @DisplayName("Deve atualizar atividade e retornar status")
    void deveAtualizarAtividade() {
        Long codigo = 100L;
        AtividadeDto request = new AtividadeDto();

        Atividade atividadeEntity = new Atividade();
        atividadeEntity.setCodigo(codigo);
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);
        atividadeEntity.setMapa(mapa);

        // Mock usuario autenticado
        Usuario usuario = new Usuario();
        when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);
        doNothing().when(accessControlService).verificarPermissao(eq(usuario), any(), any());

        // Facade gets entity to find map code -> then subprocesso code
        when(atividadeService.obterPorCodigo(codigo)).thenReturn(atividadeEntity);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(10L);
        when(subprocessoService.obterEntidadePorCodigoMapa(1L)).thenReturn(subprocesso);

        SubprocessoSituacaoDto status = SubprocessoSituacaoDto.builder().build();
        when(subprocessoService.obterSituacao(10L)).thenReturn(status);

        AtividadeOperacaoResp response = facade.atualizarAtividade(codigo, request);

        verify(atividadeService).atualizar(codigo, request);
        assertThat(response.getSubprocesso()).isNotNull();
    }

    @Test
    @DisplayName("Deve excluir atividade e retornar status")
    void deveExcluirAtividade() {
        Long codigo = 100L;

        Atividade atividadeEntity = new Atividade();
        atividadeEntity.setCodigo(codigo);
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);
        atividadeEntity.setMapa(mapa);

        // Mock usuario autenticado
        Usuario usuario = new Usuario();
        when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);
        doNothing().when(accessControlService).verificarPermissao(eq(usuario), any(), any());

        when(atividadeService.obterPorCodigo(codigo)).thenReturn(atividadeEntity);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(10L);
        when(subprocessoService.obterEntidadePorCodigoMapa(1L)).thenReturn(subprocesso);

        SubprocessoSituacaoDto status = SubprocessoSituacaoDto.builder().build();
        when(subprocessoService.obterSituacao(10L)).thenReturn(status);

        AtividadeOperacaoResp response = facade.excluirAtividade(codigo);

        verify(atividadeService).excluir(codigo);
        assertThat(response.getAtividade()).isNull();
        assertThat(response.getSubprocesso()).isNotNull();
    }

    @Test
    @DisplayName("Deve propagar erro se atividade não encontrada na exclusão")
    void devePropagarErroExclusao() {
        when(atividadeService.obterPorCodigo(1L)).thenThrow(new ErroEntidadeNaoEncontrada("Atividade", 1L));

        assertThatThrownBy(() -> facade.excluirAtividade(1L))
            .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("Deve criar conhecimento e retornar status")
    void deveCriarConhecimento() {
        Long codigoAtividade = 100L;
        ConhecimentoDto dto = new ConhecimentoDto();
        ConhecimentoDto salvo = new ConhecimentoDto();
        salvo.setCodigo(200L);

        Atividade atividadeEntity = new Atividade();
        atividadeEntity.setCodigo(codigoAtividade);
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);
        atividadeEntity.setMapa(mapa);

        // Mock usuario autenticado
        Usuario usuario = new Usuario();
        when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);
        doNothing().when(accessControlService).verificarPermissao(eq(usuario), any(), any());

        when(conhecimentoService.criar(codigoAtividade, dto)).thenReturn(salvo);

        // Mocks for creating response
        when(atividadeService.obterPorCodigo(codigoAtividade)).thenReturn(atividadeEntity);
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(10L);
        when(subprocessoService.obterEntidadePorCodigoMapa(1L)).thenReturn(subprocesso);

        SubprocessoSituacaoDto status = SubprocessoSituacaoDto.builder().build();
        when(subprocessoService.obterSituacao(10L)).thenReturn(status);

        AtividadeVisualizacaoDto vis = new AtividadeVisualizacaoDto();
        vis.setCodigo(codigoAtividade);
        when(subprocessoService.listarAtividadesSubprocesso(10L)).thenReturn(java.util.List.of(vis));

        ResultadoOperacaoConhecimento resultado = facade.criarConhecimento(codigoAtividade, dto);

        assertThat(resultado.getNovoConhecimentoId()).isEqualTo(200L);
        assertThat(resultado.getResponse().getAtividade()).isNotNull();
        assertThat(resultado.getResponse().getAtividade().getCodigo()).isEqualTo(codigoAtividade);
    }

    @Test
    @DisplayName("Deve atualizar conhecimento e retornar status")
    void deveAtualizarConhecimento() {
        Long codigoAtividade = 100L;
        Long codigoConhecimento = 200L;
        ConhecimentoDto dto = new ConhecimentoDto();

        Atividade atividadeEntity = new Atividade();
        atividadeEntity.setCodigo(codigoAtividade);
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);
        atividadeEntity.setMapa(mapa);

        // Mock usuario autenticado
        Usuario usuario = new Usuario();
        when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);
        doNothing().when(accessControlService).verificarPermissao(eq(usuario), any(), any());

        // Mocks for creating response
        when(atividadeService.obterPorCodigo(codigoAtividade)).thenReturn(atividadeEntity);
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(10L);
        when(subprocessoService.obterEntidadePorCodigoMapa(1L)).thenReturn(subprocesso);

        SubprocessoSituacaoDto status = SubprocessoSituacaoDto.builder().build();
        when(subprocessoService.obterSituacao(10L)).thenReturn(status);

        AtividadeVisualizacaoDto vis = new AtividadeVisualizacaoDto();
        vis.setCodigo(codigoAtividade);
        when(subprocessoService.listarAtividadesSubprocesso(10L)).thenReturn(java.util.List.of(vis));

        AtividadeOperacaoResp response = facade.atualizarConhecimento(codigoAtividade, codigoConhecimento, dto);

        verify(conhecimentoService).atualizar(codigoAtividade, codigoConhecimento, dto);
        assertThat(response.getAtividade()).isNotNull();
        assertThat(response.getAtividade().getCodigo()).isEqualTo(codigoAtividade);
    }

    @Test
    @DisplayName("Deve excluir conhecimento e retornar status")
    void deveExcluirConhecimento() {
        Long codigoAtividade = 100L;
        Long codigoConhecimento = 200L;

        Atividade atividadeEntity = new Atividade();
        atividadeEntity.setCodigo(codigoAtividade);
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);
        atividadeEntity.setMapa(mapa);

        // Mock usuario autenticado
        Usuario usuario = new Usuario();
        when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);
        doNothing().when(accessControlService).verificarPermissao(eq(usuario), any(), any());

        // Mocks for creating response
        when(atividadeService.obterPorCodigo(codigoAtividade)).thenReturn(atividadeEntity);
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(10L);
        when(subprocessoService.obterEntidadePorCodigoMapa(1L)).thenReturn(subprocesso);

        SubprocessoSituacaoDto status = SubprocessoSituacaoDto.builder().build();
        when(subprocessoService.obterSituacao(10L)).thenReturn(status);

        AtividadeVisualizacaoDto vis = new AtividadeVisualizacaoDto();
        vis.setCodigo(codigoAtividade);
        when(subprocessoService.listarAtividadesSubprocesso(10L)).thenReturn(java.util.List.of(vis));

        AtividadeOperacaoResp response = facade.excluirConhecimento(codigoAtividade, codigoConhecimento);

        verify(conhecimentoService).excluir(codigoAtividade, codigoConhecimento);
        assertThat(response.getAtividade()).isNotNull();
    }
}
