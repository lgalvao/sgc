package sgc.subprocesso;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import org.mockito.junit.jupiter.MockitoExtension;

import sgc.mapa.service.MapaFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.dto.DisponibilizarMapaRequest;
import sgc.subprocesso.dto.ProcessarEmBlocoRequest;
import sgc.subprocesso.service.SubprocessoFacade;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes de Cobertura para SubprocessoMapaController")
class SubprocessoMapaControllerCoverageTest {

    @InjectMocks
    private SubprocessoMapaController controller;

    @Mock
    private SubprocessoFacade subprocessoFacade;
    @Mock
    private MapaFacade mapaFacade;
    @Mock
    private UsuarioFacade usuarioService;

    @Test
    @DisplayName("Deve usar data limite fornecida no request")
    void deveUsarDataLimiteFornecida() {
        LocalDate data = LocalDate.of(2025, 12, 31);
        ProcessarEmBlocoRequest request = new ProcessarEmBlocoRequest(
                "DISPONIBILIZAR",
                List.of(1L, 2L),
                data
        );
        Usuario usuario = new Usuario();

        controller.disponibilizarMapaEmBloco(100L, request, usuario);

        ArgumentCaptor<DisponibilizarMapaRequest> captor = ArgumentCaptor.forClass(DisponibilizarMapaRequest.class);
        verify(subprocessoFacade).disponibilizarMapaEmBloco(eq(List.of(1L, 2L)), eq(100L), captor.capture(), eq(usuario));

        assertThat(captor.getValue().dataLimite()).isEqualTo(data);
    }

    @Test
    @DisplayName("Deve usar data limite padrão (hoje + 15 dias) quando nula")
    void deveUsarDataLimitePadraoQuandoNula() {
        ProcessarEmBlocoRequest request = new ProcessarEmBlocoRequest(
                "DISPONIBILIZAR",
                List.of(1L, 2L),
                null
        );
        Usuario usuario = new Usuario();

        controller.disponibilizarMapaEmBloco(100L, request, usuario);

        ArgumentCaptor<DisponibilizarMapaRequest> captor = ArgumentCaptor.forClass(DisponibilizarMapaRequest.class);
        verify(subprocessoFacade).disponibilizarMapaEmBloco(eq(List.of(1L, 2L)), eq(100L), captor.capture(), eq(usuario));

        assertThat(captor.getValue().dataLimite()).isEqualTo(LocalDate.now().plusDays(15));
    }
}
