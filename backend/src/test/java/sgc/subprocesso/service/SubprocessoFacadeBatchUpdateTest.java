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
    void deveDelegarSalvarAjustesParaAjusteMapaService() {
        Long codSubprocesso = 100L;
        
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

        facade.salvarAjustesMapa(codSubprocesso, ajustes);

        verify(ajusteMapaService).salvarAjustesMapa(codSubprocesso, ajustes);
    }
}