package sgc.subprocesso.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.subprocesso.dto.DisponibilizarMapaRequest;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeMapa;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.subprocesso.service.crud.SubprocessoCrudService;
import sgc.subprocesso.service.factory.SubprocessoFactory;
import sgc.subprocesso.service.workflow.SubprocessoCadastroWorkflowService;
import sgc.subprocesso.service.workflow.SubprocessoMapaWorkflowService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubprocessoFacadeCoverageTest {
    @InjectMocks
    private SubprocessoFacade subprocessoFacade;

    @Mock
    private SubprocessoCrudService crudService;

    @Mock
    private SubprocessoFactory subprocessoFactory;

    @Mock
    private SubprocessoCadastroWorkflowService cadastroWorkflowService;

    @Mock
    private SubprocessoMapaWorkflowService mapaWorkflowService;

    @Test
    @DisplayName("listarPorProcessoEUnidades deve delegar para crudService")
    void listarPorProcessoEUnidades_DeveDelegar() {
        Long codProcesso = 1L;
        List<Long> unidades = List.of(2L);
        subprocessoFacade.listarPorProcessoEUnidades(codProcesso, unidades);
        verify(crudService).listarPorProcessoEUnidades(codProcesso, unidades);
    }

    @Test
    @DisplayName("criarParaDiagnostico deve delegar para factory")
    void criarParaDiagnostico_DeveDelegar() {
        Processo processo = new Processo();
        Unidade unidade = new Unidade();
        UnidadeMapa unidadeMapa = new UnidadeMapa();
        Unidade unidadeOrigem = new Unidade();
        Usuario usuario = new Usuario();
        
        subprocessoFacade.criarParaDiagnostico(processo, unidade, unidadeMapa, unidadeOrigem, usuario);
        verify(subprocessoFactory).criarParaDiagnostico(processo, unidade, unidadeMapa, unidadeOrigem, usuario);
    }

    @Test
    @DisplayName("aceitarCadastroEmBloco não faz nada se lista ids vazia")
    void aceitarCadastroEmBloco_Empty() {
        Long codProcesso = 1L;
        List<Long> unidades = List.of(10L);
        Usuario usuario = new Usuario();

        // crudService retorna empty
        when(crudService.listarPorProcessoEUnidades(codProcesso, unidades)).thenReturn(Collections.emptyList());

        subprocessoFacade.aceitarCadastroEmBloco(unidades, codProcesso, usuario);

        verify(cadastroWorkflowService, never()).aceitarCadastroEmBloco(any(), any());
    }

    @Test
    @DisplayName("homologarCadastroEmBloco não faz nada se lista ids vazia")
    void homologarCadastroEmBloco_Empty() {
        Long codProcesso = 1L;
        List<Long> unidades = List.of(10L);
        Usuario usuario = new Usuario();

        when(crudService.listarPorProcessoEUnidades(codProcesso, unidades)).thenReturn(Collections.emptyList());

        subprocessoFacade.homologarCadastroEmBloco(unidades, codProcesso, usuario);

        verify(cadastroWorkflowService, never()).homologarCadastroEmBloco(any(), any());
    }

    @Test
    @DisplayName("disponibilizarMapaEmBloco não faz nada se lista ids vazia")
    void disponibilizarMapaEmBloco_Empty() {
        Long codProcesso = 1L;
        List<Long> unidades = List.of(10L);
        Usuario usuario = new Usuario();
        DisponibilizarMapaRequest req = new DisponibilizarMapaRequest(LocalDate.now(), "Obs");

        when(crudService.listarPorProcessoEUnidades(codProcesso, unidades)).thenReturn(Collections.emptyList());

        subprocessoFacade.disponibilizarMapaEmBloco(unidades, codProcesso, req, usuario);

        verify(mapaWorkflowService, never()).disponibilizarMapaEmBloco(any(), any(), any());
    }

    @Test
    @DisplayName("aceitarValidacaoEmBloco não faz nada se lista ids vazia")
    void aceitarValidacaoEmBloco_Empty() {
        Long codProcesso = 1L;
        List<Long> unidades = List.of(10L);
        Usuario usuario = new Usuario();

        when(crudService.listarPorProcessoEUnidades(codProcesso, unidades)).thenReturn(Collections.emptyList());

        subprocessoFacade.aceitarValidacaoEmBloco(unidades, codProcesso, usuario);

        verify(mapaWorkflowService, never()).aceitarValidacaoEmBloco(any(), any());
    }

    @Test
    @DisplayName("homologarValidacaoEmBloco não faz nada se lista ids vazia")
    void homologarValidacaoEmBloco_Empty() {
        Long codProcesso = 1L;
        List<Long> unidades = List.of(10L);
        Usuario usuario = new Usuario();

        when(crudService.listarPorProcessoEUnidades(codProcesso, unidades)).thenReturn(Collections.emptyList());

        subprocessoFacade.homologarValidacaoEmBloco(unidades, codProcesso, usuario);

        verify(mapaWorkflowService, never()).homologarValidacaoEmBloco(any(), any());
    }
}
