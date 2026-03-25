package sgc.alerta;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.data.domain.*;
import sgc.alerta.model.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertaFacade Test")
class AlertaFacadeTest {
    @Mock
    private AlertaService alertaService;

    @InjectMocks
    private AlertaFacade alertaFacade;

    @Nested
    @DisplayName("Listagem de Alertas")
    class ListagemAlertas {

        @Test
        @DisplayName("Deve listar alertas para Servidor chamando o método expressivo correto")
        void deveListarParaServidor() {
            String titulo = "123";
            when(alertaService.listarParaServidor(titulo)).thenReturn(Collections.emptyList());

            alertaFacade.alertasPorUsuario(titulo, 1L, "SERVIDOR");

            verify(alertaService).listarParaServidor(titulo);
            verify(alertaService, never()).listarParaGestao(anyLong(), anyString());
        }

        @Test
        @DisplayName("Deve listar alertas para Gestão chamando o método expressivo correto")
        void deveListarParaGestao() {
            String titulo = "123";
            Long codUnidade = 1L;
            when(alertaService.listarParaGestao(codUnidade, titulo)).thenReturn(Collections.emptyList());

            alertaFacade.alertasPorUsuario(titulo, codUnidade, "GESTOR");

            verify(alertaService).listarParaGestao(codUnidade, titulo);
            verify(alertaService, never()).listarParaServidor(anyString());
        }

        @Test
        @DisplayName("Deve listar alertas não lidos filtrando corretamente")
        void deveListarNaoLidos() {
            String titulo = "123";
            Alerta a1 = new Alerta();
            a1.setCodigo(1L);
            Alerta a2 = new Alerta();
            a2.setCodigo(2L);

            when(alertaService.listarParaGestao(1L, titulo)).thenReturn(List.of(a1, a2));

            AlertaUsuario au1 = new AlertaUsuario();
            au1.setCodigo(AlertaUsuario.Chave.builder().alertaCodigo(1L).usuarioTitulo(titulo).build());
            au1.setDataHoraLeitura(LocalDateTime.now());

            when(alertaService.alertasUsuarios(eq(titulo), anyList())).thenReturn(List.of(au1));

            List<Alerta> resultado = alertaFacade.listarNaoLidos(titulo, 1L, "GESTOR");

            assertThat(resultado).hasSize(1);
            assertThat(resultado.getFirst().getCodigo()).isEqualTo(2L);
        }
    }

    @Nested
    @DisplayName("Paginação")
    class Paginacao {
        @Test
        @DisplayName("Listar por unidade paginado (Gestão)")
        void listarPorUnidadePaginadoGestao() {
            Pageable p = Pageable.unpaged();
            when(alertaService.listarParaGestaoPaginado(eq(1L), anyString(), any(Pageable.class))).thenReturn(Page.empty());

            alertaFacade.listarPorUnidade("123", 1L, "GESTOR", p);

            verify(alertaService).listarParaGestaoPaginado(eq(1L), anyString(), any(Pageable.class));
        }

        @Test
        @DisplayName("Listar por unidade paginado (Servidor)")
        void listarPorUnidadePaginadoServidor() {
            Pageable p = Pageable.unpaged();
            when(alertaService.listarParaServidorPaginado(anyString(), any(Pageable.class))).thenReturn(Page.empty());

            alertaFacade.listarPorUnidade("123", 1L, "SERVIDOR", p);

            verify(alertaService).listarParaServidorPaginado(anyString(), any(Pageable.class));
        }
    }
}
