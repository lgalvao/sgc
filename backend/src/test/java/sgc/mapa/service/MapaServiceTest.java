package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.mapper.MapaCompletoMapper;
import sgc.mapa.model.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do MapaService")
class MapaServiceTest {

    @Mock
    private MapaRepo mapaRepo;
    @Mock
    private CompetenciaRepo competenciaRepo;
    @Mock
    private AtividadeRepo atividadeRepo;
    @Mock
    private MapaCompletoMapper mapaCompletoMapper;
    @Mock
    private MapaSalvamentoService mapaSalvamentoService;

    @InjectMocks
    private MapaService service;

    @Nested
    @DisplayName("Operações CRUD")
    class Crud {
        @Test
        @DisplayName("Deve listar todos os mapas")
        void deveListarMapas() {
            when(mapaRepo.findAll()).thenReturn(List.of(new Mapa()));
            var resultado = service.listar();
            assertThat(resultado).isNotNull().isNotEmpty().hasSize(1);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há mapas")
        void deveRetornarListaVaziaQuandoNaoHaMapas() {
            when(mapaRepo.findAll()).thenReturn(List.of());
            var resultado = service.listar();
            assertThat(resultado).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("Deve obter mapa por código")
        void deveObterPorCodigo() {
            when(mapaRepo.findById(1L)).thenReturn(Optional.of(new Mapa()));
            assertThat(service.obterPorCodigo(1L)).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar exceção se mapa não encontrado")
        void deveLancarExcecaoSeNaoEncontrado() {
            when(mapaRepo.findById(1L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> service.obterPorCodigo(1L))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve salvar mapa")
        void deveSalvarMapa() {
            Mapa mapa = new Mapa();
            when(mapaRepo.save(mapa)).thenReturn(mapa);
            assertThat(service.salvar(mapa)).isEqualTo(mapa);
        }

        @Test
        @DisplayName("Deve criar mapa")
        void deveCriarMapa() {
            Mapa mapa = new Mapa();
            when(mapaRepo.save(mapa)).thenReturn(mapa);
            assertThat(service.criar(mapa)).isEqualTo(mapa);
        }

        @Test
        @DisplayName("Deve atualizar mapa existente")
        void deveAtualizarMapa() {
            Mapa existente = new Mapa();
            Mapa novosDados = new Mapa();
            novosDados.setObservacoesDisponibilizacao("Obs");

            when(mapaRepo.findById(1L)).thenReturn(Optional.of(existente));
            when(mapaRepo.save(existente)).thenReturn(existente);

            Mapa atualizado = service.atualizar(1L, novosDados);
            assertThat(atualizado.getObservacoesDisponibilizacao()).isEqualTo("Obs");
        }

        @Test
        @DisplayName("Deve lançar erro ao atualizar mapa inexistente")
        void deveLancarErroAoAtualizarInexistente() {
            when(mapaRepo.findById(1L)).thenReturn(Optional.empty());
            Mapa mapa = new Mapa();
            assertThatThrownBy(() -> service.atualizar(1L, mapa))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve excluir mapa")
        void deveExcluirMapa() {
            when(mapaRepo.existsById(1L)).thenReturn(true);
            service.excluir(1L);
            verify(mapaRepo).deleteById(1L);
        }

        @Test
        @DisplayName("Deve lançar erro ao excluir mapa inexistente")
        void deveLancarErroAoExcluirInexistente() {
            when(mapaRepo.existsById(1L)).thenReturn(false);
            assertThatThrownBy(() -> service.excluir(1L))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }
    }

    @Nested
    @DisplayName("Consultas Específicas")
    class Consultas {
        @Test
        @DisplayName("Deve buscar mapa vigente por unidade")
        void deveBuscarMapaVigente() {
            when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(new Mapa()));
            var resultado = service.buscarMapaVigentePorUnidade(1L);
            assertThat(resultado).isPresent().get().isNotNull();
        }

        @Test
        @DisplayName("Deve retornar vazio quando não há mapa vigente para unidade")
        void deveRetornarVazioQuandoNaoHaMapaVigente() {
            when(mapaRepo.findMapaVigenteByUnidade(999L)).thenReturn(Optional.empty());
            var resultado = service.buscarMapaVigentePorUnidade(999L);
            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("Deve buscar por código do subprocesso")
        void deveBuscarPorSubprocesso() {
            when(mapaRepo.findBySubprocessoCodigo(1L)).thenReturn(Optional.of(new Mapa()));
            var resultado = service.buscarPorSubprocessoCodigo(1L);
            assertThat(resultado).isPresent().get().isNotNull();
        }

        @Test
        @DisplayName("Deve retornar vazio quando não há mapa para subprocesso")
        void deveRetornarVazioQuandoNaoHaMapaParaSubprocesso() {
            when(mapaRepo.findBySubprocessoCodigo(999L)).thenReturn(Optional.empty());
            var resultado = service.buscarPorSubprocessoCodigo(999L);
            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("Deve obter mapa completo DTO")
        void deveObterMapaCompleto() {
            when(mapaRepo.findById(1L)).thenReturn(Optional.of(new Mapa()));
            when(competenciaRepo.findByMapaCodigo(1L)).thenReturn(List.of(new Competencia()));
            when(mapaCompletoMapper.toDto(any(), any(), anyList())).thenReturn(new MapaCompletoDto());

            var resultado = service.obterMapaCompleto(1L, 10L);
            assertThat(resultado).isNotNull();
        }

        @Test
        @DisplayName("Deve obter mapa completo DTO sem competências")
        void deveObterMapaCompletoSemCompetencias() {
            when(mapaRepo.findById(1L)).thenReturn(Optional.of(new Mapa()));
            when(competenciaRepo.findByMapaCodigo(1L)).thenReturn(List.of());
            when(mapaCompletoMapper.toDto(any(), any(), anyList())).thenReturn(new MapaCompletoDto());

            var resultado = service.obterMapaCompleto(1L, 10L);
            assertThat(resultado).isNotNull();
            verify(competenciaRepo).findByMapaCodigo(1L);
        }
    }

    @Nested
    @DisplayName("Salvar Mapa Completo - Delegação")
    class SalvarCompleto {
        @Test
        @DisplayName("Deve delegar salvar mapa completo ao MapaSalvamentoService")
        void deveDelegarSalvarMapaCompleto() {
            Long mapaId = 1L;
            SalvarMapaRequest req = new SalvarMapaRequest();
            req.setObservacoes("Obs");
            MapaCompletoDto expectedDto = new MapaCompletoDto();

            when(mapaSalvamentoService.salvarMapaCompleto(mapaId, req, "user")).thenReturn(expectedDto);

            var resultado = service.salvarMapaCompleto(mapaId, req, "user");

            assertThat(resultado).isNotNull().isSameAs(expectedDto);
            verify(mapaSalvamentoService).salvarMapaCompleto(mapaId, req, "user");
        }

        @Test
        @DisplayName("Deve propagar erro do MapaSalvamentoService")
        void devePropagarErroDoMapaSalvamentoService() {
            Long mapaId = 1L;
            SalvarMapaRequest req = new SalvarMapaRequest();

            when(mapaSalvamentoService.salvarMapaCompleto(mapaId, req, "user"))
                .thenThrow(new ErroEntidadeNaoEncontrada("Mapa", mapaId));

            assertThatThrownBy(() -> service.salvarMapaCompleto(mapaId, req, "user"))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve propagar ErroValidacao do MapaSalvamentoService")
        void devePropagarErroValidacao() {
            Long mapaId = 1L;
            SalvarMapaRequest req = new SalvarMapaRequest();

            when(mapaSalvamentoService.salvarMapaCompleto(mapaId, req, "user"))
                .thenThrow(new ErroValidacao("Atividade 99 não pertence ao mapa"));

            assertThatThrownBy(() -> service.salvarMapaCompleto(mapaId, req, "user"))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining("não pertence ao mapa");
        }
    }
}
