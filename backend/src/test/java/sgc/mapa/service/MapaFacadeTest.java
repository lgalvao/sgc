package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.ImpactoMapaResponse;
import sgc.mapa.dto.MapaVisualizacaoResponse;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.model.Mapa;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.model.Subprocesso;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes do MapaFacade")
class MapaFacadeTest {
    @Mock
    private MapaManutencaoService mapaManutencaoService;
    @Mock
    private MapaSalvamentoService mapaSalvamentoService;
    @Mock
    private MapaVisualizacaoService mapaVisualizacaoService;
    @Mock
    private ImpactoMapaService impactoMapaService;

    @InjectMocks
    private MapaFacade facade;

    @Nested
    @DisplayName("Operações CRUD")
    class Crud {
        @Test
        @DisplayName("Deve listar todos os mapas")
        void deveListarMapas() {
            when(mapaManutencaoService.listarTodosMapas()).thenReturn(List.of(new Mapa()));
            var resultado = facade.listar();
            assertThat(resultado).isNotNull().isNotEmpty().hasSize(1);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há mapas")
        void deveRetornarListaVaziaQuandoNaoHaMapas() {
            when(mapaManutencaoService.listarTodosMapas()).thenReturn(List.of());
            var resultado = facade.listar();
            assertThat(resultado).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("Deve obter mapa por código")
        void deveObterPorCodigo() {
            when(mapaManutencaoService.buscarMapaPorCodigo(1L)).thenReturn(new Mapa());
            assertThat(facade.obterPorCodigo(1L)).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar exceção se mapa não encontrado")
        void deveLancarExcecaoSeNaoEncontrado() {
            when(mapaManutencaoService.buscarMapaPorCodigo(1L)).thenThrow(new ErroEntidadeNaoEncontrada("Mapa", 1L));
            assertThatThrownBy(() -> facade.obterPorCodigo(1L))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve salvar mapa")
        void deveSalvarMapa() {
            Mapa mapa = new Mapa();
            when(mapaManutencaoService.salvarMapa(mapa)).thenReturn(mapa);
            assertThat(facade.salvar(mapa)).isEqualTo(mapa);
        }


        @Test
        @DisplayName("Deve atualizar mapa existente")
        void deveAtualizarMapa() {
            Mapa existente = new Mapa();
            Mapa novosDados = new Mapa();
            novosDados.setObservacoesDisponibilizacao("Obs");

            when(mapaManutencaoService.buscarMapaPorCodigo(1L)).thenReturn(existente);
            when(mapaManutencaoService.salvarMapa(existente)).thenReturn(existente);

            Mapa atualizado = facade.atualizar(1L, novosDados);
            assertThat(atualizado.getObservacoesDisponibilizacao()).isEqualTo("Obs");
        }

        @Test
        @DisplayName("Deve lançar exceção ao atualizar mapa inexistente")
        void deveLancarExcecaoAoAtualizarMapaInexistente() {
            Mapa novosDados = new Mapa();
            when(mapaManutencaoService.buscarMapaPorCodigo(999L))
                    .thenThrow(new ErroEntidadeNaoEncontrada("Mapa", 999L));

            assertThatThrownBy(() -> facade.atualizar(999L, novosDados))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);

            verify(mapaManutencaoService).buscarMapaPorCodigo(999L);
        }

        @Test
        @DisplayName("Deve excluir mapa")
        void deveExcluirMapa() {
            facade.excluir(1L);
            verify(mapaManutencaoService).excluirMapa(1L);
        }
    }

    @Nested
    @DisplayName("Consultas Específicas")
    class Consultas {
        @Test
        @DisplayName("Deve buscar mapa vigente por unidade")
        void deveBuscarMapaVigente() {
            when(mapaManutencaoService.buscarMapaVigentePorUnidade(1L)).thenReturn(Optional.of(new Mapa()));
            var resultado = facade.buscarMapaVigentePorUnidade(1L);
            assertThat(resultado).isPresent().get().isNotNull();
        }

        @Test
        @DisplayName("Deve buscar por código do subprocesso")
        void deveBuscarPorSubprocesso() {
            when(mapaManutencaoService.buscarMapaPorSubprocessoCodigo(1L)).thenReturn(Optional.of(new Mapa()));
            var resultado = facade.buscarPorSubprocessoCodigo(1L);
            assertThat(resultado).isPresent().get().isNotNull();
        }
    }

    @Nested
    @DisplayName("Salvar Mapa Completo - Delegação")
    class SalvarCompleto {
        @Test
        @DisplayName("Deve delegar salvar mapa completo ao MapaSalvamentoService")
        void deveDelegarSalvarMapaCompleto() {
            Long mapaId = 1L;
            SalvarMapaRequest req = SalvarMapaRequest.builder()
                    .observacoes("Obs")
                    .build();
            Mapa expectedMapa = Mapa.builder().codigo(mapaId).build();

            when(mapaSalvamentoService.salvarMapaCompleto(mapaId, req)).thenReturn(expectedMapa);

            var resultado = facade.salvarMapaCompleto(mapaId, req);

            assertThat(resultado).isNotNull().isSameAs(expectedMapa);
            verify(mapaSalvamentoService).salvarMapaCompleto(mapaId, req);
        }
    }

    @Nested
    @DisplayName("Visualização e Impactos")
    class VisualizacaoEImpactos {
        @Test
        @DisplayName("Deve obter mapa para visualização")
        void deveObterMapaParaVisualizacao() {
            Subprocesso subprocesso = new Subprocesso();
            subprocesso.setCodigo(1L);
            MapaVisualizacaoResponse expectedResponse = MapaVisualizacaoResponse.builder().build();

            when(mapaVisualizacaoService.obterMapaParaVisualizacao(subprocesso)).thenReturn(expectedResponse);

            var resultado = facade.obterMapaParaVisualizacao(subprocesso);

            assertThat(resultado).isNotNull().isSameAs(expectedResponse);
            verify(mapaVisualizacaoService).obterMapaParaVisualizacao(subprocesso);
        }

        @Test
        @DisplayName("Deve verificar impactos de alteração no mapa")
        void deveVerificarImpactos() {
            Subprocesso subprocesso = new Subprocesso();
            subprocesso.setCodigo(1L);
            Usuario usuario = new Usuario();
            ImpactoMapaResponse expectedResponse = ImpactoMapaResponse.builder().build();

            when(impactoMapaService.verificarImpactos(subprocesso, usuario)).thenReturn(expectedResponse);

            var resultado = facade.verificarImpactos(subprocesso, usuario);

            assertThat(resultado).isNotNull().isSameAs(expectedResponse);
            verify(impactoMapaService).verificarImpactos(subprocesso, usuario);
        }
    }
}
