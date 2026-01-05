package sgc.mapa.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.dto.CompetenciaMapaDto;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.mapper.MapaCompletoMapper;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.AtividadeRepo;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;

import java.util.*;

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
            assertThat(resultado).isNotNull();
            assertThat(resultado).isNotEmpty();
            assertThat(resultado).hasSize(1);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há mapas")
        void deveRetornarListaVaziaQuandoNaoHaMapas() {
            when(mapaRepo.findAll()).thenReturn(List.of());
            var resultado = service.listar();
            assertThat(resultado).isNotNull();
            assertThat(resultado).isEmpty();
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
            assertThatThrownBy(() -> service.atualizar(1L, new Mapa()))
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
            assertThat(resultado).isPresent();
            assertThat(resultado.get()).isNotNull();
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
            assertThat(resultado).isPresent();
            assertThat(resultado.get()).isNotNull();
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
    @DisplayName("Salvar Mapa Completo")
    class SalvarCompleto {
        @Test
        @DisplayName("Deve salvar mapa completo com sucesso")
        void deveSalvarMapaCompleto() {
            Long mapaId = 1L;
            SalvarMapaRequest req = new SalvarMapaRequest();
            req.setObservacoes("Obs");
            CompetenciaMapaDto compDto = new CompetenciaMapaDto();
            compDto.setDescricao("Nova Comp");
            compDto.setAtividadesCodigos(List.of(10L));
            req.setCompetencias(List.of(compDto));

            Mapa mapa = new Mapa();
            mapa.setCodigo(mapaId);

            Atividade ativ = new Atividade();
            ativ.setCodigo(10L);
            ativ.setMapa(mapa);
            ativ.setCompetencias(new HashSet<>());

            when(mapaRepo.findById(mapaId)).thenReturn(Optional.of(mapa));
            when(competenciaRepo.findByMapaCodigo(mapaId)).thenReturn(new ArrayList<>());
            when(atividadeRepo.findByMapaCodigo(mapaId)).thenReturn(List.of(ativ));

            Competencia novaComp = new Competencia();
            novaComp.setCodigo(50L);
            when(competenciaRepo.saveAll(anyList())).thenReturn(List.of(novaComp));

            // Second call for validation
            when(competenciaRepo.findByMapaCodigo(mapaId)).thenReturn(List.of(novaComp));
            MapaCompletoDto expectedDto = new MapaCompletoDto();
            when(mapaCompletoMapper.toDto(any(), any(), anyList())).thenReturn(expectedDto);

            var resultado = service.salvarMapaCompleto(mapaId, req, "user");

            assertThat(resultado).isNotNull();
            assertThat(resultado).isSameAs(expectedDto);
            verify(mapaRepo).save(mapa);
            verify(competenciaRepo).saveAll(anyList());
            verify(atividadeRepo).saveAll(anyList());
        }

        @Test
        @DisplayName("Deve remover competências ausentes")
        void deveRemoverCompetencias() {
            Long mapaId = 1L;
            SalvarMapaRequest req = new SalvarMapaRequest();
            req.setCompetencias(List.of()); // Empty list implies remove all

            Mapa mapa = new Mapa();
            Competencia compExistente = new Competencia();
            compExistente.setCodigo(100L);

            Atividade ativ = new Atividade();
            ativ.setCodigo(10L);
            ativ.setCompetencias(new HashSet<>(List.of(compExistente)));

            when(mapaRepo.findById(mapaId)).thenReturn(Optional.of(mapa));
            // First call returns existing to be removed
            // Second call (validation) returns empty
            when(competenciaRepo.findByMapaCodigo(mapaId))
                .thenReturn(new ArrayList<>(List.of(compExistente)))
                .thenReturn(List.of());

            when(atividadeRepo.findByMapaCodigo(mapaId)).thenReturn(List.of(ativ));

            service.salvarMapaCompleto(mapaId, req, "user");

            verify(competenciaRepo).deleteAll(anyList());
        }

        @Test
        @DisplayName("Deve manter competências presentes no request")
        void deveManterCompetenciasPresentes() {
            Long mapaId = 1L;
            SalvarMapaRequest req = new SalvarMapaRequest();
            CompetenciaMapaDto compDto = new CompetenciaMapaDto();
            compDto.setCodigo(100L);
            compDto.setDescricao("Comp Mantida");
            req.setCompetencias(List.of(compDto));

            Mapa mapa = new Mapa();
            Competencia compExistente = new Competencia();
            compExistente.setCodigo(100L);
            compExistente.setDescricao("Desc Antiga");

            when(mapaRepo.findById(mapaId)).thenReturn(Optional.of(mapa));
            when(competenciaRepo.findByMapaCodigo(mapaId))
                    .thenReturn(new ArrayList<>(List.of(compExistente)));

            when(atividadeRepo.findByMapaCodigo(mapaId)).thenReturn(List.of());
            when(competenciaRepo.saveAll(anyList())).thenReturn(List.of(compExistente));
            when(mapaCompletoMapper.toDto(any(), any(), anyList())).thenReturn(new MapaCompletoDto());

            service.salvarMapaCompleto(mapaId, req, "user");

            // Verifica que NÃO removeu nada
            verify(competenciaRepo, never()).deleteAll(anyList());
            // Verifica que a descrição foi atualizada
            assertThat(compExistente.getDescricao()).isEqualTo("Comp Mantida");
        }

        @Test
        @DisplayName("Deve falhar se atividade não pertence ao mapa")
        void deveFalharSeAtividadeInvalida() {
            Long mapaId = 1L;
            SalvarMapaRequest req = new SalvarMapaRequest();
            CompetenciaMapaDto compDto = new CompetenciaMapaDto();
            compDto.setAtividadesCodigos(List.of(99L)); // ID inválido
            req.setCompetencias(List.of(compDto));

            when(mapaRepo.findById(mapaId)).thenReturn(Optional.of(new Mapa()));
            when(competenciaRepo.findByMapaCodigo(mapaId)).thenReturn(new ArrayList<>());
            when(atividadeRepo.findByMapaCodigo(mapaId)).thenReturn(List.of()); // Nenhuma atividade no mapa

            when(competenciaRepo.saveAll(anyList())).thenReturn(List.of(new Competencia()));

            assertThatThrownBy(() -> service.salvarMapaCompleto(mapaId, req, "user"))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining("não pertence ao mapa");
        }
    }
}
