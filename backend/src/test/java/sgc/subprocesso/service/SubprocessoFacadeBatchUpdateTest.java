package sgc.subprocesso.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.analise.AnaliseFacade;
import sgc.mapa.mapper.ConhecimentoMapper;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.service.AtividadeService;
import sgc.mapa.service.CompetenciaService;
import sgc.mapa.service.ConhecimentoService;
import sgc.mapa.service.MapaFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.subprocesso.dto.AtividadeAjusteDto;
import sgc.subprocesso.dto.CompetenciaAjusteDto;
import sgc.subprocesso.mapper.MapaAjusteMapper;
import sgc.subprocesso.mapper.SubprocessoDetalheMapper;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.subprocesso.service.crud.SubprocessoCrudService;
import sgc.subprocesso.service.crud.SubprocessoValidacaoService;
import sgc.subprocesso.service.workflow.SubprocessoWorkflowService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoFacade Batch Update Test")
class SubprocessoFacadeBatchUpdateTest {

    // Mocks required for SubprocessoFacade constructor
    @Mock private SubprocessoCrudService crudService;
    @Mock private SubprocessoValidacaoService validacaoService;
    @Mock private SubprocessoWorkflowService workflowService;
    @Mock private UsuarioFacade usuarioService;
    @Mock private MapaFacade mapaFacade;
    @Mock private AtividadeService atividadeService;
    @Mock private MovimentacaoRepo repositorioMovimentacao;
    @Mock private SubprocessoDetalheMapper subprocessoDetalheMapper;
    @Mock private ConhecimentoMapper conhecimentoMapper;
    @Mock private AnaliseFacade analiseFacade;
    @Mock private CompetenciaService competenciaService;
    @Mock private ConhecimentoService conhecimentoService;
    @Mock private MapaAjusteMapper mapaAjusteMapper;
    @Mock private sgc.seguranca.acesso.AccessControlService accessControlService;
    @Mock private SubprocessoRepo subprocessoRepo;
    @Mock private sgc.subprocesso.model.SubprocessoMovimentacaoRepo movimentacaoRepo;
    @Mock private sgc.mapa.service.CopiaMapaService copiaMapaService;
    @Mock private sgc.mapa.mapper.AtividadeMapper atividadeMapper;

    @InjectMocks
    private SubprocessoFacade facade;

    @Test
    void deveSalvarAjustesEmLoteEvitandoNMais1() {
        // Arrange
        Long codSubprocesso = 100L;
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(codSubprocesso);
        subprocesso.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);

        when(subprocessoRepo.findById(codSubprocesso)).thenReturn(Optional.of(subprocesso));

        CompetenciaAjusteDto comp1 = CompetenciaAjusteDto.builder()
                .codCompetencia(1L)
                .nome("Comp 1 Adjusted")
                .atividades(List.of(
                        AtividadeAjusteDto.builder().codAtividade(10L).nome("Ativ 10 Adj").build(),
                        AtividadeAjusteDto.builder().codAtividade(11L).nome("Ativ 11 Adj").build()
                ))
                .build();

        CompetenciaAjusteDto comp2 = CompetenciaAjusteDto.builder()
                .codCompetencia(2L)
                .nome("Comp 2 Adjusted")
                .atividades(List.of(
                        AtividadeAjusteDto.builder().codAtividade(12L).nome("Ativ 12 Adj").build()
                ))
                .build();

        List<CompetenciaAjusteDto> ajustes = List.of(comp1, comp2);

        // Mock batch returns
        Atividade a10 = new Atividade();
        a10.setCodigo(10L);
        Atividade a11 = new Atividade();
        a11.setCodigo(11L);
        Atividade a12 = new Atividade();
        a12.setCodigo(12L);
        when(atividadeService.atualizarDescricoesEmLote(any())).thenReturn(List.of(a10, a11, a12));

        Competencia c1 = new Competencia();
        c1.setCodigo(1L);
        Competencia c2 = new Competencia();
        c2.setCodigo(2L);
        when(competenciaService.buscarPorCodigos(any())).thenReturn(List.of(c1, c2));

        // Act
        facade.salvarAjustesMapa(codSubprocesso, ajustes);

        // Assert
        // Verify batch methods called ONCE
        verify(atividadeService, times(1)).atualizarDescricoesEmLote(any());
        verify(competenciaService, times(1)).buscarPorCodigos(any());
        verify(competenciaService, times(1)).salvarTodas(any());
        verify(subprocessoRepo, times(1)).save(subprocesso);

        // Verify singular methods NOT called (N+1 avoidance)
        verify(atividadeService, never()).obterPorCodigo(any());
        verify(atividadeService, never()).atualizar(any(), any());
        verify(competenciaService, never()).buscarPorCodigo(any());
        verify(competenciaService, never()).salvar(any());
    }
}
