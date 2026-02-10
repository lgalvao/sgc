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
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.mapper.MapaCompletoMapper;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Mapa;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes do MapaFacade")
class MapaFacadeTest {
    @Mock
    private MapaManutencaoService mapaManutencaoService;
    @Mock
    private MapaCompletoMapper mapaCompletoMapper;
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
        @DisplayName("Deve retornar vazio quando não há mapa vigente para unidade")
        void deveRetornarVazioQuandoNaoHaMapaVigente() {
            when(mapaManutencaoService.buscarMapaVigentePorUnidade(999L)).thenReturn(Optional.empty());
            var resultado = facade.buscarMapaVigentePorUnidade(999L);
            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("Deve buscar por código do subprocesso")
        void deveBuscarPorSubprocesso() {
            when(mapaManutencaoService.buscarMapaPorSubprocessoCodigo(1L)).thenReturn(Optional.of(new Mapa()));
            var resultado = facade.buscarPorSubprocessoCodigo(1L);
            assertThat(resultado).isPresent().get().isNotNull();
        }

        @Test
        @DisplayName("Deve retornar vazio quando não há mapa para subprocesso")
        void deveRetornarVazioQuandoNaoHaMapaParaSubprocesso() {
            when(mapaManutencaoService.buscarMapaPorSubprocessoCodigo(999L)).thenReturn(Optional.empty());
            var resultado = facade.buscarPorSubprocessoCodigo(999L);
            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("Deve obter mapa completo DTO")
        void deveObterMapaCompleto() {
            when(mapaManutencaoService.buscarMapaPorCodigo(1L)).thenReturn(new Mapa());
            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(1L)).thenReturn(List.of(new Competencia()));
            when(mapaCompletoMapper.toDto(any(), any(), anyList())).thenReturn(MapaCompletoDto.builder().build());

            var resultado = facade.obterMapaCompleto(1L, 10L);
            assertThat(resultado).isNotNull();
        }

        @Test
        @DisplayName("Deve obter mapa completo DTO sem competências")
        void deveObterMapaCompletoSemCompetencias() {
            when(mapaManutencaoService.buscarMapaPorCodigo(1L)).thenReturn(new Mapa());
            when(mapaManutencaoService.buscarCompetenciasPorCodMapa(1L)).thenReturn(List.of());
            when(mapaCompletoMapper.toDto(any(), any(), anyList())).thenReturn(MapaCompletoDto.builder().build());

            var resultado = facade.obterMapaCompleto(1L, 10L);
            assertThat(resultado).isNotNull();
            verify(mapaManutencaoService).buscarCompetenciasPorCodMapa(1L);
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
            MapaCompletoDto expectedDto = MapaCompletoDto.builder().build();

            when(mapaSalvamentoService.salvarMapaCompleto(mapaId, req)).thenReturn(expectedDto);

            var resultado = facade.salvarMapaCompleto(mapaId, req);

            assertThat(resultado).isNotNull().isSameAs(expectedDto);
            verify(mapaSalvamentoService).salvarMapaCompleto(mapaId, req);
        }
    }
}
