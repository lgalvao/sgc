package sgc.subprocesso.service.workflow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.alerta.AlertaFacade;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.MapaManutencaoService;
import sgc.organizacao.OrganizacaoFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.AnaliseFacade;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.service.SubprocessoTransicaoService;
import sgc.subprocesso.service.SubprocessoWorkflowService;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubprocessoWorkflowServiceCoverageTest {

    @Mock private SubprocessoRepo subprocessoRepo;
    @Mock private AlertaFacade alertaService;
    @Mock private OrganizacaoFacade unidadeService;
    @Mock private SubprocessoTransicaoService transicaoService;
    @Mock private AnaliseFacade analiseFacade;
    @Mock private UsuarioFacade usuarioServiceFacade;
    @Mock private MapaManutencaoService mapaManutencaoService;

    @InjectMocks
    private SubprocessoWorkflowService service;

    @BeforeEach
    void setup() {
        service.setSubprocessoRepo(subprocessoRepo);
        service.setMapaManutencaoService(mapaManutencaoService);
    }

    @Test
    @DisplayName("reabrirRevisaoCadastro com unidade superior deve gerar alertas para superiores")
    void reabrirRevisaoCadastro_ComSuperior() {
        Long codigo = 1L;
        Unidade sup = new Unidade();
        sup.setSigla("SUP");
        Unidade u = new Unidade();
        u.setSigla("U");
        u.setUnidadeSuperior(sup);

        Subprocesso sp = new Subprocesso();
        sp.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
        sp.setUnidade(u);
        sp.setMapa(new Mapa());

        when(subprocessoRepo.findByIdWithMapaAndAtividades(codigo)).thenReturn(Optional.of(sp));
        when(unidadeService.buscarEntidadePorSigla("ADMIN")).thenReturn(new Unidade());

        service.reabrirRevisaoCadastro(codigo, "Justificativa");

        verify(alertaService).criarAlertaReaberturaRevisao(any(), eq(u), eq("Justificativa"));
        verify(alertaService).criarAlertaReaberturaRevisaoSuperior(any(), eq(sup), eq(u));
    }

    @Test
    @DisplayName("disponibilizarCadastro com unidade sem superior deve logar warning")
    void disponibilizarCadastro_SemSuperior() {
        Long codigo = 1L;
        Unidade u = new Unidade();
        u.setSigla("U");
        u.setUnidadeSuperior(null);
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(codigo);
        sp.setUnidade(u);
        sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        sp.setMapa(new Mapa());

        when(subprocessoRepo.findByIdWithMapaAndAtividades(codigo)).thenReturn(Optional.of(sp));
        Atividade ativ = new Atividade();
        ativ.setConhecimentos(Set.of(new Conhecimento()));
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(any())).thenReturn(List.of(ativ));

        service.disponibilizarCadastro(codigo, new Usuario());

        // Assert that logic continued (transition registered)
        verify(transicaoService).registrar(any());
    }
}
