package sgc.mapa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.competencia.modelo.Competencia;
import sgc.competencia.modelo.CompetenciaAtividade;
import sgc.competencia.modelo.CompetenciaAtividadeRepo;
import sgc.competencia.modelo.CompetenciaRepo;
import sgc.comum.enums.SituacaoSubprocesso;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.CompetenciaMapaDto;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MapaServiceTest {
    @Mock
    private MapaRepo repositorioMapa;

    @Mock
    private CompetenciaRepo repositorioCompetencia;

    @Mock
    private CompetenciaAtividadeRepo repositorioCompetenciaAtividade;

    @Mock
    private SubprocessoRepo repositorioSubprocesso;

    @Mock
    private sgc.atividade.modelo.AtividadeRepo atividadeRepo;

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
        when(repositorioSubprocesso.findAll()).thenReturn(List.of(subprocesso));
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
                .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                .hasMessage("Mapa não encontrado: 1");
    }

    @Test
    void obterMapaSubprocesso_deveRetornarMapa_quandoSubprocessoTemMapa() {
        when(repositorioSubprocesso.findById(100L)).thenReturn(Optional.of(subprocesso));
        when(repositorioMapa.findById(1L)).thenReturn(Optional.of(mapa));
        when(repositorioSubprocesso.findAll()).thenReturn(List.of(subprocesso));
        when(repositorioCompetencia.findByMapaCodigo(1L)).thenReturn(Collections.emptyList());

        MapaCompletoDto result = mapaServico.obterMapaSubprocesso(100L);

        assertThat(result).isNotNull();
        assertThat(result.codigo()).isEqualTo(mapa.getCodigo());
    }

    @Test
    void obterMapaSubprocesso_deveLancarErro_quandoSubprocessoNaoTemMapa() {
        subprocesso.setMapa(null);
        when(repositorioSubprocesso.findById(100L)).thenReturn(Optional.of(subprocesso));

        assertThatThrownBy(() -> mapaServico.obterMapaSubprocesso(100L))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                .hasMessage("Subprocesso não possui mapa associado");
    }

    @Test
    void validarMapaCompleto_deveLancarErro_quandoCompetenciaNaoTemAtividades() {
        when(repositorioMapa.findById(1L)).thenReturn(Optional.of(mapa));
        when(repositorioSubprocesso.findAll()).thenReturn(List.of(subprocesso));
        when(repositorioCompetencia.findByMapaCodigo(1L)).thenReturn(List.of(competencia));
        when(repositorioCompetenciaAtividade.findByCompetenciaCodigo(1L)).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> mapaServico.validarMapaCompleto(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("A competência 'Competência 1' não possui atividades vinculadas");
    }

    @Test
    void salvarMapaSubprocesso_deveAtualizarSituacao_quandoPrimeiraEdicao() {
        SalvarMapaRequest request = new SalvarMapaRequest("Obs", List.of(new CompetenciaMapaDto(1L, "Comp 1", List.of(1L))));
        subprocesso.setSituacao(SituacaoSubprocesso.CADASTRO_HOMOLOGADO);
        when(repositorioSubprocesso.findById(100L)).thenReturn(Optional.of(subprocesso));
        when(repositorioCompetencia.findByMapaCodigo(1L)).thenReturn(Collections.emptyList());
        when(repositorioMapa.findById(1L)).thenReturn(Optional.of(mapa));
        when(repositorioMapa.save(any(Mapa.class))).thenReturn(mapa);
        when(repositorioCompetencia.findById(1L)).thenReturn(Optional.of(competencia));
        when(repositorioCompetencia.save(any(Competencia.class))).thenReturn(competencia);
        when(atividadeRepo.existsById(1L)).thenReturn(true);

        mapaServico.salvarMapaSubprocesso(100L, request, "user");

        assertThat(subprocesso.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPA_CRIADO);
    }

    @Test
    void salvarMapaSubprocesso_deveLancarErro_quandoSituacaoInvalida() {
        SalvarMapaRequest request = new SalvarMapaRequest("Obs", Collections.emptyList());
        subprocesso.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
        when(repositorioSubprocesso.findById(100L)).thenReturn(Optional.of(subprocesso));

        assertThatThrownBy(() -> mapaServico.salvarMapaSubprocesso(100L, request, "user"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Mapa só pode ser editado com cadastro homologado ou mapa criado. Situação atual: NAO_INICIADO");
    }
}
