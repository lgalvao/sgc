package sgc.subprocesso.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.service.MapaManutencaoService;
import sgc.subprocesso.dto.AtividadeAjusteDto;
import sgc.subprocesso.dto.CompetenciaAjusteDto;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.crud.SubprocessoCrudService;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoFacade Batch Update Test")
class SubprocessoFacadeBatchUpdateTest {
    @Mock
    private MapaManutencaoService mapaManutencaoService;
    @Mock
    private sgc.subprocesso.model.SubprocessoRepo subprocessoRepo;
    @Mock
    private SubprocessoAjusteMapaService ajusteMapaService;
    @Mock
    private SubprocessoCrudService crudService;
    @Mock
    private SubprocessoAtividadeService atividadeService;
    @Mock
    private SubprocessoContextoService contextoService;
    @Mock
    private SubprocessoPermissaoCalculator permissaoCalculator;
    @Mock
    private sgc.organizacao.UsuarioFacade usuarioService;

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
        when(mapaManutencaoService.atualizarDescricoesAtividadeEmLote(any())).thenReturn(List.of(a10, a11, a12));

        Competencia c1 = new Competencia();
        c1.setCodigo(1L);
        Competencia c2 = new Competencia();
        c2.setCodigo(2L);
        when(mapaManutencaoService.buscarCompetenciasPorCodigos(any())).thenReturn(List.of(c1, c2));

        // Act
        facade.salvarAjustesMapa(codSubprocesso, ajustes);

        // Assert
        // Verify batch methods called ONCE
        verify(mapaManutencaoService, times(1)).atualizarDescricoesAtividadeEmLote(any());
        verify(mapaManutencaoService, times(1)).buscarCompetenciasPorCodigos(any());
        verify(mapaManutencaoService, times(1)).salvarTodasCompetencias(any());
        verify(subprocessoRepo, times(1)).save(subprocesso);

        // Verify singular methods NOT called (N+1 avoidance)
        verify(mapaManutencaoService, never()).obterAtividadePorCodigo(any());
        verify(mapaManutencaoService, never()).atualizarAtividade(any(), any());
        verify(mapaManutencaoService, never()).buscarCompetenciaPorCodigo(any());
        verify(mapaManutencaoService, never()).salvarCompetencia(any());
    }
}