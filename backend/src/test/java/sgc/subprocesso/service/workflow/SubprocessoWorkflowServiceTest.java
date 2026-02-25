package sgc.subprocesso.service.workflow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.alerta.AlertaFacade;
import sgc.alerta.EmailService;
import sgc.comum.model.ComumRepo;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.Mapa;
import sgc.mapa.MapaFacade;
import sgc.mapa.dto.ImpactoMapaResponse;
import sgc.mapa.service.ImpactoMapaService;
import sgc.mapa.service.MapaManutencaoService;
import sgc.organizacao.OrganizacaoFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.SituacaoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.AnaliseFacade;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.service.SubprocessoTransicaoService;
import sgc.subprocesso.service.SubprocessoWorkflowService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoWorkflowService")
class SubprocessoWorkflowServiceTest {
    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private SubprocessoTransicaoService transicaoService;
    @Mock
    private OrganizacaoFacade unidadeService;
    @Mock
    private AnaliseFacade analiseFacade;
    @Mock
    private MapaManutencaoService mapaManutencaoService;
    @Mock
    private ImpactoMapaService impactoMapaService;
    @Mock
    private ComumRepo repo;
    @Mock
    private AlertaFacade alertaService;
    @Mock
    private EmailService emailService;
    @Mock
    private MovimentacaoRepo movimentacaoRepo;
    @Mock
    private MapaFacade mapaFacade;
    @Mock
    private UsuarioFacade usuarioServiceFacade;

    @InjectMocks
    private SubprocessoWorkflowService service;

    @BeforeEach
    void setup() {
        service.setMapaManutencaoService(mapaManutencaoService);
        service.setSubprocessoRepo(subprocessoRepo);
        service.setMovimentacaoRepo(movimentacaoRepo);
    }

    private Unidade criarUnidade(String sigla) {
        Unidade u = new Unidade();
        u.setSigla(sigla);
        u.setSituacao(SituacaoUnidade.ATIVA);
        return u;
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
        when(unidadeService.buscarEntidadePorSigla("ADMIN")).thenReturn(criarUnidade("ADMIN"));

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

        verify(transicaoService).registrar(any());
    }

    @Test
    @DisplayName("devolverCadastro - Sucesso")
    void devolverCadastro() {
        Long id = 1L;
        Usuario user = new Usuario();
        Unidade u = criarUnidade("U1");
        Subprocesso sp = new Subprocesso();
        sp.setUnidade(u);
        sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

        when(subprocessoRepo.findByIdWithMapaAndAtividades(id)).thenReturn(Optional.of(sp));

        service.devolverCadastro(id, user, "obs");

        verify(transicaoService).registrarAnaliseETransicao(any());
    }

    @Test
    @DisplayName("aceitarCadastro - Sucesso")
    void aceitarCadastro() {
        Long id = 1L;
        Usuario user = new Usuario();
        Unidade u = criarUnidade("U1");
        Subprocesso sp = new Subprocesso();
        sp.setUnidade(u);
        sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

        when(subprocessoRepo.findByIdWithMapaAndAtividades(id)).thenReturn(Optional.of(sp));

        service.aceitarCadastro(id, user, "obs");

        verify(transicaoService).registrarAnaliseETransicao(any());
    }

    @Test
    @DisplayName("homologarCadastroEmBloco - Sucesso")
    void homologarCadastroEmBloco() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        Usuario user = new Usuario();

        when(subprocessoRepo.findByIdWithMapaAndAtividades(id)).thenReturn(Optional.of(sp));
        when(unidadeService.buscarEntidadePorSigla("ADMIN")).thenReturn(criarUnidade("ADMIN"));

        service.homologarCadastroEmBloco(List.of(id), user);

        verify(subprocessoRepo).save(sp);
    }

    @Test
    @DisplayName("homologarRevisaoCadastro - Sucesso")
    void homologarRevisaoCadastro() {
        Long id = 1L;
        Usuario user = new Usuario();
        Subprocesso sp = new Subprocesso();
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

    @Test
    @DisplayName("listarSubprocessosHomologados")
    void listarSubprocessosHomologados() {
        service.listarSubprocessosHomologados();
        verify(subprocessoRepo).findBySituacao(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
    }
}
