package sgc.mapa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import sgc.atividade.model.Atividade;
import sgc.atividade.model.AtividadeRepo;
import sgc.mapa.model.Competencia;
import sgc.mapa.dto.AtividadeImpactadaDto;
import sgc.mapa.dto.CompetenciaImpactadaDto;
import sgc.mapa.dto.ImpactoMapaDto;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaRepo;
import sgc.mapa.model.TipoImpactoAtividade;
import sgc.mapa.model.TipoImpactoCompetencia;
import sgc.mapa.service.ImpactoAtividadeService;
import sgc.mapa.service.ImpactoCompetenciaService;
import sgc.mapa.service.ImpactoMapaService;
import sgc.sgrh.model.Usuario;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.Unidade;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

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
        assertThat(result.isTemImpactos()).isFalse();
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

        assertThat(result.isTemImpactos()).isTrue();
        assertThat(result.getTotalAtividadesRemovidas()).isEqualTo(1);
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

        when(repositorioSubprocesso.findById(100L)).thenReturn(Optional.of(subprocesso));
        when(repositorioMapa.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(mapaVigente));
        when(repositorioMapa.findBySubprocessoCodigo(100L)).thenReturn(Optional.of(mapaSubprocesso));
        when(impactoAtividadeService.detectarAtividadesInseridas(List.of(atividade), List.of())).thenReturn(List.of());
        when(impactoAtividadeService.detectarAtividadesRemovidas(List.of(atividade), List.of(), mapaVigente)).thenReturn(List.of());
        when(impactoAtividadeService.detectarAtividadesAlteradas(List.of(atividade), List.of(), mapaVigente)).thenReturn(List.of());
        when(impactoCompetenciaService.identificarCompetenciasImpactadas(mapaVigente, List.of(), List.of())).thenReturn(List.of());


        ImpactoMapaDto result = impactoMapaServico.verificarImpactos(100L, usuario);

        assertThat(result.isTemImpactos()).isFalse();
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

        AtividadeImpactadaDto atividadeInseridaDto = new AtividadeImpactadaDto(
                10L,
                "Atividade Nova",
                TipoImpactoAtividade.INSERIDA,
                null,
                List.of()
        );
        List<AtividadeImpactadaDto> inseridas = List.of(atividadeInseridaDto);

        when(repositorioSubprocesso.findById(100L)).thenReturn(Optional.of(subprocesso));
        when(repositorioMapa.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(mapaVigente));
        when(repositorioMapa.findBySubprocessoCodigo(100L)).thenReturn(Optional.of(mapaSubprocesso));
        when(impactoAtividadeService.obterAtividadesDoMapa(mapaSubprocesso)).thenReturn(List.of(atividadeNova));
        when(impactoAtividadeService.obterAtividadesDoMapa(mapaVigente)).thenReturn(List.of());
        when(impactoAtividadeService.detectarAtividadesInseridas(List.of(atividadeNova), List.of())).thenReturn(inseridas);
        when(impactoAtividadeService.detectarAtividadesRemovidas(List.of(atividadeNova), List.of(), mapaVigente)).thenReturn(List.of());
        when(impactoAtividadeService.detectarAtividadesAlteradas(List.of(atividadeNova), List.of(), mapaVigente)).thenReturn(List.of());
        when(impactoCompetenciaService.identificarCompetenciasImpactadas(mapaVigente, List.of(), List.of())).thenReturn(List.of());

        ImpactoMapaDto result = impactoMapaServico.verificarImpactos(100L, usuario);

        assertThat(result.isTemImpactos()).isTrue();
        assertThat(result.getTotalAtividadesInseridas()).isEqualTo(1);
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

        AtividadeImpactadaDto atividadeAlteradaDto = new AtividadeImpactadaDto(11L, "Descrição Nova", TipoImpactoAtividade.ALTERADA, "Descrição Antiga", List.of());
        List<AtividadeImpactadaDto> alteradas = List.of(atividadeAlteradaDto);
        CompetenciaImpactadaDto competenciaImpactadaDto = new CompetenciaImpactadaDto(20L, COMPETENCIA_AFETADA, List.of("Detalhe"), TipoImpactoCompetencia.IMPACTO_GENERICO);
        List<CompetenciaImpactadaDto> competenciasImpactadas = List.of(competenciaImpactadaDto);

        when(repositorioSubprocesso.findById(100L)).thenReturn(Optional.of(subprocesso));
        when(repositorioMapa.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(mapaVigente));
        when(repositorioMapa.findBySubprocessoCodigo(100L)).thenReturn(Optional.of(mapaSubprocesso));
        when(impactoAtividadeService.obterAtividadesDoMapa(mapaSubprocesso)).thenReturn(List.of(atividadeAtual));
        when(impactoAtividadeService.obterAtividadesDoMapa(mapaVigente)).thenReturn(List.of(atividadeVigente));


        when(impactoAtividadeService.detectarAtividadesInseridas(List.of(atividadeAtual), List.of(atividadeVigente))).thenReturn(List.of());
        when(impactoAtividadeService.detectarAtividadesRemovidas(List.of(atividadeAtual), List.of(atividadeVigente), mapaVigente)).thenReturn(List.of());
        when(impactoAtividadeService.detectarAtividadesAlteradas(List.of(atividadeAtual), List.of(atividadeVigente), mapaVigente)).thenReturn(alteradas);
        when(impactoCompetenciaService.identificarCompetenciasImpactadas(mapaVigente, List.of(), alteradas)).thenReturn(competenciasImpactadas);


        ImpactoMapaDto result = impactoMapaServico.verificarImpactos(100L, usuario);

        assertThat(result.isTemImpactos()).isTrue();
        assertThat(result.getTotalAtividadesAlteradas()).isEqualTo(1);
    }
}
