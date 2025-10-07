package sgc.mapa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.atividade.AtividadeRepository;
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
class ImpactoMapaServiceImplTest {

    @Mock
    private SubprocessoRepository subprocessoRepository;
    @Mock
    private MapaRepository mapaRepository;
    @Mock
    private AtividadeRepository atividadeRepository;
    @Mock
    private CompetenciaRepository competenciaRepository;
    @Mock
    private CompetenciaAtividadeRepository competenciaAtividadeRepository;

    @InjectMocks
    private ImpactoMapaServiceImpl impactoMapaService;

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
    void verificarImpactos_shouldReturnNoImpacts_whenNoMapaVigenteExists() {
        // Arrange
        when(subprocessoRepository.findById(100L)).thenReturn(Optional.of(subprocesso));
        when(mapaRepository.findMapaVigenteByUnidade(1L)).thenReturn(Optional.empty());

        // Act
        ImpactoMapaDto result = impactoMapaService.verificarImpactos(100L);

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
    void verificarImpactos_shouldDetectRemovidas_whenAtividadeIsRemoved() {
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

        when(subprocessoRepository.findById(100L)).thenReturn(Optional.of(subprocesso));
        when(mapaRepository.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(mapaVigente));
        when(mapaRepository.findBySubprocessoCodigo(100L)).thenReturn(Optional.of(mapaSubprocesso));

        // Mapa do subprocesso (atual) está vazio
        when(atividadeRepository.findByMapaCodigo(2L)).thenReturn(java.util.Collections.emptyList());

        // Mapa vigente continha a atividade
        when(competenciaRepository.findByMapaCodigo(1L)).thenReturn(java.util.List.of(competencia));
        when(competenciaAtividadeRepository.findByCompetenciaCodigo(20L)).thenReturn(java.util.List.of(vinculo));
        when(atividadeRepository.findAllById(java.util.Set.of(10L))).thenReturn(java.util.List.of(atividadeRemovida));

        // Mocks para identificar a competência impactada
        when(competenciaAtividadeRepository.findByAtividadeCodigo(10L)).thenReturn(java.util.List.of(vinculo));
        when(competenciaRepository.findById(20L)).thenReturn(Optional.of(competencia));


        // Act
        ImpactoMapaDto result = impactoMapaService.verificarImpactos(100L);

        // Assert
        assertThat(result.temImpactos()).isTrue();
        assertThat(result.totalAtividadesRemovidas()).isEqualTo(1);
        assertThat(result.atividadesRemovidas()).hasSize(1);
        assertThat(result.atividadesRemovidas().get(0).descricao()).isEqualTo("Atividade Antiga");
        assertThat(result.competenciasImpactadas()).hasSize(1);
        assertThat(result.competenciasImpactadas().get(0).descricao()).isEqualTo("Competência Afetada");
    }

    @Test
    void verificarImpactos_shouldDetectNoImpacts_whenMapsAreEqual() {
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

        when(subprocessoRepository.findById(100L)).thenReturn(Optional.of(subprocesso));
        when(mapaRepository.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(mapaVigente));
        when(mapaRepository.findBySubprocessoCodigo(100L)).thenReturn(Optional.of(mapaSubprocesso));

        // Both maps return the same activity
        when(atividadeRepository.findByMapaCodigo(2L)).thenReturn(java.util.List.of(atividade));
        when(competenciaRepository.findByMapaCodigo(1L)).thenReturn(java.util.List.of(competencia));
        when(competenciaAtividadeRepository.findByCompetenciaCodigo(20L)).thenReturn(java.util.List.of(vinculo));
        when(atividadeRepository.findAllById(java.util.Set.of(10L))).thenReturn(java.util.List.of(atividade));


        // Act
        ImpactoMapaDto result = impactoMapaService.verificarImpactos(100L);

        // Assert
        assertThat(result.temImpactos()).isFalse();
        assertThat(result.totalAtividadesInseridas()).isZero();
        assertThat(result.totalAtividadesRemovidas()).isZero();
        assertThat(result.totalAtividadesAlteradas()).isZero();
    }

    @Test
    void verificarImpactos_shouldDetectInseridas_whenAtividadeIsNew() {
        // Arrange
        Mapa mapaVigente = new Mapa();
        mapaVigente.setCodigo(1L);
        Mapa mapaSubprocesso = new Mapa();
        mapaSubprocesso.setCodigo(2L);

        Atividade atividadeNova = new Atividade();
        atividadeNova.setCodigo(10L);
        atividadeNova.setDescricao("Atividade Nova");
        atividadeNova.setMapa(mapaSubprocesso);

        when(subprocessoRepository.findById(100L)).thenReturn(Optional.of(subprocesso));
        when(mapaRepository.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(mapaVigente));
        when(mapaRepository.findBySubprocessoCodigo(100L)).thenReturn(Optional.of(mapaSubprocesso));

        // Mapa do subprocesso tem a nova atividade
        when(atividadeRepository.findByMapaCodigo(2L)).thenReturn(java.util.List.of(atividadeNova));

        // Mapa vigente está vazio
        when(competenciaRepository.findByMapaCodigo(1L)).thenReturn(java.util.Collections.emptyList());

        // Act
        ImpactoMapaDto result = impactoMapaService.verificarImpactos(100L);

        // Assert
        assertThat(result.temImpactos()).isTrue();
        assertThat(result.totalAtividadesInseridas()).isEqualTo(1);
        assertThat(result.atividadesInseridas()).hasSize(1);
        assertThat(result.atividadesInseridas().get(0).descricao()).isEqualTo("Atividade Nova");
        assertThat(result.competenciasImpactadas()).isEmpty(); // Inserções não impactam competências existentes
    }

    @Test
    void verificarImpactos_shouldDetectAlteradas_whenAtividadeIsModified() {
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

        when(subprocessoRepository.findById(100L)).thenReturn(Optional.of(subprocesso));
        when(mapaRepository.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(mapaVigente));
        when(mapaRepository.findBySubprocessoCodigo(100L)).thenReturn(Optional.of(mapaSubprocesso));

        // Mapa do subprocesso tem a atividade com a nova descrição
        when(atividadeRepository.findByMapaCodigo(2L)).thenReturn(java.util.List.of(atividadeAtual));

        // Mapa vigente tem a atividade com a descrição antiga
        when(competenciaRepository.findByMapaCodigo(1L)).thenReturn(java.util.List.of(competencia));
        when(competenciaAtividadeRepository.findByCompetenciaCodigo(20L)).thenReturn(java.util.List.of(vinculo));
        when(atividadeRepository.findAllById(java.util.Set.of(10L))).thenReturn(java.util.List.of(atividadeVigente));

        // Mocks para identificar a competência impactada
        when(competenciaAtividadeRepository.findByAtividadeCodigo(10L)).thenReturn(java.util.List.of(vinculo));
        when(competenciaRepository.findById(20L)).thenReturn(Optional.of(competencia));

        // Act
        ImpactoMapaDto result = impactoMapaService.verificarImpactos(100L);

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