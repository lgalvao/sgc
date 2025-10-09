package sgc.mapa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.competencia.modelo.Competencia;
import sgc.competencia.modelo.CompetenciaAtividade;
import sgc.competencia.modelo.CompetenciaAtividadeRepo;
import sgc.competencia.modelo.CompetenciaRepo;
import sgc.mapa.dto.ImpactoMapaDto;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImpactoMapaServiceImplTest {
    @Mock
    private SubprocessoRepo repositorioSubprocesso;

    @Mock
    private MapaRepo repositorioMapa;

    @Mock
    private AtividadeRepo atividadeRepo;

    @Mock
    private CompetenciaRepo repositorioCompetencia;

    @Mock
    private CompetenciaAtividadeRepo repositorioCompetenciaAtividade;

    @InjectMocks
    private ImpactoMapaServiceImpl impactoMapaServico;

    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);

        subprocesso = new Subprocesso();
        subprocesso.setCodigo(100L);
        subprocesso.setUnidade(unidade);
    }

    @Test
    void verificarImpactos_deveRetornarSemImpactos_quandoNaoHaMapaVigente() {
        when(repositorioSubprocesso.findById(100L)).thenReturn(Optional.of(subprocesso));
        when(repositorioMapa.findMapaVigenteByUnidade(1L)).thenReturn(Optional.empty());

        ImpactoMapaDto result = impactoMapaServico.verificarImpactos(100L);

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

        when(atividadeRepo.findByMapaCodigo(2L)).thenReturn(java.util.Collections.emptyList());

        when(repositorioCompetencia.findByMapaCodigo(1L)).thenReturn(java.util.List.of(competencia));
        when(repositorioCompetenciaAtividade.findByCompetenciaCodigo(20L)).thenReturn(java.util.List.of(vinculo));
        when(atividadeRepo.findAllById(java.util.Set.of(10L))).thenReturn(java.util.List.of(atividadeRemovida));

        when(repositorioCompetenciaAtividade.findByAtividadeCodigo(10L)).thenReturn(java.util.List.of(vinculo));
        when(repositorioCompetencia.findById(20L)).thenReturn(Optional.of(competencia));


        ImpactoMapaDto result = impactoMapaServico.verificarImpactos(100L);

        assertThat(result.temImpactos()).isTrue();
        assertThat(result.totalAtividadesRemovidas()).isEqualTo(1);
        assertThat(result.atividadesRemovidas()).hasSize(1);
        assertThat(result.atividadesRemovidas().getFirst().descricao()).isEqualTo("Atividade Antiga");
        assertThat(result.competenciasImpactadas()).hasSize(1);
        assertThat(result.competenciasImpactadas().getFirst().descricao()).isEqualTo("Competência Afetada");
    }

    @Test
    void verificarImpactos_deveDetectarSemImpactos_quandoMapasSaoIguais() {
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

        when(atividadeRepo.findByMapaCodigo(2L)).thenReturn(java.util.List.of(atividade));
        when(repositorioCompetencia.findByMapaCodigo(1L)).thenReturn(java.util.List.of(competencia));
        when(repositorioCompetenciaAtividade.findByCompetenciaCodigo(20L)).thenReturn(java.util.List.of(vinculo));
        when(atividadeRepo.findAllById(java.util.Set.of(10L))).thenReturn(java.util.List.of(atividade));


        ImpactoMapaDto result = impactoMapaServico.verificarImpactos(100L);

        assertThat(result.temImpactos()).isFalse();
        assertThat(result.totalAtividadesInseridas()).isZero();
        assertThat(result.totalAtividadesRemovidas()).isZero();
        assertThat(result.totalAtividadesAlteradas()).isZero();
    }

    @Test
    void verificarImpactos_deveDetectarInseridas_quandoAtividadeEhNova() {
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

        when(atividadeRepo.findByMapaCodigo(2L)).thenReturn(java.util.List.of(atividadeNova));

        when(repositorioCompetencia.findByMapaCodigo(1L)).thenReturn(java.util.Collections.emptyList());

        ImpactoMapaDto result = impactoMapaServico.verificarImpactos(100L);

        assertThat(result.temImpactos()).isTrue();
        assertThat(result.totalAtividadesInseridas()).isEqualTo(1);
        assertThat(result.atividadesInseridas()).hasSize(1);
        assertThat(result.atividadesInseridas().getFirst().descricao()).isEqualTo("Atividade Nova");
        assertThat(result.competenciasImpactadas()).isEmpty();
    }

    @Test
    void verificarImpactos_deveDetectarAlteradas_quandoAtividadeEhModificada() {
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

        when(atividadeRepo.findByMapaCodigo(2L)).thenReturn(java.util.List.of(atividadeAtual));

        when(repositorioCompetencia.findByMapaCodigo(1L)).thenReturn(java.util.List.of(competencia));
        when(repositorioCompetenciaAtividade.findByCompetenciaCodigo(20L)).thenReturn(java.util.List.of(vinculo));
        when(atividadeRepo.findAllById(java.util.Set.of(10L))).thenReturn(java.util.List.of(atividadeVigente));

        when(repositorioCompetenciaAtividade.findByAtividadeCodigo(10L)).thenReturn(java.util.List.of(vinculo));
        when(repositorioCompetencia.findById(20L)).thenReturn(Optional.of(competencia));

        ImpactoMapaDto result = impactoMapaServico.verificarImpactos(100L);

        assertThat(result.temImpactos()).isTrue();
        assertThat(result.totalAtividadesAlteradas()).isEqualTo(1);
        assertThat(result.atividadesAlteradas()).hasSize(1);
        assertThat(result.atividadesAlteradas().getFirst().descricao()).isEqualTo("Descrição Nova");
        assertThat(result.atividadesAlteradas().getFirst().descricaoAnterior()).isEqualTo("Descrição Antiga");
        assertThat(result.competenciasImpactadas()).hasSize(1);
        assertThat(result.competenciasImpactadas().getFirst().descricao()).isEqualTo("Competência Afetada");
    }
}