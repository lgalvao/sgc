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

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlertaFacade - Cobertura adicional")
class AlertaFacadeCoverageTest {
    @Mock
    private AlertaService alertaService;
    @Mock
    private UsuarioService usuarioService;
    @Mock
    private UnidadeService unidadeService;

    @InjectMocks
    private AlertaFacade alertaFacade;

    @Test
    @DisplayName("criarAlertaCadastroDisponibilizado deve usar sigla da unidade de origem na descrição")
    void deveUsarSiglaDaUnidadeOrigemNaDescricao() {

        Processo processo = Processo.builder().codigo(10L).descricao("Processo teste").build();

        Unidade unidadeOrigem = new Unidade();
        unidadeOrigem.setCodigo(1L);
        unidadeOrigem.setSigla("ADMIN");

        Unidade unidadeDestino = new Unidade();
        unidadeDestino.setCodigo(2L);
        unidadeDestino.setSigla("UNIT");

        when(alertaService.salvar(any(Alerta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        alertaFacade.criarAlertaCadastroDisponibilizado(processo, unidadeOrigem, unidadeDestino);

        ArgumentCaptor<Alerta> captor = ArgumentCaptor.forClass(Alerta.class);
        verify(alertaService).salvar(captor.capture());
        assertThat(captor.getValue().getDescricao()).contains("pela unidade ADMIN");
    }

    @Test
    @DisplayName("criarAlertasProcessoIniciado deve criar alertas para unidades operacionais e intermediárias")
    void deveCriarAlertasProcessoIniciado() {
        Processo processo = Processo.builder().codigo(10L).build();

        Unidade raiz = new Unidade();
        raiz.setCodigo(1L);
        raiz.setTipo(TipoUnidade.RAIZ);

        Unidade inter = new Unidade();
        inter.setCodigo(2L);
        inter.setTipo(TipoUnidade.INTERMEDIARIA);
        inter.setUnidadeSuperior(raiz);

        Unidade oper = new Unidade();
        oper.setCodigo(3L);
        oper.setTipo(TipoUnidade.OPERACIONAL);
        oper.setUnidadeSuperior(inter);

        when(unidadeService.buscarPorCodigo(1L)).thenReturn(raiz);
        when(alertaService.salvar(any(Alerta.class))).thenAnswer(i -> i.getArgument(0));

        List<Alerta> result = alertaFacade.criarAlertasProcessoIniciado(processo, List.of(oper));

        assertThat(result).hasSize(3);
        verify(alertaService, times(3)).salvar(any(Alerta.class));
    }

    @Test
    @DisplayName("criarAlertasProcessoIniciado com interoperacional deve adicionar nas operacionais e intermediárias")
    void deveCriarAlertasProcessoIniciadoInteroperacional() {
        Processo processo = Processo.builder().codigo(10L).build();

        Unidade raiz = new Unidade();
        raiz.setCodigo(1L);
        raiz.setTipo(TipoUnidade.RAIZ);

        Unidade interop = new Unidade();
        interop.setCodigo(4L);
        interop.setTipo(TipoUnidade.INTEROPERACIONAL);
        interop.setUnidadeSuperior(raiz);

        when(unidadeService.buscarPorCodigo(1L)).thenReturn(raiz);
        when(alertaService.salvar(any(Alerta.class))).thenAnswer(i -> i.getArgument(0));

        List<Alerta> result = alertaFacade.criarAlertasProcessoIniciado(processo, List.of(interop));

        // INTEROPERACIONAL adds itself to operacionais and intermediarias.
        // Its superior (RAIZ) adds to intermediarias.
        // So we get:
        // operacionais = {4L} (1 alert)
        // intermediarias = {4L, 1L} (2 alerts)
        // Total = 3
        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("criarAlertaAdmin deve criar alerta usando a unidade raiz como origem")
    void deveCriarAlertaAdmin() {
        Processo processo = Processo.builder().codigo(10L).build();
        Unidade raiz = new Unidade(); raiz.setCodigo(1L);
        Unidade destino = new Unidade(); destino.setCodigo(2L);

        when(unidadeService.buscarPorCodigo(1L)).thenReturn(raiz);
        when(alertaService.salvar(any(Alerta.class))).thenAnswer(i -> i.getArgument(0));

        alertaFacade.criarAlertaAdmin(processo, destino, "Teste");

        ArgumentCaptor<Alerta> captor = ArgumentCaptor.forClass(Alerta.class);
        verify(alertaService).salvar(captor.capture());
        assertThat(captor.getValue().getUnidadeOrigem()).isEqualTo(raiz);
    }

    @Test
    @DisplayName("criarAlertaTransicao deve criar alerta com origem e destino informados")
    void deveCriarAlertaTransicao() {
        Processo processo = Processo.builder().codigo(10L).build();
        Unidade origem = new Unidade(); origem.setCodigo(2L);
        Unidade destino = new Unidade(); destino.setCodigo(3L);

        when(alertaService.salvar(any(Alerta.class))).thenAnswer(i -> i.getArgument(0));

        alertaFacade.criarAlertaTransicao(processo, "Teste transicao", origem, destino);

        ArgumentCaptor<Alerta> captor = ArgumentCaptor.forClass(Alerta.class);
        verify(alertaService).salvar(captor.capture());
        assertThat(captor.getValue().getUnidadeOrigem()).isEqualTo(origem);
        assertThat(captor.getValue().getUnidadeDestino()).isEqualTo(destino);
    }

    @Test
    @DisplayName("marcarComoLidos deve atualizar AlertaUsuario quando existe, mas data hora eh nula")
    void deveMarcarComoLidosAtualizandoExistente() {
        String titulo = "usuario";
        Usuario user = new Usuario(); user.setTituloEleitoral(titulo);

        when(usuarioService.buscar(titulo)).thenReturn(user);

        AlertaUsuario.Chave chave = AlertaUsuario.Chave.builder().alertaCodigo(100L).usuarioTitulo(titulo).build();
        AlertaUsuario au = new AlertaUsuario();
        au.setCodigo(chave);
        au.setDataHoraLeitura(null);

        when(alertaService.alertaUsuario(chave)).thenReturn(Optional.of(au));
        when(alertaService.salvarAlertaUsuario(any(AlertaUsuario.class))).thenAnswer(i -> i.getArgument(0));

        alertaFacade.marcarComoLidos(titulo, List.of(100L));

        verify(alertaService).salvarAlertaUsuario(au);
        assertThat(au.getDataHoraLeitura()).isNotNull();
    }

    @Test
    @DisplayName("marcarComoLidos deve criar AlertaUsuario quando não existe e alerta existe")
    void deveMarcarComoLidosCriandoNovo() {
        String titulo = "usuario";
        Usuario user = new Usuario(); user.setTituloEleitoral(titulo);

        when(usuarioService.buscar(titulo)).thenReturn(user);

        AlertaUsuario.Chave chave = AlertaUsuario.Chave.builder().alertaCodigo(100L).usuarioTitulo(titulo).build();
        when(alertaService.alertaUsuario(chave)).thenReturn(Optional.empty());

        Alerta alerta = new Alerta(); alerta.setCodigo(100L);
        when(alertaService.porCodigo(100L)).thenReturn(Optional.of(alerta));

        alertaFacade.marcarComoLidos(titulo, List.of(100L));

        ArgumentCaptor<AlertaUsuario> captor = ArgumentCaptor.forClass(AlertaUsuario.class);
        verify(alertaService).salvarAlertaUsuario(captor.capture());
        assertThat(captor.getValue().getAlerta()).isEqualTo(alerta);
        assertThat(captor.getValue().getUsuario()).isEqualTo(user);
        assertThat(captor.getValue().getDataHoraLeitura()).isNotNull();
    }

    @Test
    @DisplayName("criarAlertaCadastroDevolvido, criarAlertaAlteracaoDataLimite, reaberturas")
    void deveCriarAlertasEspecificos() {
        Processo processo = Processo.builder().codigo(10L).descricao("P1").build();
        Unidade raiz = new Unidade(); raiz.setCodigo(1L);
        Unidade destino = new Unidade(); destino.setCodigo(2L);

        when(unidadeService.buscarPorCodigo(1L)).thenReturn(raiz);
        when(alertaService.salvar(any(Alerta.class))).thenAnswer(i -> i.getArgument(0));

        alertaFacade.criarAlertaCadastroDevolvido(processo, destino, "motivo1");
        alertaFacade.criarAlertaAlteracaoDataLimite(processo, destino, "10/10", 1);
        alertaFacade.criarAlertaReaberturaCadastro(processo, destino, "justifica");
        alertaFacade.criarAlertaReaberturaCadastroSuperior(processo, raiz, destino);
        alertaFacade.criarAlertaReaberturaRevisao(processo, destino, "justifica2");
        alertaFacade.criarAlertaReaberturaRevisaoSuperior(processo, raiz, destino);

        verify(alertaService, times(6)).salvar(any(Alerta.class));
    }

    @Test
    @DisplayName("listarPorUnidade com paginacao paginada deve usar sorting")
    void deveListarPorUnidadePaginadoComSort() {
        Pageable pageable = PageRequest.of(0, 10);
        when(alertaService.listarParaGestaoPaginado(eq(1L), eq("t1"), any(Pageable.class))).thenReturn(Page.empty());

        alertaFacade.listarPorUnidade("t1", 1L, "GESTOR", pageable);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(alertaService).listarParaGestaoPaginado(eq(1L), eq("t1"), captor.capture());

        Pageable p = captor.getValue();
        assertThat(p.getSort().isSorted()).isTrue();
    }
}
