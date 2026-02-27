package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.alerta.*;
import sgc.comum.model.*;
import sgc.mapa.dto.*;
import sgc.mapa.model.*;
import sgc.mapa.service.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoService")
class SubprocessoServiceTest {
    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private MovimentacaoRepo movimentacaoRepo;
    @Mock
    private ComumRepo repo;
    @Mock
    private AnaliseRepo analiseRepo;
    @Mock
    private AlertaFacade alertaService;
    @Mock
    private OrganizacaoFacade organizacaoFacade;
    @Mock
    private HierarquiaService hierarquiaService;
    @Mock
    private UsuarioFacade usuarioFacade;
    @Mock
    private ImpactoMapaService impactoMapaService;
    @Mock
    private CopiaMapaService copiaMapaService;
    @Mock
    private EmailService emailService;
    @Mock
    private MapaManutencaoService mapaManutencaoService;
    @InjectMocks
    private SubprocessoService service;

    @BeforeEach
    void setup() {
        service.setMapaManutencaoService(mapaManutencaoService);
        service.setSubprocessoRepo(subprocessoRepo);
        service.setMovimentacaoRepo(movimentacaoRepo);
        service.setCopiaMapaService(copiaMapaService);
    }

    private Unidade criarUnidade(Long codigo, String sigla) {
        Unidade u = new Unidade();
        u.setCodigo(codigo);
        u.setSigla(sigla);
        u.setSituacao(SituacaoUnidade.ATIVA);
        return u;
    }

    private Unidade criarUnidade(String sigla) {
        return criarUnidade(new Random().nextLong(), sigla);
    }

    @Test
    @DisplayName("alterarDataLimite - Etapa 1")
    void alterarDataLimite_Etapa1() {
        Long codigo = 1L;
        LocalDate novaData = LocalDate.of(2023, 10, 1);
        Subprocesso sp = new Subprocesso();
        sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        Unidade unidade = criarUnidade("U1");
        sp.setUnidade(unidade);
        Processo processo = new Processo();
        processo.setDescricao("Processo teste");
        sp.setProcesso(processo);

        when(subprocessoRepo.findByIdWithMapaAndAtividades(codigo)).thenReturn(Optional.of(sp));

        service.alterarDataLimite(codigo, novaData);

        verify(subprocessoRepo).save(sp);
        assertEquals(novaData.atStartOfDay(), sp.getDataLimiteEtapa1());
        verify(emailService).enviarEmail(anyString(), eq("SGC: Data limite alterada"), contains("foi alterada para"));
        verify(alertaService).criarAlertaAlteracaoDataLimite(any(), any(), any(), eq(1));
    }

    @Test
    @DisplayName("reabrirCadastro - Sucesso")
    void reabrirCadastro() {
        Long codigo = 1L;
        Unidade u = criarUnidade("U1");
        Unidade sup = criarUnidade("SUP");
        u.setUnidadeSuperior(sup);

        Subprocesso sp = new Subprocesso();
        sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        sp.setUnidade(u);

        when(subprocessoRepo.findByIdWithMapaAndAtividades(codigo)).thenReturn(Optional.of(sp));
        when(organizacaoFacade.buscarEntidadePorSigla("ADMIN")).thenReturn(criarUnidade("ADMIN"));
        when(usuarioFacade.usuarioAutenticado()).thenReturn(new Usuario());

        service.reabrirCadastro(codigo, "J");

        verify(alertaService).criarAlertaReaberturaCadastro(any(), eq(u), eq("J"));
    }

    @Test
    @DisplayName("disponibilizarCadastro - Sucesso")
    void disponibilizarCadastro() {
        Long id = 1L;
        Usuario user = new Usuario();
        Unidade u = criarUnidade("U1");

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(id);
        sp.setUnidade(u);
        sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

        Mapa m = new Mapa();
        m.setCodigo(100L);
        sp.setMapa(m);

        when(subprocessoRepo.findByIdWithMapaAndAtividades(id)).thenReturn(Optional.of(sp));
        Atividade a = new Atividade();
        a.setConhecimentos(Set.of(new Conhecimento()));
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(any())).thenReturn(List.of(a));

