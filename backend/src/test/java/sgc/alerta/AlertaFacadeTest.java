package sgc.alerta;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.data.domain.*;
import sgc.alerta.model.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;

import java.lang.reflect.*;
import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertaFacade Test")
@SuppressWarnings("NullAway.Init")
class AlertaFacadeTest {
    @Mock
    private AlertaService alertaService;
    @Mock
    private UsuarioService usuarioService;
    @Mock
    private UnidadeService unidadeService;

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

    @Test
    @DisplayName("Deve lançar erro quando unidade obrigatória estiver ausente")
    void deveLancarErroQuandoUnidadeObrigatoriaAusente() throws Exception {
        Method metodo = AlertaFacade.class.getDeclaredMethod("obterUnidadeObrigatoria", Map.class, Long.class);
        metodo.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<Long, Unidade> unidadesPorCodigo = Collections.EMPTY_MAP;

        assertThatThrownBy(() -> metodo.invoke(alertaFacade, unidadesPorCodigo, 999L))
                .isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(IllegalStateException.class)
                .hasRootCauseMessage("Unidade 999 ausente na construção de alertas");
    }

    @Test
    @DisplayName("Deve manter alerta como não lido quando leitura estiver nula")
    void deveManterAlertaNaoLidoQuandoLeituraNula() {
        String titulo = "123";
        Alerta alerta = new Alerta();
        alerta.setCodigo(1L);

        AlertaUsuario leitura = new AlertaUsuario();
        leitura.setCodigo(AlertaUsuario.Chave.builder().alertaCodigo(1L).usuarioTitulo(titulo).build());
        leitura.setDataHoraLeitura(null);

        when(alertaService.listarParaGestao(1L, titulo)).thenReturn(List.of(alerta));
        when(alertaService.alertasUsuarios(titulo, List.of(1L))).thenReturn(List.of(leitura));

        List<Alerta> resultado = alertaFacade.alertasPorUsuario(titulo, 1L, "GESTOR");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getDataHoraLeitura()).isNull();
    }

    @Test
    @DisplayName("Deve criar alertas para unidade interoperacional e cadeia superior")
    void deveCriarAlertasParaUnidadeInteroperacionalECadeiaSuperior() {
        Processo processo = new Processo();
        processo.setCodigo(10L);

        Unidade raiz = new Unidade();
        raiz.setCodigo(1L);
        raiz.setTipo(TipoUnidade.RAIZ);
        raiz.setSigla("RAIZ");

        Unidade superior = new Unidade();
        superior.setCodigo(2L);
        superior.setTipo(TipoUnidade.INTERMEDIARIA);
        superior.setSigla("SUP");
        superior.setUnidadeSuperior(raiz);

        Unidade interoperacional = new Unidade();
        interoperacional.setCodigo(3L);
        interoperacional.setTipo(TipoUnidade.INTEROPERACIONAL);
        interoperacional.setSigla("INTOP");
        interoperacional.setUnidadeSuperior(superior);

        when(unidadeService.buscarPorCodigo(1L)).thenReturn(raiz);
        when(alertaService.salvar(any(Alerta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<Alerta> alertas = alertaFacade.criarAlertasProcessoIniciado(processo, List.of(interoperacional));

        assertThat(alertas).isNotEmpty();
        assertThat(alertas).allMatch(a -> a.getProcesso() == processo);
    }

    @Test
    @DisplayName("Deve criar alerta para participante do tipo raiz")
    void deveCriarAlertaParaParticipanteRaiz() {
        Processo processo = new Processo();
        processo.setCodigo(11L);

        Unidade raiz = new Unidade();
        raiz.setCodigo(1L);
        raiz.setTipo(TipoUnidade.RAIZ);
        raiz.setSigla("RAIZ");

        when(unidadeService.buscarPorCodigo(1L)).thenReturn(raiz);
        when(alertaService.salvar(any(Alerta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<Alerta> alertas = alertaFacade.criarAlertasProcessoIniciado(processo, List.of(raiz));

        assertThat(alertas).isNotEmpty();
        assertThat(alertas).anyMatch(alerta -> alerta.getUnidadeDestino() == raiz);
    }
}
