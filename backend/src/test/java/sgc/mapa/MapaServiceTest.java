package sgc.mapa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sgc.competencia.modelo.Competencia;
import sgc.competencia.modelo.CompetenciaAtividade;
import sgc.competencia.modelo.CompetenciaAtividadeRepo;
import sgc.competencia.modelo.CompetenciaRepo;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.mapa.service.MapaService;
import sgc.subprocesso.modelo.Subprocesso;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MapaServiceTest {
    @Mock
    private MapaRepo mapaRepo;

    @Mock
    private CompetenciaRepo competenciaRepo;

    @Mock
    private CompetenciaAtividadeRepo competenciaAtividadeRepo;

    @InjectMocks
    private MapaService mapaService;

    private Mapa mapa;
    private Competencia competencia;
    private CompetenciaAtividade competenciaAtividade;

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

        competenciaAtividade = new CompetenciaAtividade();
        competenciaAtividade.setId(new CompetenciaAtividade.Id(1L, 1L));
    }

    @Test
    void obterMapaCompleto_deveRetornarMapaCompleto_quandoMapaExistir() {
        when(mapaRepo.findById(1L)).thenReturn(Optional.of(mapa));
        when(competenciaRepo.findByMapaCodigo(1L)).thenReturn(List.of(competencia));
        when(competenciaAtividadeRepo.findByCompetenciaCodigo(1L)).thenReturn(List.of(competenciaAtividade));

        MapaCompletoDto mapaCompleto = mapaService.obterMapaCompleto(1L, 100L);

        assertThat(mapaCompleto).isNotNull();
        assertThat(mapaCompleto.codigo()).isEqualTo(1L);
        assertThat(mapaCompleto.subprocessoCodigo()).isEqualTo(100L);
        assertThat(mapaCompleto.observacoes()).isEqualTo("Observações do Mapa");
        assertThat(mapaCompleto.competencias()).hasSize(1);
        assertThat(mapaCompleto.competencias().getFirst().descricao()).isEqualTo("Competência 1");
    }

    @Test
    void obterMapaCompleto_deveLancarErro_quandoMapaNaoEncontrado() {
        when(mapaRepo.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mapaService.obterMapaCompleto(1L, 100L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                .hasMessage("Mapa não encontrado: 1");
    }
}