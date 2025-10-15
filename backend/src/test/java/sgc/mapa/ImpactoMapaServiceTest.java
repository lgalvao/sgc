package sgc.mapa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.competencia.modelo.Competencia;
import sgc.competencia.modelo.CompetenciaAtividade;
import sgc.competencia.modelo.CompetenciaAtividadeRepo;
import sgc.competencia.modelo.CompetenciaRepo;
import sgc.mapa.dto.AtividadeImpactadaDto;
import sgc.mapa.dto.CompetenciaImpactadaDto;
import sgc.mapa.dto.ImpactoMapaDto;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.mapa.modelo.TipoImpactoAtividade;
import sgc.mapa.modelo.TipoImpactoCompetencia;
import sgc.sgrh.Usuario;
import sgc.subprocesso.SituacaoSubprocesso;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ImpactoMapaServiceTest {
    private static final String COMPETENCIA_AFETADA = "Competência Afetada";
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

    @Mock
    private ImpactoAtividadeService impactoAtividadeService;

    @Mock
    private ImpactoCompetenciaService impactoCompetenciaService;

    @InjectMocks
    private ImpactoMapaService impactoMapaServico;

    private Subprocesso subprocesso;
    @Mock
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);

        subprocesso = new Subprocesso();
        subprocesso.setCodigo(100L);
        subprocesso.setUnidade(unidade);
        subprocesso.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);

        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_CHEFE"));
        lenient().doReturn(authorities).when(usuario).getAuthorities();
    }

    @Test
    void verificarImpactos_deveRetornarSemImpactos_quandoNaoHaMapaVigente() {
        when(repositorioSubprocesso.findById(100L)).thenReturn(Optional.of(subprocesso));
        when(repositorioMapa.findMapaVigenteByUnidade(1L)).thenReturn(Optional.empty());

        ImpactoMapaDto result = impactoMapaServico.verificarImpactos(100L, usuario);

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
        competencia.setDescricao(COMPETENCIA_AFETADA);
        competencia.setMapa(mapaVigente);

        new CompetenciaAtividade().setId(new CompetenciaAtividade.Id(10L, 20L));

        AtividadeImpactadaDto atividadeRemovidaDto = new AtividadeImpactadaDto(10L, "Atividade Antiga", TipoImpactoAtividade.REMOVIDA, null, List.of());
        List<AtividadeImpactadaDto> removidas = List.of(atividadeRemovidaDto);
        CompetenciaImpactadaDto competenciaImpactadaDto = new CompetenciaImpactadaDto(20L, COMPETENCIA_AFETADA, List.of("Detalhe"), TipoImpactoCompetencia.IMPACTO_GENERICO);
        List<CompetenciaImpactadaDto> competenciasImpactadas = List.of(competenciaImpactadaDto);

        when(repositorioSubprocesso.findById(100L)).thenReturn(Optional.of(subprocesso));
        when(repositorioMapa.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(mapaVigente));
        when(repositorioMapa.findBySubprocessoCodigo(100L)).thenReturn(Optional.of(mapaSubprocesso));
        when(atividadeRepo.findByMapaCodigo(mapaSubprocesso.getCodigo())).thenReturn(List.of());
        when(impactoAtividadeService.obterAtividadesDoMapa(mapaVigente)).thenReturn(List.of(atividadeRemovida));
        when(impactoAtividadeService.detectarAtividadesInseridas(List.of(), List.of(atividadeRemovida))).thenReturn(List.of());
        when(impactoAtividadeService.detectarAtividadesRemovidas(List.of(), List.of(atividadeRemovida), mapaVigente)).thenReturn(removidas);
        when(impactoAtividadeService.detectarAtividadesAlteradas(List.of(), List.of(atividadeRemovida), mapaVigente)).thenReturn(List.of());
        when(impactoCompetenciaService.identificarCompetenciasImpactadas(mapaVigente, removidas, List.of())).thenReturn(competenciasImpactadas);


        ImpactoMapaDto result = impactoMapaServico.verificarImpactos(100L, usuario);

        assertThat(result.temImpactos()).isTrue();
        assertThat(result.totalAtividadesRemovidas()).isEqualTo(1);
        assertThat(result.atividadesRemovidas()).hasSize(1);
        assertThat(result.atividadesRemovidas().getFirst().descricao()).isEqualTo("Atividade Antiga");
        assertThat(result.competenciasImpactadas()).hasSize(1);
        assertThat(result.competenciasImpactadas().getFirst().descricao()).isEqualTo(COMPETENCIA_AFETADA);
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

        new CompetenciaAtividade().setId(new CompetenciaAtividade.Id(10L, 20L));

        when(repositorioSubprocesso.findById(100L)).thenReturn(Optional.of(subprocesso));
        when(repositorioMapa.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(mapaVigente));
        when(repositorioMapa.findBySubprocessoCodigo(100L)).thenReturn(Optional.of(mapaSubprocesso));
        when(impactoAtividadeService.detectarAtividadesInseridas(List.of(atividade), List.of())).thenReturn(List.of());
        when(impactoAtividadeService.detectarAtividadesRemovidas(List.of(atividade), List.of(), mapaVigente)).thenReturn(List.of());
        when(impactoAtividadeService.detectarAtividadesAlteradas(List.of(atividade), List.of(), mapaVigente)).thenReturn(List.of());
        when(impactoCompetenciaService.identificarCompetenciasImpactadas(mapaVigente, List.of(), List.of())).thenReturn(List.of());


        ImpactoMapaDto result = impactoMapaServico.verificarImpactos(100L, usuario);

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

        AtividadeImpactadaDto atividadeInseridaDto = new AtividadeImpactadaDto(10L, "Atividade Nova", TipoImpactoAtividade.INSERIDA, null, List.of());
        List<AtividadeImpactadaDto> inseridas = List.of(atividadeInseridaDto);

        when(repositorioSubprocesso.findById(100L)).thenReturn(Optional.of(subprocesso));
        when(repositorioMapa.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(mapaVigente));
        when(repositorioMapa.findBySubprocessoCodigo(100L)).thenReturn(Optional.of(mapaSubprocesso));
        when(atividadeRepo.findByMapaCodigo(mapaSubprocesso.getCodigo())).thenReturn(List.of(atividadeNova));
        when(impactoAtividadeService.obterAtividadesDoMapa(mapaVigente)).thenReturn(List.of());
        when(impactoAtividadeService.detectarAtividadesInseridas(List.of(atividadeNova), List.of())).thenReturn(inseridas);
        when(impactoAtividadeService.detectarAtividadesRemovidas(List.of(atividadeNova), List.of(), mapaVigente)).thenReturn(List.of());
        when(impactoAtividadeService.detectarAtividadesAlteradas(List.of(atividadeNova), List.of(), mapaVigente)).thenReturn(List.of());
        when(impactoCompetenciaService.identificarCompetenciasImpactadas(mapaVigente, List.of(), List.of())).thenReturn(List.of());

        ImpactoMapaDto result = impactoMapaServico.verificarImpactos(100L, usuario);

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
        atividadeAtual.setCodigo(11L);
        atividadeAtual.setDescricao("Descrição Nova");
        atividadeAtual.setMapa(mapaSubprocesso);

        Competencia competencia = new Competencia();
        competencia.setCodigo(20L);
        competencia.setDescricao(COMPETENCIA_AFETADA);
        competencia.setMapa(mapaVigente);

        new CompetenciaAtividade().setId(new CompetenciaAtividade.Id(10L, 20L));

        AtividadeImpactadaDto atividadeAlteradaDto = new AtividadeImpactadaDto(11L, "Descrição Nova", TipoImpactoAtividade.ALTERADA, "Descrição Antiga", List.of());
        List<AtividadeImpactadaDto> alteradas = List.of(atividadeAlteradaDto);
        CompetenciaImpactadaDto competenciaImpactadaDto = new CompetenciaImpactadaDto(20L, COMPETENCIA_AFETADA, List.of("Detalhe"), TipoImpactoCompetencia.IMPACTO_GENERICO);
        List<CompetenciaImpactadaDto> competenciasImpactadas = List.of(competenciaImpactadaDto);

        when(repositorioSubprocesso.findById(100L)).thenReturn(Optional.of(subprocesso));
        when(repositorioMapa.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(mapaVigente));
        when(repositorioMapa.findBySubprocessoCodigo(100L)).thenReturn(Optional.of(mapaSubprocesso));
        when(atividadeRepo.findByMapaCodigo(mapaSubprocesso.getCodigo())).thenReturn(List.of(atividadeAtual));
        when(impactoAtividadeService.obterAtividadesDoMapa(mapaVigente)).thenReturn(List.of(atividadeVigente));
        AtividadeImpactadaDto atividadeRemovidaDto = new AtividadeImpactadaDto(10L, "Descrição Antiga", TipoImpactoAtividade.REMOVIDA, null, List.of());
        List<AtividadeImpactadaDto> removidas = List.of(atividadeRemovidaDto);

        when(impactoAtividadeService.detectarAtividadesInseridas(List.of(atividadeAtual), List.of(atividadeVigente))).thenReturn(alteradas);
        when(impactoAtividadeService.detectarAtividadesRemovidas(List.of(atividadeAtual), List.of(atividadeVigente), mapaVigente)).thenReturn(removidas);
        when(impactoAtividadeService.detectarAtividadesAlteradas(List.of(atividadeAtual), List.of(atividadeVigente), mapaVigente)).thenReturn(List.of());
        when(impactoCompetenciaService.identificarCompetenciasImpactadas(mapaVigente, removidas, List.of())).thenReturn(competenciasImpactadas);

        ImpactoMapaDto result = impactoMapaServico.verificarImpactos(100L, usuario);

        assertThat(result.temImpactos()).isTrue();
        assertThat(result.totalAtividadesInseridas()).isEqualTo(1);
        assertThat(result.totalAtividadesRemovidas()).isEqualTo(1);
        assertThat(result.totalAtividadesAlteradas()).isZero();
        assertThat(result.atividadesInseridas().getFirst().descricao()).isEqualTo("Descrição Nova");
        assertThat(result.atividadesRemovidas().getFirst().descricao()).isEqualTo("Descrição Antiga");
        assertThat(result.competenciasImpactadas()).hasSize(1);
        assertThat(result.competenciasImpactadas().getFirst().descricao()).isEqualTo(COMPETENCIA_AFETADA);
    }
}