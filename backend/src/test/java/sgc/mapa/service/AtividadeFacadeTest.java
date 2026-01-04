package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.AtividadeDto;
import sgc.mapa.dto.ConhecimentoDto;
import sgc.mapa.dto.ResultadoOperacaoConhecimento;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Mapa;
import sgc.subprocesso.dto.AtividadeOperacaoResponse;
import sgc.subprocesso.dto.AtividadeVisualizacaoDto;
import sgc.subprocesso.dto.SubprocessoSituacaoDto;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para AtividadeFacade")
class AtividadeFacadeTest {

    @InjectMocks
    private AtividadeFacade facade;

    @Mock
    private AtividadeService atividadeService;

    @Mock
    private SubprocessoService subprocessoService;

    // SubprocessoCadastroController is not used in the Facade code provided, so removed mock.

    @Test
    @DisplayName("Deve criar atividade e retornar status")
    void deveCriarAtividade() {
        AtividadeDto request = new AtividadeDto();
        request.setMapaCodigo(1L);
        // Facade calls: criar -> (returns dto)
        AtividadeDto created = new AtividadeDto();
        created.setCodigo(100L);
        created.setMapaCodigo(1L);

        // Facade then gets Subprocesso code by Map code
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(10L);
        when(subprocessoService.obterEntidadePorCodigoMapa(1L)).thenReturn(subprocesso);

        when(atividadeService.criar(request, "user")).thenReturn(created);

        // Facade gets status
        SubprocessoSituacaoDto status = SubprocessoSituacaoDto.builder().situacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO).build();
        when(subprocessoService.obterStatus(10L)).thenReturn(status);

        // Facade searches for activity in list to return visualization
        AtividadeVisualizacaoDto vis = new AtividadeVisualizacaoDto();
        vis.setCodigo(100L);
        when(subprocessoService.listarAtividadesSubprocesso(10L)).thenReturn(java.util.List.of(vis));

        AtividadeOperacaoResponse response = facade.criarAtividade(request, "user");

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

        // Facade gets entity to find map code -> then subprocesso code
        when(atividadeService.obterEntidadePorCodigo(codigo)).thenReturn(atividadeEntity);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(10L);
        when(subprocessoService.obterEntidadePorCodigoMapa(1L)).thenReturn(subprocesso);

        SubprocessoSituacaoDto status = SubprocessoSituacaoDto.builder().build();
        when(subprocessoService.obterStatus(10L)).thenReturn(status);

        AtividadeOperacaoResponse response = facade.atualizarAtividade(codigo, request);

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

        when(atividadeService.obterEntidadePorCodigo(codigo)).thenReturn(atividadeEntity);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(10L);
        when(subprocessoService.obterEntidadePorCodigoMapa(1L)).thenReturn(subprocesso);

        SubprocessoSituacaoDto status = SubprocessoSituacaoDto.builder().build();
        when(subprocessoService.obterStatus(10L)).thenReturn(status);

        AtividadeOperacaoResponse response = facade.excluirAtividade(codigo);

        verify(atividadeService).excluir(codigo);
        assertThat(response.getAtividade()).isNull();
        assertThat(response.getSubprocesso()).isNotNull();
    }

    @Test
    @DisplayName("Deve propagar erro se atividade não encontrada na exclusão")
    void devePropagarErroExclusao() {
        when(atividadeService.obterEntidadePorCodigo(1L)).thenThrow(new ErroEntidadeNaoEncontrada("Atividade", 1L));

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

        when(atividadeService.criarConhecimento(codigoAtividade, dto)).thenReturn(salvo);

        // Mocks for creating response
        when(atividadeService.obterEntidadePorCodigo(codigoAtividade)).thenReturn(atividadeEntity);
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(10L);
        when(subprocessoService.obterEntidadePorCodigoMapa(1L)).thenReturn(subprocesso);

        SubprocessoSituacaoDto status = SubprocessoSituacaoDto.builder().build();
        when(subprocessoService.obterStatus(10L)).thenReturn(status);

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

        // Mocks for creating response
        when(atividadeService.obterEntidadePorCodigo(codigoAtividade)).thenReturn(atividadeEntity);
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(10L);
        when(subprocessoService.obterEntidadePorCodigoMapa(1L)).thenReturn(subprocesso);

        SubprocessoSituacaoDto status = SubprocessoSituacaoDto.builder().build();
        when(subprocessoService.obterStatus(10L)).thenReturn(status);

        AtividadeVisualizacaoDto vis = new AtividadeVisualizacaoDto();
        vis.setCodigo(codigoAtividade);
        when(subprocessoService.listarAtividadesSubprocesso(10L)).thenReturn(java.util.List.of(vis));

        AtividadeOperacaoResponse response = facade.atualizarConhecimento(codigoAtividade, codigoConhecimento, dto);

        verify(atividadeService).atualizarConhecimento(codigoAtividade, codigoConhecimento, dto);
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

        // Mocks for creating response
        when(atividadeService.obterEntidadePorCodigo(codigoAtividade)).thenReturn(atividadeEntity);
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(10L);
        when(subprocessoService.obterEntidadePorCodigoMapa(1L)).thenReturn(subprocesso);

        SubprocessoSituacaoDto status = SubprocessoSituacaoDto.builder().build();
        when(subprocessoService.obterStatus(10L)).thenReturn(status);

        AtividadeVisualizacaoDto vis = new AtividadeVisualizacaoDto();
        vis.setCodigo(codigoAtividade);
        when(subprocessoService.listarAtividadesSubprocesso(10L)).thenReturn(java.util.List.of(vis));

        AtividadeOperacaoResponse response = facade.excluirConhecimento(codigoAtividade, codigoConhecimento);

        verify(atividadeService).excluirConhecimento(codigoAtividade, codigoConhecimento);
        assertThat(response.getAtividade()).isNotNull();
    }
}
