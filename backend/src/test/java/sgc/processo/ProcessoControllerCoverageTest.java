package sgc.processo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import sgc.processo.dto.IniciarProcessoRequest;
import sgc.processo.model.TipoProcesso;
import sgc.processo.service.ProcessoFacade;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes de Cobertura para ProcessoController")
class ProcessoControllerCoverageTest {

    @InjectMocks
    private ProcessoController processoController;

    @Mock
    private ProcessoFacade processoFacade;

    @Test
    @DisplayName("Deve retornar 400 se tipo de processo for inválido ao iniciar")
    void deveRetornar400SeTipoInvalidoAoIniciar() {
        IniciarProcessoRequest req = new IniciarProcessoRequest(null, Collections.emptyList());
        ResponseEntity<Object> response = processoController.iniciar(1L, req);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

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
}
