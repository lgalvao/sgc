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
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;

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
    private MapaRepo repositorioMapa;

    @Mock
    private CompetenciaRepo repositorioCompetencia;

    @Mock
    private CompetenciaAtividadeRepo repositorioCompetenciaAtividade;

    @Mock
    private SubprocessoRepo repositorioSubprocesso;

    @InjectMocks
    private MapaService mapaServico;

    private Mapa mapa;
    private Subprocesso subprocesso;
    private Competencia competencia;
    private CompetenciaAtividade competenciaAtividade;

    @BeforeEach
    void setUp() {
        mapa = new Mapa();
        mapa.setCodigo(1L);
        mapa.setObservacoesDisponibilizacao("Observações do Mapa");

        subprocesso = new Subprocesso();
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
        when(repositorioMapa.findById(1L)).thenReturn(Optional.of(mapa));
        when(repositorioSubprocesso.findByMapaCodigo(1L)).thenReturn(Optional.of(subprocesso));
        when(repositorioCompetencia.findByMapaCodigo(1L)).thenReturn(List.of(competencia));
        when(repositorioCompetenciaAtividade.findByCompetenciaCodigo(1L)).thenReturn(List.of(competenciaAtividade));

        MapaCompletoDto mapaCompleto = mapaServico.obterMapaCompleto(1L);

        assertThat(mapaCompleto).isNotNull();
        assertThat(mapaCompleto.codigo()).isEqualTo(1L);
        assertThat(mapaCompleto.subprocessoCodigo()).isEqualTo(100L);
        assertThat(mapaCompleto.observacoes()).isEqualTo("Observações do Mapa");
        assertThat(mapaCompleto.competencias()).hasSize(1);
        assertThat(mapaCompleto.competencias().getFirst().descricao()).isEqualTo("Competência 1");
    }

    @Test
    void obterMapaCompleto_deveLancarErro_quandoMapaNaoEncontrado() {
        when(repositorioMapa.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mapaServico.obterMapaCompleto(1L))
                .isInstanceOf(ErroDominioNaoEncontrado.class)
                .hasMessage("Mapa não encontrado: 1");
    }
}