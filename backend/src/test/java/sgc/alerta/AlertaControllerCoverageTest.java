package sgc.alerta;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import sgc.organizacao.model.Usuario;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertaControllerCoverageTest")
class AlertaControllerCoverageTest {

    @Mock
    private AlertaFacade alertaService;

    @InjectMocks
    private AlertaController controller;

    @Test
    @DisplayName("listarAlertas - Principal Usuario")
    void listarAlertas_PrincipalUsuario() {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("12345");

        controller.listarAlertas(usuario);

        verify(alertaService).listarAlertasPorUsuario("12345");
    }

    @Test
    @DisplayName("listarAlertas - Principal UserDetails")
    void listarAlertas_PrincipalUserDetails() {
        UserDetails userDetails = org.mockito.Mockito.mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("user_details");

        controller.listarAlertas(userDetails);

        verify(alertaService).listarAlertasPorUsuario("user_details");
    }

    @Test
    @DisplayName("listarAlertas - Principal Object")
    void listarAlertas_PrincipalObject() {
        Object principal = "StringPrincipal";

        controller.listarAlertas(principal);

        verify(alertaService).listarAlertasPorUsuario("StringPrincipal");
    }

    @Test
    @DisplayName("listarAlertas - Principal Unknown Object")
    void listarAlertas_PrincipalUnknownObject() {
        Object principal = new Object() {
            @Override
            public String toString() {
                return "UnknownPrincipal";
            }
        };

        controller.listarAlertas(principal);

        verify(alertaService).listarAlertasPorUsuario("UnknownPrincipal");
    }
}
