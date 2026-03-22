package sgc.alerta;

import org.junit.jupiter.api.*;
import org.mockito.*;
import sgc.alerta.model.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("AlertaFacade - Cobertura adicional")
class AlertaFacadeCoverageTest {
    private AlertaService alertaService;
    private UnidadeService unidadeService;
    private UsuarioService usuarioService;
    private AlertaFacade alertaFacade;

    @BeforeEach
    void setUp() {
        alertaService = mock(AlertaService.class, withSettings().lenient());
        unidadeService = mock(UnidadeService.class, withSettings().lenient());
        usuarioService = mock(UsuarioService.class, withSettings().lenient());
        alertaFacade = new AlertaFacade(alertaService, usuarioService, unidadeService);
    }

    @Test
    @DisplayName("criarAlertaCadastroDisponibilizado deve usar sigla da unidade de origem na descrição")
    void deveUsarSiglaDaUnidadeOrigemNaDescricao() {
        Processo processo = Processo.builder().codigo(10L).descricao("Processo teste").build();
        Unidade unidadeOrigem = new Unidade();
        unidadeOrigem.setCodigo(1L);
        unidadeOrigem.setSigla("ADMIN");
        Unidade unidadeDestino = new Unidade();
        unidadeDestino.setCodigo(2L);

        when(alertaService.salvar(any(Alerta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        alertaFacade.criarAlertaCadastroDisponibilizado(processo, unidadeOrigem, unidadeDestino);

        ArgumentCaptor<Alerta> captor = ArgumentCaptor.forClass(Alerta.class);
        verify(alertaService).salvar(captor.capture());
        assertThat(captor.getValue().getDescricao()).contains("pela unidade ADMIN");
    }

    @Test
    @DisplayName("criarAlertasProcessoIniciado deve criar alertas para unidades interoperacionais")
    void deveCriarAlertasParaUnidadesInteroperacionais() {
        Processo processo = Processo.builder().codigo(10L).build();
        
        Unidade unidadeInteroperacional = new Unidade();
        unidadeInteroperacional.setCodigo(200L);
        unidadeInteroperacional.setTipo(TipoUnidade.INTEROPERACIONAL);
        
        Unidade raiz = new Unidade();
        raiz.setCodigo(1L);
        when(unidadeService.buscarPorCodigo(1L)).thenReturn(raiz);

        alertaFacade.criarAlertasProcessoIniciado(processo, List.of(unidadeInteroperacional));

        // Deve criar 2 alertas (operacional e intermediária)
        verify(alertaService, times(2)).salvar(any(Alerta.class));
    }

    @Test
    @DisplayName("criarAlertasProcessoIniciado deve criar alertas para unidades intermediárias")
    void deveCriarAlertasParaUnidadesIntermediarias() {
        Processo processo = Processo.builder().codigo(10L).build();
        
        Unidade unidadeIntermediaria = new Unidade();
        unidadeIntermediaria.setCodigo(100L);
        unidadeIntermediaria.setTipo(TipoUnidade.INTERMEDIARIA);
        
        Unidade raiz = new Unidade();
        raiz.setCodigo(1L);
        when(unidadeService.buscarPorCodigo(1L)).thenReturn(raiz);

        alertaFacade.criarAlertasProcessoIniciado(processo, List.of(unidadeIntermediaria));

        verify(alertaService, atLeastOnce()).salvar(any(Alerta.class));
    }

    @Test
    @DisplayName("criarAlertaCadastroDevolvido deve formatar descrição corretamente")
    void deveCriarAlertaCadastroDevolvido() {
        Processo processo = Processo.builder().descricao("P1").build();
        Unidade destino = new Unidade();
        destino.setCodigo(2L);
        Unidade raiz = new Unidade();
        raiz.setCodigo(1L);
        when(unidadeService.buscarPorCodigo(1L)).thenReturn(raiz);

        alertaFacade.criarAlertaCadastroDevolvido(processo, destino, "Erro fatal");

        ArgumentCaptor<Alerta> captor = ArgumentCaptor.forClass(Alerta.class);
        verify(alertaService).salvar(captor.capture());
        assertThat(captor.getValue().getDescricao()).contains("P1").contains("Erro fatal");
    }

    @Test
    @DisplayName("marcarComoLidos deve atualizar data de leitura se AlertaUsuario já existir e não estiver lido")
    void deveAtualizarDataLeituraSeExistirENaoLido() {
        String usuario = "user1";
        List<Long> codigos = List.of(100L);
        
        AlertaUsuario au = new AlertaUsuario();
        au.setDataHoraLeitura(null);
        when(alertaService.alertaUsuario(any())).thenReturn(Optional.of(au));

        alertaFacade.marcarComoLidos(usuario, codigos);

        assertThat(au.getDataHoraLeitura()).isNotNull();
        verify(alertaService).salvarAlertaUsuario(au);
    }

    @Test
    @DisplayName("marcarComoLidos não deve atualizar data de leitura se AlertaUsuario já estiver lido")
    void naoDeveAtualizarDataLeituraSeJaLido() {
        String usuario = "user1";
        List<Long> codigos = List.of(100L);
        
        LocalDateTime leituraAnterior = LocalDateTime.now().minusDays(1);
        AlertaUsuario au = new AlertaUsuario();
        au.setDataHoraLeitura(leituraAnterior);
        when(alertaService.alertaUsuario(any())).thenReturn(Optional.of(au));

        alertaFacade.marcarComoLidos(usuario, codigos);

        assertThat(au.getDataHoraLeitura()).isEqualTo(leituraAnterior);
        verify(alertaService, never()).salvarAlertaUsuario(au);
    }

    @Test
    @DisplayName("criarAlertaReaberturaCadastroSuperior deve criar alerta com sigla da subordinada")
    void deveCriarAlertaReaberturaCadastroSuperior() {
        Processo processo = Processo.builder().build();
        Unidade superior = new Unidade();
        superior.setCodigo(3L);
        Unidade subordinada = new Unidade();
        subordinada.setCodigo(4L);
        subordinada.setSigla("SUB");
        
        Unidade raiz = new Unidade();
        raiz.setCodigo(1L);
        when(unidadeService.buscarPorCodigo(1L)).thenReturn(raiz);

        alertaFacade.criarAlertaReaberturaCadastroSuperior(processo, superior, subordinada);

        ArgumentCaptor<Alerta> captor = ArgumentCaptor.forClass(Alerta.class);
        verify(alertaService).salvar(captor.capture());
        assertThat(captor.getValue().getDescricao()).contains("Cadastro da SUB reaberto");
    }

    @Test
    @DisplayName("criarAlertaReaberturaRevisao deve conter justificativa")
    void deveCriarAlertaReaberturaRevisao() {
        Processo processo = Processo.builder().build();
        Unidade unidade = new Unidade();
        unidade.setCodigo(5L);
        unidade.setSigla("U1");
        Unidade raiz = new Unidade();
        raiz.setCodigo(1L);
        when(unidadeService.buscarPorCodigo(1L)).thenReturn(raiz);

        alertaFacade.criarAlertaReaberturaRevisao(processo, unidade, "Ajuste necessário");

        ArgumentCaptor<Alerta> captor = ArgumentCaptor.forClass(Alerta.class);
        verify(alertaService).salvar(captor.capture());
        assertThat(captor.getValue().getDescricao()).contains("Justificativa: Ajuste necessário");
    }

    @Test
    @DisplayName("criarAlertaReaberturaRevisaoSuperior deve criar alerta informativo")
    void deveCriarAlertaReaberturaRevisaoSuperior() {
        Processo processo = Processo.builder().build();
        Unidade superior = new Unidade();
        superior.setCodigo(6L);
        Unidade subordinada = new Unidade();
        subordinada.setCodigo(7L);
        subordinada.setSigla("SUB");
        Unidade raiz = new Unidade();
        raiz.setCodigo(1L);
        when(unidadeService.buscarPorCodigo(1L)).thenReturn(raiz);

        alertaFacade.criarAlertaReaberturaRevisaoSuperior(processo, superior, subordinada);

        ArgumentCaptor<Alerta> captor = ArgumentCaptor.forClass(Alerta.class);
        verify(alertaService).salvar(captor.capture());
        assertThat(captor.getValue().getDescricao()).contains("Revisão de cadastro da unidade SUB reaberta");
    }
}
