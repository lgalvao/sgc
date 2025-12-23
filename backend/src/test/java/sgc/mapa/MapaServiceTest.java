package sgc.mapa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sgc.atividade.internal.model.Atividade;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.api.CompetenciaMapaDto;
import sgc.mapa.api.MapaCompletoDto;
import sgc.mapa.api.SalvarMapaRequest;
import sgc.mapa.internal.mapper.MapaCompletoMapper;
import sgc.mapa.internal.model.Competencia;
import sgc.mapa.internal.model.CompetenciaRepo;
import sgc.mapa.internal.model.Mapa;
import sgc.mapa.internal.model.MapaRepo;
import sgc.mapa.MapaService;
import sgc.subprocesso.internal.model.Subprocesso;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do Serviço de Mapas")
class MapaServiceTest {
    @Mock
    private MapaRepo mapaRepo;

    @Mock
    private CompetenciaRepo competenciaRepo;

    @Mock
    private MapaCompletoMapper mapaCompletoMapper;

    @InjectMocks
    private MapaService mapaService;

    private Mapa mapa;
    private Competencia competencia;

    @BeforeEach
    void setUp() {
        mapa = new Mapa();
        mapa.setCodigo(1L);
        mapa.setObservacoesDisponibilizacao("Observações do Mapa");

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(100L);
        subprocesso.setMapa(mapa);

        competencia = new Competencia();
        competencia.setCodigo(1L);
        competencia.setDescricao("Competência 1");
        competencia.setMapa(mapa);

        Atividade atividade = new Atividade();
        atividade.setCodigo(1L);
        competencia.setAtividades(Set.of(atividade));
    }

    @Nested
    @DisplayName("CRUD de Mapa")
    class CrudMapa {
        @Test
        @DisplayName("Deve listar mapas")
        void deveListarMapas() {
            when(mapaRepo.findAll()).thenReturn(List.of(mapa));
            assertThat(mapaService.listar()).hasSize(1);
        }

        @Test
        @DisplayName("Deve obter mapa por código")
        void deveObterPorCodigo() {
            when(mapaRepo.findById(1L)).thenReturn(Optional.of(mapa));
            assertThat(mapaService.obterPorCodigo(1L)).isEqualTo(mapa);
        }

        @Test
        @DisplayName("Deve lançar erro ao obter mapa inexistente")
        void deveLancarErroAoObterMapaInexistente() {
            when(mapaRepo.findById(1L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> mapaService.obterPorCodigo(1L))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve criar mapa")
        void deveCriarMapa() {
            when(mapaRepo.save(mapa)).thenReturn(mapa);
            assertThat(mapaService.criar(mapa)).isEqualTo(mapa);
        }

        @Test
        @DisplayName("Deve atualizar mapa")
        void deveAtualizarMapa() {
            when(mapaRepo.findById(1L)).thenReturn(Optional.of(mapa));
            when(mapaRepo.save(mapa)).thenReturn(mapa);
            assertThat(mapaService.atualizar(1L, mapa)).isEqualTo(mapa);
        }

        @Test
        @DisplayName("Deve lançar erro ao atualizar mapa inexistente")
        void deveLancarErroAoAtualizarMapaInexistente() {
            when(mapaRepo.findById(1L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> mapaService.atualizar(1L, mapa))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve excluir mapa")
        void deveExcluirMapa() {
            when(mapaRepo.existsById(1L)).thenReturn(true);
            mapaService.excluir(1L);
            verify(mapaRepo).deleteById(1L);
        }

        @Test
        @DisplayName("Deve lançar erro ao excluir mapa inexistente")
        void deveLancarErroAoExcluirMapaInexistente() {
            when(mapaRepo.existsById(1L)).thenReturn(false);
            assertThatThrownBy(() -> mapaService.excluir(1L))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }
    }

    @Nested
    @DisplayName("Mapa Completo")
    class MapaCompleto {
        @Test
        @DisplayName("Deve retornar mapa completo quando existir")
        void deveRetornarMapaCompletoQuandoExistir() {
            when(mapaRepo.findById(1L)).thenReturn(Optional.of(mapa));
            when(competenciaRepo.findByMapaCodigo(1L)).thenReturn(List.of(competencia));

            MapaCompletoDto dtoMock = MapaCompletoDto.builder()
                    .codigo(1L)
                    .subprocessoCodigo(100L)
                    .observacoes("Observações do Mapa")
                    .competencias(List.of(new CompetenciaMapaDto(1L, "Competência 1", List.of(1L))))
                    .build();

            when(mapaCompletoMapper.toDto(any(), anyLong(), anyList())).thenReturn(dtoMock);

            MapaCompletoDto mapaCompleto = mapaService.obterMapaCompleto(1L, 100L);

            assertThat(mapaCompleto).isNotNull();
            assertThat(mapaCompleto.getCodigo()).isEqualTo(1L);
            assertThat(mapaCompleto.getSubprocessoCodigo()).isEqualTo(100L);
            assertThat(mapaCompleto.getObservacoes()).isEqualTo("Observações do Mapa");
            assertThat(mapaCompleto.getCompetencias()).hasSize(1);
            assertThat(mapaCompleto.getCompetencias().getFirst().getDescricao())
                    .isEqualTo("Competência 1");
        }

        @Test
        @DisplayName("Deve lançar erro ao buscar mapa completo inexistente")
        void deveLancarErroAoBuscarMapaCompletoInexistente() {
            when(mapaRepo.findById(anyLong())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> mapaService.obterMapaCompleto(1L, 100L))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                    .hasMessage("Mapa não encontrado: 1");
        }

        @Test
        @DisplayName("Deve lançar erro ao salvar mapa completo se mapa não existe")
        void deveLancarErroAoSalvarMapaCompletoInexistente() {
            when(mapaRepo.findById(1L)).thenReturn(Optional.empty());
            SalvarMapaRequest req = new SalvarMapaRequest();
            assertThatThrownBy(() -> mapaService.salvarMapaCompleto(1L, req, "user"))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve lançar erro ao salvar mapa completo se competência não existe")
        void deveLancarErroAoSalvarMapaCompletoSeCompetenciaNaoExiste() {
            SalvarMapaRequest req = new SalvarMapaRequest();
            req.setObservacoes("Obs");
            CompetenciaMapaDto cDto1 = new CompetenciaMapaDto(99L, "Comp 99", List.of());
            req.setCompetencias(List.of(cDto1));

            when(mapaRepo.findById(1L)).thenReturn(Optional.of(mapa));
            when(competenciaRepo.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> mapaService.salvarMapaCompleto(1L, req, "user"))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }
    }
}
