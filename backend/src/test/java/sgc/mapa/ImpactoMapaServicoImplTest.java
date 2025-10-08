package sgc.mapa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.atividade.RepositorioAtividade;
import sgc.atividade.Atividade;
import sgc.competencia.Competencia;
import sgc.competencia.CompetenciaAtividade;
import sgc.competencia.CompetenciaAtividadeRepository;
import sgc.competencia.CompetenciaRepository;
import sgc.mapa.dto.ImpactoMapaDto;
import sgc.subprocesso.Subprocesso;
import sgc.subprocesso.SubprocessoRepository;
import sgc.unidade.Unidade;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImpactoMapaServicoImplTest {

    @Mock
    private SubprocessoRepository repositorioSubprocesso;
    @Mock
    private MapaRepository repositorioMapa;
    @Mock
    private RepositorioAtividade repositorioAtividade;
    @Mock
    private CompetenciaRepository repositorioCompetencia;
    @Mock
    private CompetenciaAtividadeRepository repositorioCompetenciaAtividade;

    @InjectMocks
    private ImpactoMapaServicoImpl impactoMapaServico;

    private Subprocesso subprocesso;
    private Unidade unidade;

    @BeforeEach
    void setUp() {
        unidade = new Unidade();
        unidade.setCodigo(1L);

        subprocesso = new Subprocesso();
        subprocesso.setCodigo(100L);
        subprocesso.setUnidade(unidade);
    }

    @Test
    void verificarImpactos_deveRetornarSemImpactos_quandoNaoHaMapaVigente() {
        // Arrange
        when(repositorioSubprocesso.findById(100L)).thenReturn(Optional.of(subprocesso));
        when(repositorioMapa.findMapaVigenteByUnidade(1L)).thenReturn(Optional.empty());

        // Act
        ImpactoMapaDto result = impactoMapaServico.verificarImpactos(100L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.temImpactos()).isFalse();
        assertThat(result.totalAtividadesInseridas()).isZero();
        assertThat(result.totalAtividadesRemovidas()).isZero();
        assertThat(result.totalAtividadesAlteradas()).isZero();
        assertThat(result.atividadesInseridas()).isEmpty();
        assertThat(result.atividadesRemovidas()).isEmpty();
        assertThat(result.atividadesAlteradas()).isEmpty();
        assertThat(result.competenciasImpactadas()).isEmpty();
    }

    @Test
    void verificarImpactos_deveDetectarRemovidas_quandoAtividadeEhRemovida() {
        // Arrange
        Mapa mapaVigente = new Mapa();
        mapaVigente.setCodigo(1L);
        Mapa mapaSubprocesso = new Mapa();
        mapaSubprocesso.setCodigo(2L);

        Atividade atividadeRemovida = new Atividade();
        atividadeRemovida.setCodigo(10L);
        atividadeRemovida.setDescricao("Atividade Antiga");
        atividadeRemovida.setMapa(mapaVigente);

        Competencia competencia = new Competencia();
        competencia.setCodigo(20L);
        competencia.setDescricao("Competência Afetada");
        competencia.setMapa(mapaVigente);

        CompetenciaAtividade vinculo = new CompetenciaAtividade();
        vinculo.setId(new CompetenciaAtividade.Id(10L, 20L));

        when(repositorioSubprocesso.findById(100L)).thenReturn(Optional.of(subprocesso));
        when(repositorioMapa.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(mapaVigente));
        when(repositorioMapa.findBySubprocessoCodigo(100L)).thenReturn(Optional.of(mapaSubprocesso));

        when(repositorioAtividade.findByMapaCodigo(2L)).thenReturn(java.util.Collections.emptyList());

        when(repositorioCompetencia.findByMapaCodigo(1L)).thenReturn(java.util.List.of(competencia));
        when(repositorioCompetenciaAtividade.findByCompetenciaCodigo(20L)).thenReturn(java.util.List.of(vinculo));
        when(repositorioAtividade.findAllById(java.util.Set.of(10L))).thenReturn(java.util.List.of(atividadeRemovida));

        when(repositorioCompetenciaAtividade.findByAtividadeCodigo(10L)).thenReturn(java.util.List.of(vinculo));
        when(repositorioCompetencia.findById(20L)).thenReturn(Optional.of(competencia));


        // Act
        ImpactoMapaDto result = impactoMapaServico.verificarImpactos(100L);

        // Assert
        assertThat(result.temImpactos()).isTrue();
        assertThat(result.totalAtividadesRemovidas()).isEqualTo(1);
        assertThat(result.atividadesRemovidas()).hasSize(1);
        assertThat(result.atividadesRemovidas().get(0).descricao()).isEqualTo("Atividade Antiga");
        assertThat(result.competenciasImpactadas()).hasSize(1);
        assertThat(result.competenciasImpactadas().get(0).descricao()).isEqualTo("Competência Afetada");
    }

    @Test
    void verificarImpactos_deveDetectarSemImpactos_quandoMapasSaoIguais() {
        // Arrange
        Mapa mapaVigente = new Mapa();
        mapaVigente.setCodigo(1L);
        Mapa mapaSubprocesso = new Mapa();
        mapaSubprocesso.setCodigo(2L);

        Atividade atividade = new Atividade();
        atividade.setCodigo(10L);
        atividade.setDescricao("Atividade Comum");

        Competencia competencia = new Competencia();
        competencia.setCodigo(20L);
        competencia.setMapa(mapaVigente);

        CompetenciaAtividade vinculo = new CompetenciaAtividade();
        vinculo.setId(new CompetenciaAtividade.Id(10L, 20L));

        when(repositorioSubprocesso.findById(100L)).thenReturn(Optional.of(subprocesso));
        when(repositorioMapa.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(mapaVigente));
        when(repositorioMapa.findBySubprocessoCodigo(100L)).thenReturn(Optional.of(mapaSubprocesso));

        when(repositorioAtividade.findByMapaCodigo(2L)).thenReturn(java.util.List.of(atividade));
        when(repositorioCompetencia.findByMapaCodigo(1L)).thenReturn(java.util.List.of(competencia));
        when(repositorioCompetenciaAtividade.findByCompetenciaCodigo(20L)).thenReturn(java.util.List.of(vinculo));
        when(repositorioAtividade.findAllById(java.util.Set.of(10L))).thenReturn(java.util.List.of(atividade));


        // Act
        ImpactoMapaDto result = impactoMapaServico.verificarImpactos(100L);

        // Assert
        assertThat(result.temImpactos()).isFalse();
        assertThat(result.totalAtividadesInseridas()).isZero();
        assertThat(result.totalAtividadesRemovidas()).isZero();
        assertThat(result.totalAtividadesAlteradas()).isZero();
    }

    @Test
    void verificarImpactos_deveDetectarInseridas_quandoAtividadeEhNova() {
        // Arrange
        Mapa mapaVigente = new Mapa();
        mapaVigente.setCodigo(1L);
        Mapa mapaSubprocesso = new Mapa();
        mapaSubprocesso.setCodigo(2L);

        Atividade atividadeNova = new Atividade();
        atividadeNova.setCodigo(10L);
        atividadeNova.setDescricao("Atividade Nova");
        atividadeNova.setMapa(mapaSubprocesso);

        when(repositorioSubprocesso.findById(100L)).thenReturn(Optional.of(subprocesso));
        when(repositorioMapa.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(mapaVigente));
        when(repositorioMapa.findBySubprocessoCodigo(100L)).thenReturn(Optional.of(mapaSubprocesso));

        when(repositorioAtividade.findByMapaCodigo(2L)).thenReturn(java.util.List.of(atividadeNova));

        when(repositorioCompetencia.findByMapaCodigo(1L)).thenReturn(java.util.Collections.emptyList());

        // Act
        ImpactoMapaDto result = impactoMapaServico.verificarImpactos(100L);

        // Assert
        assertThat(result.temImpactos()).isTrue();
        assertThat(result.totalAtividadesInseridas()).isEqualTo(1);
        assertThat(result.atividadesInseridas()).hasSize(1);
        assertThat(result.atividadesInseridas().get(0).descricao()).isEqualTo("Atividade Nova");
        assertThat(result.competenciasImpactadas()).isEmpty();
    }

    @Test
    void verificarImpactos_deveDetectarAlteradas_quandoAtividadeEhModificada() {
        // Arrange
        Mapa mapaVigente = new Mapa();
        mapaVigente.setCodigo(1L);
        Mapa mapaSubprocesso = new Mapa();
        mapaSubprocesso.setCodigo(2L);

        Atividade atividadeVigente = new Atividade();
        atividadeVigente.setCodigo(10L);
        atividadeVigente.setDescricao("Descrição Antiga");
        atividadeVigente.setMapa(mapaVigente);

        Atividade atividadeAtual = new Atividade();
        atividadeAtual.setCodigo(10L);
        atividadeAtual.setDescricao("Descrição Nova");
        atividadeAtual.setMapa(mapaSubprocesso);

        Competencia competencia = new Competencia();
        competencia.setCodigo(20L);
        competencia.setDescricao("Competência Afetada");
        competencia.setMapa(mapaVigente);

        CompetenciaAtividade vinculo = new CompetenciaAtividade();
        vinculo.setId(new CompetenciaAtividade.Id(10L, 20L));

        when(repositorioSubprocesso.findById(100L)).thenReturn(Optional.of(subprocesso));
        when(repositorioMapa.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(mapaVigente));
        when(repositorioMapa.findBySubprocessoCodigo(100L)).thenReturn(Optional.of(mapaSubprocesso));

        when(repositorioAtividade.findByMapaCodigo(2L)).thenReturn(java.util.List.of(atividadeAtual));

        when(repositorioCompetencia.findByMapaCodigo(1L)).thenReturn(java.util.List.of(competencia));
        when(repositorioCompetenciaAtividade.findByCompetenciaCodigo(20L)).thenReturn(java.util.List.of(vinculo));
        when(repositorioAtividade.findAllById(java.util.Set.of(10L))).thenReturn(java.util.List.of(atividadeVigente));

        when(repositorioCompetenciaAtividade.findByAtividadeCodigo(10L)).thenReturn(java.util.List.of(vinculo));
        when(repositorioCompetencia.findById(20L)).thenReturn(Optional.of(competencia));

        // Act
        ImpactoMapaDto result = impactoMapaServico.verificarImpactos(100L);

        // Assert
        assertThat(result.temImpactos()).isTrue();
        assertThat(result.totalAtividadesAlteradas()).isEqualTo(1);
        assertThat(result.atividadesAlteradas()).hasSize(1);
        assertThat(result.atividadesAlteradas().get(0).descricao()).isEqualTo("Descrição Nova");
        assertThat(result.atividadesAlteradas().get(0).descricaoAnterior()).isEqualTo("Descrição Antiga");
        assertThat(result.competenciasImpactadas()).hasSize(1);
        assertThat(result.competenciasImpactadas().get(0).descricao()).isEqualTo("Competência Afetada");
    }
}