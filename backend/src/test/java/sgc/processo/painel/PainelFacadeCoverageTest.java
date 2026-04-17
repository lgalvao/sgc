package sgc.processo.painel;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.data.domain.*;
import sgc.alerta.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.processo.service.*;
import sgc.processo.dto.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PainelFacade - Cobertura de Testes")
class PainelFacadeCoverageTest {

    @InjectMocks
    private PainelFacade target;

    @Mock
    private ProcessoService processoService;
    @Mock
    private AlertaFacade alertaFacade;
    @Mock
    private UnidadeService unidadeService;
    @Mock
    private UnidadeHierarquiaService hierarquiaService;

    @Test
    @DisplayName("listarProcessos - deve cobrir processo sem participantes")
    void listarProcessos_SemParticipantes() {
        ContextoUsuarioAutenticado contexto = mock(ContextoUsuarioAutenticado.class);
        when(contexto.perfil()).thenReturn(Perfil.ADMIN);
        
        Processo p = new Processo();
        p.setCodigo(1L);
        p.setDescricao("P1");
        p.setTipo(TipoProcesso.MAPEAMENTO);
        p.setSituacao(SituacaoProcesso.CRIADO);
        p.setParticipantes(Collections.emptyList());
        
        when(processoService.listarTodos(any())).thenReturn(new PageImpl<>(List.of(p)));

        Page<ProcessoResumoDto> result = target.listarProcessos(contexto, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().unidadesParticipantes()).isEmpty();
    }
}
