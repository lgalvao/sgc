package sgc.mapa.service;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.AtividadeResponse;
import sgc.mapa.dto.AtualizarAtividadeRequest;
import sgc.mapa.dto.AtualizarConhecimentoRequest;
import sgc.mapa.dto.ConhecimentoResponse;
import sgc.mapa.dto.CriarAtividadeRequest;
import sgc.mapa.dto.CriarConhecimentoRequest;
import sgc.mapa.dto.ResultadoOperacaoConhecimento;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Mapa;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Usuario;
import sgc.seguranca.acesso.AccessControlService;
import sgc.subprocesso.dto.AtividadeOperacaoResponse;
import sgc.subprocesso.dto.AtividadeVisualizacaoDto;
import sgc.subprocesso.dto.SubprocessoSituacaoDto;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoFacade;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes para AtividadeFacade")
class AtividadeFacadeTest {

    @InjectMocks
    private AtividadeFacade facade;

    @Mock
    private AtividadeService atividadeService;

    @Mock
    private ConhecimentoService conhecimentoService;

    @Mock
    private SubprocessoFacade subprocessoFacade;

    @Mock
    private AccessControlService accessControlService;

    @Mock
    private UsuarioFacade usuarioService;

    @Mock
    private MapaFacade mapaFacade;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    @DisplayName("Deve criar atividade e retornar status")
    void deveCriarAtividade() {
        CriarAtividadeRequest request = CriarAtividadeRequest.builder()
                .mapaCodigo(1L)
                .descricao("Teste")
                .build();
        AtividadeResponse created = AtividadeResponse.builder()
                .codigo(100L)
                .mapaCodigo(1L)
                .build();

        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("user");
        when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(10L);

        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);
        mapa.setSubprocesso(subprocesso);
        when(mapaFacade.obterPorCodigo(1L)).thenReturn(mapa);

        doNothing().when(accessControlService).verificarPermissao(eq(usuario), any(), any());
        when(subprocessoFacade.obterEntidadePorCodigoMapa(1L)).thenReturn(subprocesso);
        when(atividadeService.criar(request)).thenReturn(created);

