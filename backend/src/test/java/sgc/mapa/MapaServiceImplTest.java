package sgc.mapa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.atividade.AtividadeRepository;
import sgc.competencia.Competencia;
import sgc.competencia.CompetenciaAtividade;
import sgc.competencia.CompetenciaAtividadeRepository;
import sgc.competencia.CompetenciaRepository;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.CompetenciaMapaDto;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.subprocesso.Subprocesso;
import sgc.subprocesso.SubprocessoRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MapaServiceImplTest {

    @Mock
    private MapaRepository mapaRepository;
    @Mock
    private CompetenciaRepository competenciaRepository;
    @Mock
    private CompetenciaAtividadeRepository competenciaAtividadeRepository;
    @Mock
    private AtividadeRepository atividadeRepository;
    @Mock
    private SubprocessoRepository subprocessoRepository;

    @InjectMocks
    private MapaServiceImpl mapaService;

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
        when(mapaRepository.findById(1L)).thenReturn(Optional.of(mapa));
        when(subprocessoRepository.findAll()).thenReturn(List.of(subprocesso));
        when(competenciaRepository.findByMapaCodigo(1L)).thenReturn(List.of(competencia));
        when(competenciaAtividadeRepository.findByCompetenciaCodigo(1L)).thenReturn(List.of(competenciaAtividade));

        MapaCompletoDto mapaCompleto = mapaService.obterMapaCompleto(1L);

        assertThat(mapaCompleto).isNotNull();
        assertThat(mapaCompleto.codigo()).isEqualTo(1L);
        assertThat(mapaCompleto.subprocessoCodigo()).isEqualTo(100L);
        assertThat(mapaCompleto.observacoes()).isEqualTo("Observações do Mapa");
        assertThat(mapaCompleto.competencias()).hasSize(1);
        assertThat(mapaCompleto.competencias().get(0).descricao()).isEqualTo("Competência 1");
    }

    @Test
    void obterMapaCompleto_deveLancarErro_quandoMapaNaoEncontrado() {
        when(mapaRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mapaService.obterMapaCompleto(1L))
            .isInstanceOf(ErroEntidadeNaoEncontrada.class)
            .hasMessage("Mapa não encontrado: 1");
    }

    @Test
    void obterMapaSubprocesso_deveRetornarMapa_quandoSubprocessoTemMapa() {
        when(subprocessoRepository.findById(100L)).thenReturn(Optional.of(subprocesso));
        when(mapaRepository.findById(1L)).thenReturn(Optional.of(mapa));
        when(subprocessoRepository.findAll()).thenReturn(List.of(subprocesso));
        when(competenciaRepository.findByMapaCodigo(1L)).thenReturn(Collections.emptyList());

        MapaCompletoDto result = mapaService.obterMapaSubprocesso(100L);

        assertThat(result).isNotNull();
        assertThat(result.codigo()).isEqualTo(mapa.getCodigo());
    }

    @Test
    void obterMapaSubprocesso_deveLancarErro_quandoSubprocessoNaoTemMapa() {
        subprocesso.setMapa(null);
        when(subprocessoRepository.findById(100L)).thenReturn(Optional.of(subprocesso));

        assertThatThrownBy(() -> mapaService.obterMapaSubprocesso(100L))
            .isInstanceOf(ErroEntidadeNaoEncontrada.class)
            .hasMessage("Subprocesso não possui mapa associado");
    }

    @Test
    void validarMapaCompleto_deveLancarErro_quandoCompetenciaNaoTemAtividades() {
        when(mapaRepository.findById(1L)).thenReturn(Optional.of(mapa));
        when(subprocessoRepository.findAll()).thenReturn(List.of(subprocesso));
        when(competenciaRepository.findByMapaCodigo(1L)).thenReturn(List.of(competencia));
        when(competenciaAtividadeRepository.findByCompetenciaCodigo(1L)).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> mapaService.validarMapaCompleto(1L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("A competência 'Competência 1' não possui atividades vinculadas");
    }

    @Test
    void salvarMapaSubprocesso_deveAtualizarSituacao_quandoPrimeiraEdicao() {
        SalvarMapaRequest request = new SalvarMapaRequest("Obs", List.of(new CompetenciaMapaDto(1L, "Comp 1", List.of(1L))));
        subprocesso.setSituacaoId("CADASTRO_HOMOLOGADO");
        when(subprocessoRepository.findById(100L)).thenReturn(Optional.of(subprocesso));
        when(competenciaRepository.findByMapaCodigo(1L)).thenReturn(Collections.emptyList());
        when(mapaRepository.findById(1L)).thenReturn(Optional.of(mapa));
        when(mapaRepository.save(any(Mapa.class))).thenReturn(mapa);
        when(competenciaRepository.findById(1L)).thenReturn(Optional.of(competencia));
        when(competenciaRepository.save(any(Competencia.class))).thenReturn(competencia);

        mapaService.salvarMapaSubprocesso(100L, request, "user");

        assertThat(subprocesso.getSituacaoId()).isEqualTo("MAPA_CRIADO");
    }

    @Test
    void salvarMapaSubprocesso_deveLancarErro_quandoSituacaoInvalida() {
        SalvarMapaRequest request = new SalvarMapaRequest("Obs", Collections.emptyList());
        subprocesso.setSituacaoId("INVALIDA");
        when(subprocessoRepository.findById(100L)).thenReturn(Optional.of(subprocesso));

        assertThatThrownBy(() -> mapaService.salvarMapaSubprocesso(100L, request, "user"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Mapa só pode ser editado com cadastro homologado ou mapa criado. Situação atual: INVALIDA");
    }
}