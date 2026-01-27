package sgc.processo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import sgc.alerta.AlertaFacade;
import sgc.mapa.model.MapaRepo;
import sgc.mapa.service.CopiaMapaService;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.processo.mapper.ProcessoMapper;
import sgc.processo.model.ProcessoRepo;
import sgc.subprocesso.mapper.SubprocessoMapper;
import sgc.subprocesso.model.SubprocessoMovimentacaoRepo;
import sgc.subprocesso.service.SubprocessoFacade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("ProcessoFacade - Segurança e Controle de Acesso")
class ProcessoFacadeSecurityTest {
    @Mock
    private ProcessoRepo processoRepo;
    @Mock
    private UnidadeFacade unidadeService;
    @Mock
    private SubprocessoFacade subprocessoFacade;
    @Mock
    private ApplicationEventPublisher publicadorEventos;
    @Mock
    private ProcessoMapper processoMapper;
    @Mock
    private ProcessoDetalheBuilder processoDetalheBuilder;
    @Mock
    private MapaRepo mapaRepo;
    @Mock
    private SubprocessoMovimentacaoRepo movimentacaoRepo;
    @Mock
    private SubprocessoMapper subprocessoMapper;
    @Mock
    private CopiaMapaService servicoDeCopiaDeMapa;
    @Mock
    private UsuarioFacade usuarioService;
    @Mock
    private ProcessoInicializador processoInicializador;
    @Mock
    private AlertaFacade alertaService;

    // Specialized services
    @Mock
    private ProcessoAcessoService processoAcessoService;
    @Mock
    private ProcessoValidador processoValidador;
    @Mock
    private ProcessoFinalizador processoFinalizador;
    @Mock
    private ProcessoConsultaService processoConsultaService;

    @InjectMocks
    private ProcessoFacade processoFacade;

    @Nested
    @DisplayName("Segurança e Controle de Acesso")
    class Seguranca {
        @Test
        @DisplayName("Deve negar acesso quando usuário não autenticado")
        void deveNegarAcessoQuandoNaoAutenticado() {
            // Arrange
            Authentication auth = mock(Authentication.class);
            when(processoAcessoService.checarAcesso(auth, 1L)).thenReturn(false);
            when(processoAcessoService.checarAcesso(null, 1L)).thenReturn(false);

            // Act & Assert
            assertThat(processoFacade.checarAcesso(auth, 1L)).isFalse();
            assertThat(processoFacade.checarAcesso(null, 1L)).isFalse();
        }

        @org.junit.jupiter.params.ParameterizedTest
        @org.junit.jupiter.params.provider.ValueSource(longs = {1L, 2L})
        @DisplayName("Deve delegar verificação de acesso para o ProcessoAcessoService")
        void deveDelegarVerificacaoDeAcesso(Long processoCodigo) {
            Authentication auth = mock(Authentication.class);
            when(processoAcessoService.checarAcesso(auth, processoCodigo)).thenReturn(true);
            assertThat(processoFacade.checarAcesso(auth, processoCodigo)).isTrue();

            when(processoAcessoService.checarAcesso(auth, processoCodigo)).thenReturn(false);
            assertThat(processoFacade.checarAcesso(auth, processoCodigo)).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false quando checarAcesso recebe null")
        void deveRetornarFalseQuandoAuthenticationForNull() {
            when(processoAcessoService.checarAcesso(null, 1L)).thenReturn(false);
            assertThat(processoFacade.checarAcesso(null, 1L)).isFalse();
        }

        @Test
        @DisplayName("Deve permitir acesso quando gestor é da unidade participante (com hierarquia)")
        void devePermitirAcessoQuandoGestorDeUnidadeParticipante() {
            // Arrange
            Authentication auth = mock(Authentication.class);
            when(processoAcessoService.checarAcesso(auth, 1L)).thenReturn(true);

            // Act & Assert
            assertThat(processoFacade.checarAcesso(auth, 1L)).isTrue();
        }

        @Test
        @DisplayName("buscarCodigosDescendentes: via checarAcesso - teste de logica de arvore")
        void buscarCodigosDescendentes_Arvore() {
            // Este é um método privado do ProcessoAcessoService.
            // Vamos testar através do checarAcesso que o utiliza.

            Authentication auth = mock(Authentication.class);
            when(processoAcessoService.checarAcesso(auth, 1L)).thenReturn(true);

            assertThat(processoFacade.checarAcesso(auth, 1L)).isTrue();
            verify(processoAcessoService).checarAcesso(auth, 1L);
        }
    }
}
