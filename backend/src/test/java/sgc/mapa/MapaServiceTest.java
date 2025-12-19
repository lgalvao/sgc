package sgc.mapa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sgc.atividade.model.Atividade;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.CompetenciaMapaDto;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.mapper.MapaCompletoMapper;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.mapa.service.MapaService;
import sgc.subprocesso.model.Subprocesso;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

    @Test
    void listar() {
        when(mapaRepo.findAll()).thenReturn(List.of(mapa));
        assertThat(mapaService.listar()).hasSize(1);
    }

    @Test
    void obterPorCodigo() {
        when(mapaRepo.findById(1L)).thenReturn(Optional.of(mapa));
        assertThat(mapaService.obterPorCodigo(1L)).isEqualTo(mapa);
    }

    @Test
    void obterPorCodigo_NaoEncontrado() {
        when(mapaRepo.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> mapaService.obterPorCodigo(1L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    void criar() {
        when(mapaRepo.save(mapa)).thenReturn(mapa);
        assertThat(mapaService.criar(mapa)).isEqualTo(mapa);
    }

    @Test
    void atualizar() {
        when(mapaRepo.findById(1L)).thenReturn(Optional.of(mapa));
        when(mapaRepo.save(mapa)).thenReturn(mapa);
        assertThat(mapaService.atualizar(1L, mapa)).isEqualTo(mapa);
    }

    @Test
    void atualizar_NaoEncontrado() {
        when(mapaRepo.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> mapaService.atualizar(1L, mapa))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    void excluir() {
        when(mapaRepo.existsById(1L)).thenReturn(true);
        mapaService.excluir(1L);
        verify(mapaRepo).deleteById(1L);
    }

    @Test
    void excluir_NaoEncontrado() {
        when(mapaRepo.existsById(1L)).thenReturn(false);
        assertThatThrownBy(() -> mapaService.excluir(1L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    void obterMapaCompleto_deveRetornarMapaCompleto_quandoMapaExistir() {
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
    void obterMapaCompleto_deveLancarErro_quandoMapaNaoEncontrado() {
        when(mapaRepo.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mapaService.obterMapaCompleto(1L, 100L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                .hasMessage("Mapa não encontrado: 1");
    }

    @Test
    void salvarMapaCompleto_MapaNaoEncontrado() {
        when(mapaRepo.findById(1L)).thenReturn(Optional.empty());
        SalvarMapaRequest req = new SalvarMapaRequest();
        assertThatThrownBy(() -> mapaService.salvarMapaCompleto(1L, req, "user"))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    void salvarMapaCompleto_CompetenciaNaoEncontrada() {
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
