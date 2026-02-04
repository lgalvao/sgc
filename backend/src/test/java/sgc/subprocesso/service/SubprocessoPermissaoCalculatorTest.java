package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.seguranca.acesso.Acao;
import sgc.seguranca.acesso.AccessControlService;
import sgc.subprocesso.dto.SubprocessoPermissoesDto;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.crud.SubprocessoCrudService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("SubprocessoPermissaoCalculator")
class SubprocessoPermissaoCalculatorTest {
    @Mock
    private AccessControlService accessControlService;

    @Mock
    private SubprocessoCrudService crudService;

    @InjectMocks
    private SubprocessoPermissaoCalculator calculator;

    private Usuario usuario;
    private Subprocesso subprocesso;
    private Processo processo;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setTituloEleitoral("12345678901");
        usuario.setMatricula("123456");
        usuario.setNome("Teste User");

        processo = new Processo();
        processo.setCodigo(1L);

        subprocesso = new Subprocesso();
        subprocesso.setCodigo(10L);
        subprocesso.setProcesso(processo);
    }

    @Nested
    @DisplayName("podeExecutar")
    class PodeExecutar {

        @Test
        @DisplayName("deve delegar para accessControlService")
        void deveDelegarParaAccessControlService() {
            // Arrange
            when(accessControlService.podeExecutar(usuario, Acao.VISUALIZAR_SUBPROCESSO, subprocesso))
                    .thenReturn(true);

            // Act
            boolean resultado = calculator.podeExecutar(usuario, Acao.VISUALIZAR_SUBPROCESSO, subprocesso);

            // Assert
            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("deve retornar false quando usuário não tem permissão")
        void deveRetornarFalseQuandoNaoTemPermissao() {
            // Arrange
            when(accessControlService.podeExecutar(usuario, Acao.EDITAR_MAPA, subprocesso))
                    .thenReturn(false);

            // Act
            boolean resultado = calculator.podeExecutar(usuario, Acao.EDITAR_MAPA, subprocesso);

            // Assert
            assertThat(resultado).isFalse();
        }
    }

    @Nested
    @DisplayName("obterPermissoes")
    class ObterPermissoes {

        @Test
        @DisplayName("deve buscar subprocesso e calcular permissões")
        void deveBuscarSubprocessoECalcularPermissoes() {
            // Arrange
            processo.setTipo(TipoProcesso.MAPEAMENTO);
            when(crudService.buscarSubprocesso(10L)).thenReturn(subprocesso);
            when(accessControlService.podeExecutar(any(), any(), any())).thenReturn(true);

            // Act
            SubprocessoPermissoesDto resultado = calculator.obterPermissoes(10L, usuario);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.podeVerPagina()).isTrue();
        }
    }

    @Nested
    @DisplayName("calcularPermissoes")
    class CalcularPermissoes {

        @Test
        @DisplayName("deve usar ações de MAPEAMENTO quando processo é do tipo MAPEAMENTO")
        void deveUsarAcoesMapeamento() {
            // Arrange
            processo.setTipo(TipoProcesso.MAPEAMENTO);

            // Configurar mock genérico primeiro (retorna false)
            when(accessControlService.podeExecutar(usuario, any(), subprocesso))
                    .thenReturn(false);
            
            // Depois configurar os casos específicos (retornam true) - sobrescrevem o genérico
            when(accessControlService.podeExecutar(usuario, Acao.DISPONIBILIZAR_CADASTRO, subprocesso))
                    .thenReturn(true);
            when(accessControlService.podeExecutar(usuario, Acao.DEVOLVER_CADASTRO, subprocesso))
                    .thenReturn(true);
            when(accessControlService.podeExecutar(usuario, Acao.ACEITAR_CADASTRO, subprocesso))
                    .thenReturn(true);

            // Act
            SubprocessoPermissoesDto resultado = calculator.calcularPermissoes(subprocesso, usuario);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.podeDisponibilizarCadastro()).isTrue();
            assertThat(resultado.podeDevolverCadastro()).isTrue();
            assertThat(resultado.podeAceitarCadastro()).isTrue();
        }

        @Test
        @DisplayName("deve usar ações de REVISAO quando processo é do tipo REVISAO")
        void deveUsarAcoesRevisao() {
            // Arrange
            processo.setTipo(TipoProcesso.REVISAO);

            // Configurar mock genérico primeiro (retorna false)
            when(accessControlService.podeExecutar(usuario, any(), subprocesso))
                    .thenReturn(false);
            
            // Depois configurar os casos específicos (retornam true) - sobrescrevem o genérico
            when(accessControlService.podeExecutar(usuario, Acao.DISPONIBILIZAR_REVISAO_CADASTRO, subprocesso))
                    .thenReturn(true);
            when(accessControlService.podeExecutar(usuario, Acao.DEVOLVER_REVISAO_CADASTRO, subprocesso))
                    .thenReturn(true);
            when(accessControlService.podeExecutar(usuario, Acao.ACEITAR_REVISAO_CADASTRO, subprocesso))
                    .thenReturn(true);

            // Act
            SubprocessoPermissoesDto resultado = calculator.calcularPermissoes(subprocesso, usuario);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.podeDisponibilizarCadastro()).isTrue();
            assertThat(resultado.podeDevolverCadastro()).isTrue();
            assertThat(resultado.podeAceitarCadastro()).isTrue();
        }

        @Test
        @DisplayName("deve calcular todas as permissões corretamente")
        void deveCalcularTodasPermissoes() {
            // Arrange
            processo.setTipo(TipoProcesso.MAPEAMENTO);
            when(accessControlService.podeExecutar(any(), any(), any())).thenReturn(true);

            // Act
            SubprocessoPermissoesDto resultado = calculator.calcularPermissoes(subprocesso, usuario);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.podeVerPagina()).isTrue();
            assertThat(resultado.podeEditarMapa()).isTrue();
            assertThat(resultado.podeEditarCadastro()).isTrue();
            assertThat(resultado.podeVisualizarMapa()).isTrue();
            assertThat(resultado.podeDisponibilizarMapa()).isTrue();
            assertThat(resultado.podeDisponibilizarCadastro()).isTrue();
            assertThat(resultado.podeDevolverCadastro()).isTrue();
            assertThat(resultado.podeAceitarCadastro()).isTrue();
            assertThat(resultado.podeVisualizarDiagnostico()).isTrue();
            assertThat(resultado.podeAlterarDataLimite()).isTrue();
            assertThat(resultado.podeVisualizarImpacto()).isTrue();
            assertThat(resultado.podeRealizarAutoavaliacao()).isTrue();
            assertThat(resultado.podeReabrirCadastro()).isTrue();
            assertThat(resultado.podeReabrirRevisao()).isTrue();
            assertThat(resultado.podeEnviarLembrete()).isTrue();
            assertThat(resultado.podeApresentarSugestoes()).isTrue();
            assertThat(resultado.podeValidarMapa()).isTrue();
            assertThat(resultado.podeAceitarMapa()).isTrue();
            assertThat(resultado.podeDevolverMapa()).isTrue();
            assertThat(resultado.podeHomologarMapa()).isTrue();
        }

        @Test
        @DisplayName("deve retornar todas permissões false quando usuário não tem acesso")
        void deveRetornarTodasPermissoesFalse() {
            // Arrange
            processo.setTipo(TipoProcesso.MAPEAMENTO);
            when(accessControlService.podeExecutar(any(), any(), any())).thenReturn(false);

            // Act
            SubprocessoPermissoesDto resultado = calculator.calcularPermissoes(subprocesso, usuario);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.podeVerPagina()).isFalse();
            assertThat(resultado.podeEditarMapa()).isFalse();
            assertThat(resultado.podeEditarCadastro()).isFalse();
        }
    }
}
