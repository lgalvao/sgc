package sgc.alerta;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.alerta.model.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertaFacade - Cobertura Adicional")
class AlertaFacadeCoverageTest {
    @Mock
    private AlertaService alertaService;

    @InjectMocks
    private AlertaFacade alertaFacade;

    @Test
    @DisplayName("criarAlertaCadastroDisponibilizado deve usar sigla da unidade de origem na descrição")
    void deveUsarSiglaDaUnidadeOrigemNaDescricao() {
        // Arrange
        Processo processo = Processo.builder().codigo(10L).descricao("Processo Teste").build();

        Unidade unidadeOrigem = new Unidade();
        unidadeOrigem.setCodigo(1L);
        unidadeOrigem.setSigla("ADMIN");

        Unidade unidadeDestino = new Unidade();
        unidadeDestino.setCodigo(2L);
        unidadeDestino.setSigla("UNIT");

        when(alertaService.salvar(any(Alerta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        alertaFacade.criarAlertaCadastroDisponibilizado(processo, unidadeOrigem, unidadeDestino);

        // Assert
        ArgumentCaptor<Alerta> captor = ArgumentCaptor.forClass(Alerta.class);
        verify(alertaService).salvar(captor.capture());
        assertThat(captor.getValue().getDescricao()).contains("pela unidade ADMIN");
    }
}
