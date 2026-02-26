package sgc.processo.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.processo.dto.*;
import sgc.processo.erros.*;
import sgc.processo.model.*;
import sgc.testutils.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessoManutencaoServiceValidationTest")
class ProcessoManutencaoServiceValidationTest {

    @Mock
    private ProcessoRepo processoRepo;
    @Mock
    private OrganizacaoFacade unidadeService;
    @Mock
    private ProcessoValidador processoValidador;
    @InjectMocks
    private ProcessoManutencaoService service;

    @Test
    @DisplayName("Deve impedir criação de processo com unidade INTERMEDIARIA")
    void deveImpedirCriacaoComUnidadeIntermediaria() {
        // Given
        CriarProcessoRequest request = CriarProcessoRequest.builder()
                .descricao("Processo Teste")
                .tipo(TipoProcesso.MAPEAMENTO)
                .dataLimiteEtapa1(LocalDateTime.now().plusDays(10))
                .unidades(List.of(1L))
                .build();

        Unidade unidadeIntermediaria = UnidadeTestBuilder.umaDe().comCodigo("1")
                .comTipo(TipoUnidade.INTERMEDIARIA)
                .build();

        when(unidadeService.unidadePorCodigo(1L)).thenReturn(unidadeIntermediaria);

        when(processoValidador.validarTiposUnidades(anyList()))
                .thenReturn(Optional.of("Unidades do tipo INTERMEDIARIA não podem participar de processos: COORD_11"));

        // When/Then
        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(ErroProcesso.class)
                .hasMessageContaining("Unidades do tipo INTERMEDIARIA não podem participar de processos");
    }
}
