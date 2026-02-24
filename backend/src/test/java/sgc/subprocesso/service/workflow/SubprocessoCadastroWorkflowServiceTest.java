package sgc.subprocesso.service.workflow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.alerta.AlertaFacade;
import sgc.analise.AnaliseFacade;
import sgc.comum.ComumRepo;
import sgc.mapa.dto.ImpactoMapaResponse;
import sgc.mapa.service.ImpactoMapaService;
import sgc.mapa.MapaFacade;
import sgc.organizacao.OrganizacaoFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.SituacaoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.service.crud.SubprocessoCrudService;
import sgc.subprocesso.service.crud.SubprocessoValidacaoService;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class SubprocessoCadastroWorkflowServiceTest {
    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private SubprocessoTransicaoService transicaoService;
    @Mock
    private OrganizacaoFacade unidadeService;
    @Mock
    private AnaliseFacade analiseFacade;
    @Mock
    private SubprocessoValidacaoService validacaoService;
    @Mock
    private ImpactoMapaService impactoMapaService;
    @Mock
    private ComumRepo repo;
    @Mock
    private SubprocessoCrudService crudService;
    @Mock
    private AlertaFacade alertaService;
    @Mock
    private MovimentacaoRepo movimentacaoRepo;
    @Mock
    private MapaFacade mapaFacade;
    @Mock
    private UsuarioFacade usuarioServiceFacade;

    @InjectMocks
    private SubprocessoCadastroWorkflowService service;

    private Unidade criarUnidade(String sigla) {
        Unidade u = new Unidade();
        u.setSigla(sigla);
        u.setSituacao(SituacaoUnidade.ATIVA);
        return u;
    }

    @Test
    @DisplayName("reabrirCadastro - Sucesso com loop de alertas")
    void reabrirCadastro_LoopAlertas() {
        Long codigo = 1L;
        Unidade u = criarUnidade("U1");
        Unidade sup = criarUnidade("SUP");
        u.setUnidadeSuperior(sup);
        
        Subprocesso sp = new Subprocesso();
        sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
        sp.setUnidade(u);

        when(crudService.buscarSubprocesso(codigo)).thenReturn(sp);
        when(unidadeService.buscarEntidadePorSigla("ADMIN")).thenReturn(criarUnidade("ADMIN"));

        service.reabrirCadastro(codigo, "J");

        verify(alertaService).criarAlertaReaberturaCadastro(any(), eq(u), eq("J"));
        verify(alertaService).criarAlertaReaberturaCadastroSuperior(any(), eq(sup), eq(u));
    }

    @Test
    @DisplayName("disponibilizarCadastro quando unidade superior é null")
    void disponibilizarCadastro_SuperiorNull() {
        Long id = 1L;
        Usuario user = new Usuario();
        Unidade u = criarUnidade("U1");
        u.setUnidadeSuperior(null);
        
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(id);
        sp.setUnidade(u);

        when(crudService.buscarSubprocesso(id)).thenReturn(sp);
        when(validacaoService.obterAtividadesSemConhecimento(id)).thenReturn(Collections.emptyList());

        service.disponibilizarCadastro(id, user);

        verify(transicaoService).registrar(argThat(cmd -> cmd.destino().equals(u)));
    }

    @Test
    @DisplayName("devolverCadastro quando unidade superior é null")
    void devolverCadastro_SuperiorNull() {
        Long id = 1L;
        Usuario user = new Usuario();
        Unidade u = criarUnidade("U1");
        u.setUnidadeSuperior(null);
        Subprocesso sp = new Subprocesso();
        sp.setUnidade(u);

        when(crudService.buscarSubprocesso(id)).thenReturn(sp);

        service.devolverCadastro(id, user, "obs");

        verify(transicaoService).registrarAnaliseETransicao(argThat(req -> req.unidadeAnalise().equals(u)));
    }

    @Test
    @DisplayName("aceitarCadastro quando unidade superior é null")
    void aceitarCadastro_SuperiorNull() {
        Long id = 1L;
        Usuario user = new Usuario();
        Unidade u = criarUnidade("U1");
        u.setUnidadeSuperior(null);
        Subprocesso sp = new Subprocesso();
        sp.setUnidade(u);

        when(crudService.buscarSubprocesso(id)).thenReturn(sp);

        service.aceitarCadastro(id, user, "obs");

        verify(transicaoService).registrarAnaliseETransicao(argThat(req -> req.unidadeDestinoTransicao().equals(u)));
    }

    @Test
    @DisplayName("aceitarRevisaoCadastro quando superior da analise é null")
    void aceitarRevisaoCadastro_SuperiorAnaliseNull() {
        Long id = 1L;
        Usuario user = new Usuario();
        Unidade u = criarUnidade("U1");
        u.setUnidadeSuperior(null);
        Subprocesso sp = new Subprocesso();
        sp.setUnidade(u);

        when(crudService.buscarSubprocesso(id)).thenReturn(sp);

        service.aceitarRevisaoCadastro(id, user, "obs");

        verify(transicaoService).registrarAnaliseETransicao(argThat(req -> req.unidadeDestinoTransicao().equals(u)));
    }

    @Test
    @DisplayName("aceitarCadastroEmBloco - Sucesso")
    void aceitarCadastroEmBloco() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setUnidade(criarUnidade("U1"));
        Usuario user = new Usuario();

        when(crudService.buscarSubprocesso(id)).thenReturn(sp);

        service.aceitarCadastroEmBloco(List.of(id), user);

        verify(transicaoService).registrarAnaliseETransicao(any());
    }

    @Test
    @DisplayName("homologarCadastroEmBloco - Sucesso")
    void homologarCadastroEmBloco() {
        Long id = 1L;
        Subprocesso sp = new Subprocesso();
        sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        Usuario user = new Usuario();

        when(crudService.buscarSubprocesso(id)).thenReturn(sp);
        when(unidadeService.buscarEntidadePorSigla("ADMIN")).thenReturn(criarUnidade("ADMIN"));

        service.homologarCadastroEmBloco(List.of(id), user);

        verify(subprocessoRepo).save(sp);
    }

    @Test
    @DisplayName("reabrirRevisaoCadastro - Sucesso")
    void reabrirRevisaoCadastro() {
        Long codigo = 1L;
        Unidade u = criarUnidade("U1");
        Subprocesso sp = new Subprocesso();
        sp.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
        sp.setUnidade(u);

        when(crudService.buscarSubprocesso(codigo)).thenReturn(sp);
        when(unidadeService.buscarEntidadePorSigla("ADMIN")).thenReturn(criarUnidade("ADMIN"));

        service.reabrirRevisaoCadastro(codigo, "J");

        verify(alertaService).criarAlertaReaberturaRevisao(any(), eq(u), eq("J"));
    }

    @Test
    @DisplayName("disponibilizarRevisao - Sucesso")
    void disponibilizarRevisao() {
        Long id = 1L;
        Usuario user = new Usuario();
        Unidade u = criarUnidade("U1");
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(id);
        sp.setUnidade(u);

        when(crudService.buscarSubprocesso(id)).thenReturn(sp);
        when(validacaoService.obterAtividadesSemConhecimento(id)).thenReturn(Collections.emptyList());

        service.disponibilizarRevisao(id, user);

        verify(subprocessoRepo).save(sp);
    }

    @Test
    @DisplayName("devolverRevisaoCadastro - Sucesso")
    void devolverRevisaoCadastro() {
        Long id = 1L;
        Usuario user = new Usuario();
        Unidade u = criarUnidade("U1");
        Subprocesso sp = new Subprocesso();
        sp.setUnidade(u);

        when(crudService.buscarSubprocesso(id)).thenReturn(sp);

        service.devolverRevisaoCadastro(id, user, "obs");

        verify(transicaoService).registrarAnaliseETransicao(any());
    }

    @Test
    @DisplayName("homologarRevisaoCadastro - Com Impactos")
    void homologarRevisaoCadastro_ComImpactos() {
        Long id = 1L;
        Usuario user = new Usuario();
        Subprocesso sp = new Subprocesso();
        
        when(crudService.buscarSubprocesso(id)).thenReturn(sp);
        ImpactoMapaResponse impacts = ImpactoMapaResponse.builder().temImpactos(true).build();
        when(impactoMapaService.verificarImpactos(sp, user)).thenReturn(impacts);
        when(unidadeService.buscarEntidadePorSigla("ADMIN")).thenReturn(criarUnidade("ADMIN"));

        service.homologarRevisaoCadastro(id, user, "obs");

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
        verify(subprocessoRepo).save(sp);
    }

    @Test
    @DisplayName("homologarRevisaoCadastro - Sem Impactos")
    void homologarRevisaoCadastro_SemImpactos() {
        Long id = 1L;
        Usuario user = new Usuario();
        Subprocesso sp = new Subprocesso();
        
        when(crudService.buscarSubprocesso(id)).thenReturn(sp);
        ImpactoMapaResponse impacts = ImpactoMapaResponse.builder().temImpactos(false).build();
        when(impactoMapaService.verificarImpactos(sp, user)).thenReturn(impacts);

        service.homologarRevisaoCadastro(id, user, "obs");

        assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_MAPA_HOMOLOGADO);
        verify(subprocessoRepo).save(sp);
    }

    @Test
    @DisplayName("aceitarRevisaoCadastro - Com Unidade Superior da Analise")
    void aceitarRevisaoCadastro_ComSuperiorAnalise() {
        Long id = 1L;
        Usuario user = new Usuario();
        Unidade u = criarUnidade("U1");
        Unidade sup = criarUnidade("SUP");
        Unidade supSup = criarUnidade("SUPSUP");
        u.setUnidadeSuperior(sup);
        sup.setUnidadeSuperior(supSup);
        
        Subprocesso sp = new Subprocesso();
        sp.setUnidade(u);

        when(crudService.buscarSubprocesso(id)).thenReturn(sp);

        service.aceitarRevisaoCadastro(id, user, "obs");

        verify(transicaoService).registrarAnaliseETransicao(argThat(req -> req.unidadeDestinoTransicao().equals(sup)));
    }
}

