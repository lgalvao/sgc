package sgc.alerta;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sgc.alerta.model.Alerta;
import sgc.organizacao.OrganizacaoFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Unidade;
import sgc.processo.model.Processo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("AlertaFacade - Cobertura Adicional")
class AlertaFacadeCoverageTest {

    @Mock
    private AlertaService alertaService;
    @Mock
    private UsuarioFacade usuarioService;

    @Mock
    private OrganizacaoFacade unidadeService;

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