        SubprocessoSituacaoDto status = SubprocessoSituacaoDto.builder().situacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO).build();
        when(subprocessoFacade.obterSituacao(10L)).thenReturn(status);

        AtividadeVisualizacaoDto vis = AtividadeVisualizacaoDto.builder()
                .codigo(100L)
                .build();
        when(subprocessoFacade.listarAtividadesSubprocesso(10L)).thenReturn(java.util.List.of(vis));

        AtividadeOperacaoResponse response = facade.criarAtividade(request);

        verify(accessControlService).verificarPermissao(eq(usuario), any(), any());
        assertThat(response.atividade().codigo()).isEqualTo(100L);
        assertThat(response.subprocesso()).isNotNull();
        assertThat(response.subprocesso().situacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
    }

    @Test
    @DisplayName("Deve atualizar atividade e retornar status")
    void deveAtualizarAtividade() {
        Long codigo = 100L;
        AtualizarAtividadeRequest request = AtualizarAtividadeRequest.builder()
                .descricao("Nova descrição")
                .build();

        Atividade atividadeEntity = new Atividade();
        atividadeEntity.setCodigo(codigo);
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(10L);
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);
        mapa.setSubprocesso(subprocesso);
        atividadeEntity.setMapa(mapa);

        Usuario usuario = new Usuario();
        when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);
        doNothing().when(accessControlService).verificarPermissao(eq(usuario), any(), any());
        when(atividadeService.obterPorCodigo(codigo)).thenReturn(atividadeEntity);
        when(subprocessoFacade.obterEntidadePorCodigoMapa(1L)).thenReturn(subprocesso);

        SubprocessoSituacaoDto status = SubprocessoSituacaoDto.builder().build();
        when(subprocessoFacade.obterSituacao(10L)).thenReturn(status);

        AtividadeOperacaoResponse response = facade.atualizarAtividade(codigo, request);

        verify(atividadeService).atualizar(codigo, request);
        assertThat(response.subprocesso()).isNotNull();
    }

    @Test
    @DisplayName("Deve excluir atividade e retornar status")
    void deveExcluirAtividade() {
        Long codigo = 100L;

        Atividade atividadeEntity = new Atividade();
        atividadeEntity.setCodigo(codigo);
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(10L);
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);
        mapa.setSubprocesso(subprocesso);
        atividadeEntity.setMapa(mapa);

        Usuario usuario = new Usuario();
        when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);
        doNothing().when(accessControlService).verificarPermissao(eq(usuario), any(), any());
        when(atividadeService.obterPorCodigo(codigo)).thenReturn(atividadeEntity);
        when(subprocessoFacade.obterEntidadePorCodigoMapa(1L)).thenReturn(subprocesso);

        SubprocessoSituacaoDto status = SubprocessoSituacaoDto.builder().build();
        when(subprocessoFacade.obterSituacao(10L)).thenReturn(status);

        AtividadeOperacaoResponse response = facade.excluirAtividade(codigo);

        verify(atividadeService).excluir(codigo);
        assertThat(response.atividade()).isNull();
        assertThat(response.subprocesso()).isNotNull();
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
        CriarConhecimentoRequest request = CriarConhecimentoRequest.builder()
                .atividadeCodigo(codigoAtividade)
                .descricao("Teste")
                .build();
        ConhecimentoResponse salvo = ConhecimentoResponse.builder()
                .codigo(200L)
                .build();

        Atividade atividadeEntity = new Atividade();
        atividadeEntity.setCodigo(codigoAtividade);
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(10L);
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);
        mapa.setSubprocesso(subprocesso);
        atividadeEntity.setMapa(mapa);

        Usuario usuario = new Usuario();
        when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);
        doNothing().when(accessControlService).verificarPermissao(eq(usuario), any(), any());
        when(conhecimentoService.criar(codigoAtividade, request)).thenReturn(salvo);
        when(atividadeService.obterPorCodigo(codigoAtividade)).thenReturn(atividadeEntity);
        when(subprocessoFacade.obterEntidadePorCodigoMapa(1L)).thenReturn(subprocesso);

        SubprocessoSituacaoDto status = SubprocessoSituacaoDto.builder().build();
        when(subprocessoFacade.obterSituacao(10L)).thenReturn(status);

        AtividadeVisualizacaoDto vis = AtividadeVisualizacaoDto.builder()
                .codigo(codigoAtividade)
                .build();
        when(subprocessoFacade.listarAtividadesSubprocesso(10L)).thenReturn(java.util.List.of(vis));

        ResultadoOperacaoConhecimento resultado = facade.criarConhecimento(codigoAtividade, request);

        assertThat(resultado.novoConhecimentoId()).isEqualTo(200L);
        assertThat(resultado.response().atividade()).isNotNull();
        assertThat(resultado.response().atividade().codigo()).isEqualTo(codigoAtividade);
    }

    @Test
    @DisplayName("Deve atualizar conhecimento e retornar status")
    void deveAtualizarConhecimento() {
        Long codigoAtividade = 100L;
        Long codigoConhecimento = 200L;
        AtualizarConhecimentoRequest request = AtualizarConhecimentoRequest.builder()
                .descricao("Nova descrição")
                .build();

        Atividade atividadeEntity = new Atividade();
        atividadeEntity.setCodigo(codigoAtividade);
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(10L);
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);
        mapa.setSubprocesso(subprocesso);
        atividadeEntity.setMapa(mapa);

        Usuario usuario = new Usuario();
        when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);
        doNothing().when(accessControlService).verificarPermissao(eq(usuario), any(), any());
        when(atividadeService.obterPorCodigo(codigoAtividade)).thenReturn(atividadeEntity);
        when(subprocessoFacade.obterEntidadePorCodigoMapa(1L)).thenReturn(subprocesso);

        SubprocessoSituacaoDto status = SubprocessoSituacaoDto.builder().build();
        when(subprocessoFacade.obterSituacao(10L)).thenReturn(status);

        AtividadeVisualizacaoDto vis = AtividadeVisualizacaoDto.builder()
                .codigo(codigoAtividade)
                .build();
        when(subprocessoFacade.listarAtividadesSubprocesso(10L)).thenReturn(java.util.List.of(vis));

        AtividadeOperacaoResponse response = facade.atualizarConhecimento(codigoAtividade, codigoConhecimento, request);

        verify(conhecimentoService).atualizar(codigoAtividade, codigoConhecimento, request);
        assertThat(response.atividade()).isNotNull();
        assertThat(response.atividade().codigo()).isEqualTo(codigoAtividade);
    }

    @Test
    @DisplayName("Deve excluir conhecimento e retornar status")
    void deveExcluirConhecimento() {
        Long codigoAtividade = 100L;
        Long codigoConhecimento = 200L;

        Atividade atividadeEntity = new Atividade();
        atividadeEntity.setCodigo(codigoAtividade);
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(10L);
        Mapa mapa = new Mapa();
        mapa.setCodigo(1L);
        mapa.setSubprocesso(subprocesso);
        atividadeEntity.setMapa(mapa);

        Usuario usuario = new Usuario();
        when(usuarioService.obterUsuarioAutenticado()).thenReturn(usuario);
        doNothing().when(accessControlService).verificarPermissao(eq(usuario), any(), any());
        when(atividadeService.obterPorCodigo(codigoAtividade)).thenReturn(atividadeEntity);
        when(subprocessoFacade.obterEntidadePorCodigoMapa(1L)).thenReturn(subprocesso);

        SubprocessoSituacaoDto status = SubprocessoSituacaoDto.builder().build();
        when(subprocessoFacade.obterSituacao(10L)).thenReturn(status);

        AtividadeVisualizacaoDto vis = AtividadeVisualizacaoDto.builder()
                .codigo(codigoAtividade)
                .build();
        when(subprocessoFacade.listarAtividadesSubprocesso(10L)).thenReturn(java.util.List.of(vis));

        AtividadeOperacaoResponse response = facade.excluirConhecimento(codigoAtividade, codigoConhecimento);

        verify(conhecimentoService).excluir(codigoAtividade, codigoConhecimento);
        assertThat(response.atividade()).isNotNull();
    }

    @Test
    @DisplayName("Deve obter atividade por ID delegando ao service")
    void deveObterAtividadePorId() {
        AtividadeResponse expected = AtividadeResponse.builder().codigo(1L).build();
        when(atividadeService.obterResponse(1L)).thenReturn(expected);

        AtividadeResponse actual = facade.obterAtividadePorId(1L);

        assertThat(actual).isSameAs(expected);
    }

    @Test
    @DisplayName("Deve listar conhecimentos por atividade delegando ao service")
    void deveListarConhecimentos() {
        java.util.List<ConhecimentoResponse> expected = java.util.List.of();
        when(conhecimentoService.listarPorAtividade(100L)).thenReturn(expected);

        java.util.List<ConhecimentoResponse> actual = facade.listarConhecimentosPorAtividade(100L);

        assertThat(actual).isSameAs(expected);
    }
}
