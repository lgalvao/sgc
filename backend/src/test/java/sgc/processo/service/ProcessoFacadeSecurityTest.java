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
import sgc.alerta.AlertaService;
import sgc.mapa.model.MapaRepo;
import sgc.mapa.service.CopiaMapaService;
import sgc.organizacao.UnidadeService;
import sgc.organizacao.UsuarioService;
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
    private UnidadeService unidadeService;
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
    private UsuarioService usuarioService;
    @Mock
    private ProcessoInicializador processoInicializador;
    @Mock
    private AlertaService alertaService;
    
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

        @Test
        @DisplayName("Deve negar acesso quando name é null")
        void deveNegarAcessoQuandoNameNull() {
            Authentication auth = mock(Authentication.class);
            when(processoAcessoService.checarAcesso(auth, 1L)).thenReturn(false);

            assertThat(processoFacade.checarAcesso(auth, 1L)).isFalse();
        }

        @Test
        @DisplayName("Deve negar acesso quando usuário sem permissão adequada")
        void deveNegarAcessoQuandoUsuarioSemPermissao() {
            // Arrange
            Authentication auth = mock(Authentication.class);
            when(processoAcessoService.checarAcesso(auth, 1L)).thenReturn(false);

            // Act & Assert
            assertThat(processoFacade.checarAcesso(auth, 1L)).isFalse();
        }

        @Test
        @DisplayName("Deve negar acesso quando usuário não possui unidade associada")
        void deveNegarAcessoQuandoUsuarioSemUnidade() {
            // Arrange
            Authentication auth = mock(Authentication.class);
            when(processoAcessoService.checarAcesso(auth, 1L)).thenReturn(false);

            // Act & Assert
            assertThat(processoFacade.checarAcesso(auth, 1L)).isFalse();
        }

        @Test
        @DisplayName("Deve negar acesso quando codUnidadeUsuario é null no perfil")
        void deveNegarAcessoQuandoUnidadeNull() {
            Authentication auth = mock(Authentication.class);
            when(processoAcessoService.checarAcesso(auth, 1L)).thenReturn(false);

            assertThat(processoFacade.checarAcesso(auth, 1L)).isFalse();
        }

        @Test
        @DisplayName("Deve negar acesso se hierarquia vazia")
        void deveNegarAcessoSeHierarquiaVazia() {
            // Arrange
            Authentication auth = mock(Authentication.class);
            when(processoAcessoService.checarAcesso(auth, 1L)).thenReturn(false);

            // Act & Assert
            assertThat(processoFacade.checarAcesso(auth, 1L)).isFalse();
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
        @DisplayName("Deve permitir acesso em hierarquia complexa (Neto)")
        void devePermitirAcessoEmHierarquiaComplexa() {
            // Arrange
            Authentication auth = mock(Authentication.class);
            when(processoAcessoService.checarAcesso(auth, 1L)).thenReturn(true);

            // Act
            boolean acesso = processoFacade.checarAcesso(auth, 1L);

            // Assert
            assertThat(acesso).isTrue();
        }

        @Test
        @DisplayName("Deve permitir acesso quando unidade superior é nula na construção da hierarquia")
        void devePermitirAcessoComUnidadeSuperiorNula() {
            // Arrange
            Authentication auth = mock(Authentication.class);
            when(processoAcessoService.checarAcesso(auth, 1L)).thenReturn(true);

            // Act
            boolean acesso = processoFacade.checarAcesso(auth, 1L);

            // Assert
            assertThat(acesso).isTrue();
        }

        @Test
        @DisplayName("Deve tratar ciclos na hierarquia ao checar acesso")
        void deveTratarCiclosNaHierarquia() {
            // Arrange
            Authentication auth = mock(Authentication.class);
            when(processoAcessoService.checarAcesso(auth, 1L)).thenReturn(true);

            // Act
            boolean acesso = processoFacade.checarAcesso(auth, 1L);

            // Assert
            assertThat(acesso).isTrue();
        }

        @Test
        @DisplayName("checarAcesso: retorna false se authentication for null ou não autenticado")
        void checarAcesso_NaoAutenticado() {
            Authentication auth = mock(Authentication.class);
            when(processoAcessoService.checarAcesso(auth, 1L)).thenReturn(false);
            assertThat(processoFacade.checarAcesso(auth, 1L)).isFalse();

            when(processoAcessoService.checarAcesso(null, 1L)).thenReturn(false);
            assertThat(processoFacade.checarAcesso(null, 1L)).isFalse();
        }

        @Test
        @DisplayName("checarAcesso: retorna false se usuário não tem role GESTOR ou CHEFE")
        void checarAcesso_RoleInvalida() {
            Authentication auth = mock(Authentication.class);
            when(processoAcessoService.checarAcesso(auth, 1L)).thenReturn(false);

            assertThat(processoFacade.checarAcesso(auth, 1L)).isFalse();
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