        service.disponibilizarCadastro(id, user);

        verify(movimentacaoRepo).save(any(Movimentacao.class));
        verify(subprocessoRepo).save(sp);
        assertEquals(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO, sp.getSituacao());
    }

    @Test
    @DisplayName("devolverCadastro - Sucesso")
    void devolverCadastro() {
        Long id = 1L;
        Usuario user = new Usuario();
        user.setTituloEleitoral("123");
        Unidade u = criarUnidade("U1");

        Subprocesso sp = new Subprocesso();
        sp.setCodigo(id);
        sp.setUnidade(u);
        sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

        when(subprocessoRepo.findByIdWithMapaAndAtividades(id)).thenReturn(Optional.of(sp));
        when(organizacaoFacade.buscarPorSigla(any())).thenReturn(new sgc.organizacao.dto.UnidadeDto());

        service.devolverCadastro(id, user, "obs");

        verify(movimentacaoRepo).save(any(Movimentacao.class));
        verify(analiseRepo).save(any(Analise.class));
    }

    @Test
    @DisplayName("aceitarCadastro - Sucesso")
    void aceitarCadastro() {
        Long id = 1L;
        Usuario user = new Usuario();
        user.setTituloEleitoral("123");
        Unidade u = criarUnidade("U1");
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(id);
        sp.setUnidade(u);
        sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

        when(subprocessoRepo.findByIdWithMapaAndAtividades(id)).thenReturn(Optional.of(sp));
        when(organizacaoFacade.buscarPorSigla(any())).thenReturn(new sgc.organizacao.dto.UnidadeDto());

        service.aceitarCadastro(id, user, "obs");

        verify(movimentacaoRepo).save(any(Movimentacao.class));
        verify(analiseRepo).save(any(Analise.class));
    }

    @Test
    @DisplayName("homologarCadastroEmBloco - Sucesso")
    void homologarCadastroEmBloco() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(id);
        sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        Usuario user = new Usuario();

        when(subprocessoRepo.findByIdWithMapaAndAtividades(id)).thenReturn(Optional.of(sp));
        when(organizacaoFacade.buscarEntidadePorSigla("ADMIN")).thenReturn(criarUnidade("ADMIN"));

        service.homologarCadastroEmBloco(List.of(id), user);

        verify(subprocessoRepo).save(sp);
        verify(movimentacaoRepo).save(any(Movimentacao.class));
    }

    @Test
    @DisplayName("homologarRevisaoCadastro - Sucesso")
    void homologarRevisaoCadastro() {
        Long id = 1L;
        Usuario user = new Usuario();
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(id);
        sp.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);

        when(subprocessoRepo.findByIdWithMapaAndAtividades(id)).thenReturn(Optional.of(sp));
        ImpactoMapaResponse impacts = ImpactoMapaResponse.builder().temImpactos(false).build();
        when(impactoMapaService.verificarImpactos(sp, user)).thenReturn(impacts);

        service.homologarRevisaoCadastro(id, user, "obs");

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_MAPA_HOMOLOGADO);
        verify(subprocessoRepo).save(sp);
    }

    @Test
    @DisplayName("atualizarSituacaoParaEmAndamento - Mapeamento")
    void atualizarParaEmAndamento_Mapeamento() {
        Long codMapa = 100L;
        Subprocesso sp = new Subprocesso();
        sp.setSituacaoForcada(SituacaoSubprocesso.NAO_INICIADO);

        Processo p = new Processo();
        p.setTipo(TipoProcesso.MAPEAMENTO);
        sp.setProcesso(p);

        when(repo.buscar(Subprocesso.class, "mapa.codigo", codMapa)).thenReturn(sp);

        service.atualizarParaEmAndamento(codMapa);

        assertEquals(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, sp.getSituacao());
        verify(subprocessoRepo).save(sp);
    }
}
