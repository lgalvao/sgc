package sgc.processo;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import sgc.processo.dto.IniciarProcessoRequest;
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoFacade;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes de Cobertura para ProcessoController")
class ProcessoControllerCoverageTest {

    @InjectMocks
    private ProcessoController processoController;

    @Mock
    private ProcessoFacade processoFacade;

    @Test
    @DisplayName("Deve estourar erro se processo sumir apos iniciar")
    void deveEstourarErroSeProcessoSumir() {
        Long cod = 1L;
        IniciarProcessoRequest req = new IniciarProcessoRequest(TipoProcesso.MAPEAMENTO, Collections.emptyList());

        when(processoFacade.iniciarProcessoMapeamento(cod, Collections.emptyList())).thenReturn(Collections.emptyList());
        when(processoFacade.obterPorId(cod)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> processoController.iniciar(cod, req))
                .isInstanceOf(sgc.comum.erros.ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("Deve retornar 404 se processo não encontrado")
    void deveRetornar404SeProcessoNaoEncontrado() {
        Long cod = 999L;
        when(processoFacade.obterPorId(cod)).thenReturn(Optional.empty());

        var response = processoController.obterPorId(cod);

        org.assertj.core.api.Assertions.assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    @DisplayName("Deve retornar Bad Request quando houver erros ao iniciar")
    void deveRetornarBadRequestQuandoHouverErrosAoIniciar() {
        Long cod = 1L;
        IniciarProcessoRequest req = new IniciarProcessoRequest(TipoProcesso.MAPEAMENTO, Collections.emptyList());

        when(processoFacade.iniciarProcessoMapeamento(cod, Collections.emptyList())).thenReturn(List.of("Erro 1"));

        var response = processoController.iniciar(cod, req);

        org.assertj.core.api.Assertions.assertThat(response.getStatusCode().value()).isEqualTo(400);
        org.assertj.core.api.Assertions.assertThat(response.getBody()).isInstanceOf(java.util.Map.class);
    }



    @Test
    @DisplayName("Deve retornar Bad Request se processador nao encontrado (branch defensivo)")
    void deveRetornarBadRequestSeProcessadorNaoEncontrado() {
        // Spy
        ProcessoController spy = org.mockito.Mockito.spy(new ProcessoController(processoFacade));
        @SuppressWarnings("unchecked")
        Map<TipoProcesso, java.util.function.BiFunction<Long, List<Long>, List<String>>> map = org.mockito.Mockito.mock(Map.class);
        when(map.get(any(TipoProcesso.class))).thenReturn(null);
        doReturn(map).when(spy).getProcessadoresInicio();

        Long cod = 1L;
        IniciarProcessoRequest req = new IniciarProcessoRequest(TipoProcesso.MAPEAMENTO, Collections.emptyList());

        var response = spy.iniciar(cod, req);

        org.assertj.core.api.Assertions.assertThat(response.getStatusCode().value()).isEqualTo(400);
    }
}
