package sgc.subprocesso.service.workflow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.dto.CompetenciaRequest;
import sgc.subprocesso.dto.DisponibilizarMapaRequest;
import sgc.subprocesso.dto.SubmeterMapaAjustadoRequest;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SubprocessoWorkflowFacadeTest {

    @Mock private SubprocessoCadastroWorkflowService cadastroService;
    @Mock private SubprocessoMapaWorkflowService mapaService;
    @Mock private SubprocessoAdminWorkflowService adminService;

    @InjectMocks
    private SubprocessoWorkflowFacade facade;

    @Test
    @DisplayName("Deve delegar operações administrativas")
    void deveDelegarAdmin() {
        Long cod = 1L;
        LocalDate data = LocalDate.now();
        facade.alterarDataLimite(cod, data);
        verify(adminService).alterarDataLimite(cod, data);

        facade.atualizarSituacaoParaEmAndamento(cod);
        verify(adminService).atualizarSituacaoParaEmAndamento(cod);

        facade.listarSubprocessosHomologados();
        verify(adminService).listarSubprocessosHomologados();
    }

    @Test
    @DisplayName("Deve delegar operações de cadastro")
    void deveDelegarCadastro() {
        Long cod = 1L;
        String just = "Just";
        Usuario usuario = new Usuario();

        facade.reabrirCadastro(cod, just);
        verify(cadastroService).reabrirCadastro(cod, just);

        facade.disponibilizarCadastro(cod, usuario);
        verify(cadastroService).disponibilizarCadastro(cod, usuario);

        facade.aceitarCadastro(cod, just, usuario);
        verify(cadastroService).aceitarCadastro(cod, just, usuario);
        
        List<Long> lista = Collections.singletonList(1L);
        facade.aceitarCadastroEmBloco(lista, 10L, usuario);
        verify(cadastroService).aceitarCadastroEmBloco(lista, usuario);
    }

    @Test
    @DisplayName("Deve delegar operações de mapa")
    void deveDelegarMapa() {
        Long cod = 1L;
        Usuario usuario = new Usuario();
        SalvarMapaRequest salvarReq = new SalvarMapaRequest(null, Collections.emptyList());
        
        facade.salvarMapaSubprocesso(cod, salvarReq);
        verify(mapaService).salvarMapaSubprocesso(cod, salvarReq);

        CompetenciaRequest compReq = new CompetenciaRequest("Comp", Collections.singletonList(1L));
        facade.adicionarCompetencia(cod, compReq);
        verify(mapaService).adicionarCompetencia(cod, compReq);

        DisponibilizarMapaRequest dispReq = new DisponibilizarMapaRequest(LocalDate.now().plusDays(1), "Obs");
        facade.disponibilizarMapa(cod, dispReq, usuario);
        verify(mapaService).disponibilizarMapa(cod, dispReq, usuario);
        
        facade.validarMapa(cod, usuario);
        verify(mapaService).validarMapa(cod, usuario);
        
        SubmeterMapaAjustadoRequest subReq = new SubmeterMapaAjustadoRequest(null, null);
        facade.submeterMapaAjustado(cod, subReq, usuario);
        verify(mapaService).submeterMapaAjustado(cod, subReq, usuario);
    }
}
