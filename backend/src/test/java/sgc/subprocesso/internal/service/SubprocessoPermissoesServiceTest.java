package sgc.subprocesso.internal.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import sgc.atividade.internal.model.AtividadeRepo;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.mapa.internal.model.Mapa;
import sgc.processo.internal.model.Processo;
import sgc.processo.internal.model.TipoProcesso;
import sgc.sgrh.internal.model.Perfil;
import sgc.sgrh.internal.model.Usuario;
import sgc.sgrh.internal.model.UsuarioPerfil;
import sgc.subprocesso.api.SubprocessoPermissoesDto;
import sgc.subprocesso.internal.model.SituacaoSubprocesso;
import sgc.subprocesso.internal.model.Subprocesso;
import sgc.unidade.internal.model.Unidade;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoPermissoesService")
class SubprocessoPermissoesServiceTest {

    @Mock
    private AtividadeRepo atividadeRepo;

    @InjectMocks
    private SubprocessoPermissoesService service;

    @Nested
    @DisplayName("Validação de Acesso")
    class Validacao {
        
        @Test
        @DisplayName("Deve validar acesso da unidade com sucesso")
        void deveValidarAcessoUnidadeComSucesso() {
            // Arrange
            Subprocesso sub = mock(Subprocesso.class);
            Unidade unidade = mock(Unidade.class);
            when(unidade.getCodigo()).thenReturn(1L);
            when(sub.getUnidade()).thenReturn(unidade);
            when(sub.getSituacao()).thenReturn(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);

            // Act & Assert
            assertDoesNotThrow(() -> service.validar(sub, 1L, "ENVIAR_REVISAO"));
        }

        @Test
        @DisplayName("Deve lançar erro quando unidade sem acesso")
        void deveLancarErroQuandoUnidadeSemAcesso() {
            // Arrange
            Subprocesso sub = mock(Subprocesso.class);
            Unidade unidade = mock(Unidade.class);
            when(unidade.getCodigo()).thenReturn(1L);
            when(sub.getUnidade()).thenReturn(unidade);

            // Act & Assert
            assertThatThrownBy(() -> service.validar(sub, 2L, "QUALQUER_ACAO"))
                    .isInstanceOf(ErroAccessoNegado.class)
                    .hasMessageContaining("Unidade '2' sem acesso a este subprocesso (Unidade do Subprocesso: '1')")
                    .hasNoCause();
        }

        @ParameterizedTest
        @CsvSource({
            "ENVIAR_REVISAO",
            "AJUSTAR_MAPA"
        })
        @DisplayName("Deve lançar erro quando situação inválida para ação específica")
        void deveLancarErroQuandoSituacaoInvalidaParaAcao(String acao) {
            // Arrange
            Subprocesso sub = mock(Subprocesso.class);
            Unidade unidade = mock(Unidade.class);
            when(unidade.getCodigo()).thenReturn(1L);
            when(sub.getUnidade()).thenReturn(unidade);
            when(sub.getSituacao()).thenReturn(SituacaoSubprocesso.NAO_INICIADO);

            // Act & Assert
            assertThatThrownBy(() -> service.validar(sub, 1L, acao))
                    .isInstanceOf(ErroAccessoNegado.class)
                    .hasMessageContaining("Ação '" + acao + "' inválida")
                    .hasNoCause();
        }

        @Test
        @DisplayName("Deve lançar erro quando mapa vazio ao ajustar mapa em situação revisão mapa ajustado")
        void deveLancarErroQuandoMapaVazioAoAjustarMapaEmSituacaoRevisaoMapaAjustado() {
            // Arrange
            Subprocesso sub = mock(Subprocesso.class);
            Unidade unidade = mock(Unidade.class);
            when(unidade.getCodigo()).thenReturn(1L);
            when(sub.getUnidade()).thenReturn(unidade);
            when(sub.getCodigo()).thenReturn(123L);
            when(sub.getSituacao()).thenReturn(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
            
            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sub.getMapa()).thenReturn(mapa);
            
            when(atividadeRepo.countByMapaCodigo(10L)).thenReturn(0L);

            // Act & Assert
            assertThatThrownBy(() -> service.validar(sub, 1L, "AJUSTAR_MAPA"))
                    .isInstanceOf(ErroAccessoNegado.class)
                    .hasMessageContaining("mapa do subprocesso '123' está vazio")
                    .hasNoCause();
        }

        @Test
        @DisplayName("Deve validar ajuste de mapa com sucesso quando mapa tem atividades")
        void deveValidarAjusteMapaComSucesso() {
            // Arrange
            Subprocesso sub = mock(Subprocesso.class);
            Unidade unidade = mock(Unidade.class);
            when(unidade.getCodigo()).thenReturn(1L);
            when(sub.getUnidade()).thenReturn(unidade);
            when(sub.getSituacao()).thenReturn(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
            
            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sub.getMapa()).thenReturn(mapa);
            
            when(atividadeRepo.countByMapaCodigo(10L)).thenReturn(5L);

            // Act & Assert
            assertDoesNotThrow(() -> service.validar(sub, 1L, "AJUSTAR_MAPA"));
        }

        @Test
        @DisplayName("Deve validar ajuste de mapa quando situação é REVISAO_CADASTRO_HOMOLOGADA")
        void deveValidarAjusteMapaQuandoSituacaoRevisaoCadastroHomologada() {
            // Arrange
            Subprocesso sub = mock(Subprocesso.class);
            Unidade unidade = mock(Unidade.class);
            when(unidade.getCodigo()).thenReturn(1L);
            when(sub.getUnidade()).thenReturn(unidade);
            when(sub.getSituacao()).thenReturn(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);

            // Act & Assert
            assertDoesNotThrow(() -> service.validar(sub, 1L, "AJUSTAR_MAPA"));
        }

        @Test
        @DisplayName("Deve validar ajuste de mapa quando situação é REVISAO_MAPA_AJUSTADO")
        void deveValidarAjusteMapaQuandoSituacaoRevisaoMapaAjustado() {
            // Arrange
            Subprocesso sub = mock(Subprocesso.class);
            Unidade unidade = mock(Unidade.class);
            when(unidade.getCodigo()).thenReturn(1L);
            when(sub.getUnidade()).thenReturn(unidade);
            when(sub.getSituacao()).thenReturn(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);

            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sub.getMapa()).thenReturn(mapa);
            when(atividadeRepo.countByMapaCodigo(10L)).thenReturn(1L); // Mapa não vazio

            // Act & Assert
            assertDoesNotThrow(() -> service.validar(sub, 1L, "AJUSTAR_MAPA"));
        }
    }

    @Nested
    @DisplayName("Cálculo de Permissões por Perfil")
    class PermissoesPorPerfil {
        
        @Test
        @DisplayName("Deve permitir edição de mapa para ADMIN em situação válida")
        void devePermitirEdicaoMapaParaAdmin() {
            // Arrange
            Usuario admin = criarUsuarioComPerfil(Perfil.ADMIN, null);
            Subprocesso sub = criarSubprocesso(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

            // Act
            SubprocessoPermissoesDto permissoes = service.calcularPermissoes(sub, admin);

            // Assert
            assertThat(permissoes.isPodeEditarMapa()).isTrue();
            assertThat(permissoes.isPodeAlterarDataLimite()).isTrue();
            assertThat(permissoes.isPodeDevolverCadastro()).isTrue();
            assertThat(permissoes.isPodeAceitarCadastro()).isTrue();
        }

        @Test
        @DisplayName("Deve permitir edição de mapa para GESTOR da mesma unidade")
        void devePermitirEdicaoMapaParaGestorMesmaUnidade() {
            // Arrange
            Unidade unidade = criarUnidade(10L, null);
            Usuario gestor = criarUsuarioComPerfil(Perfil.GESTOR, unidade);
            Subprocesso sub = criarSubprocessoComUnidade(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, unidade);

            // Act
            SubprocessoPermissoesDto permissoes = service.calcularPermissoes(sub, gestor);

            // Assert
            assertThat(permissoes.isPodeEditarMapa()).isTrue();
            assertThat(permissoes.isPodeAlterarDataLimite()).isFalse();
            assertThat(permissoes.isPodeDevolverCadastro()).isTrue();
        }

        @Test
        @DisplayName("Deve permitir edição de mapa para CHEFE da mesma unidade")
        void devePermitirEdicaoMapaParaChefeMesmaUnidade() {
            // Arrange
            Unidade unidade = criarUnidade(10L, null);
            Usuario chefe = criarUsuarioComPerfil(Perfil.CHEFE, unidade);
            Subprocesso sub = criarSubprocessoComUnidade(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, unidade);

            // Act
            SubprocessoPermissoesDto permissoes = service.calcularPermissoes(sub, chefe);

            // Assert
            assertThat(permissoes.isPodeEditarMapa()).isTrue();
            assertThat(permissoes.isPodeAlterarDataLimite()).isFalse();
            assertThat(permissoes.isPodeDevolverCadastro()).isFalse();
        }

        @Test
        @DisplayName("Deve permitir edição de mapa para SERVIDOR da mesma unidade")
        void devePermitirEdicaoMapaParaServidorMesmaUnidade() {
            // Arrange
            Unidade unidade = criarUnidade(10L, null);
            Usuario servidor = criarUsuarioComPerfil(Perfil.SERVIDOR, unidade);
            Subprocesso sub = criarSubprocessoComUnidade(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, unidade);

            // Act
            SubprocessoPermissoesDto permissoes = service.calcularPermissoes(sub, servidor);

            // Assert
            assertThat(permissoes.isPodeEditarMapa()).isTrue();
            assertThat(permissoes.isPodeAlterarDataLimite()).isFalse();
        }

        @Test
        @DisplayName("Não deve permitir edição de mapa para SERVIDOR de outra unidade")
        void naoDevePermitirEdicaoParaServidorOutraUnidade() {
            // Arrange
            Unidade unidadeServidor = criarUnidade(20L, null);
            Unidade unidadeSubprocesso = criarUnidade(10L, null);
            Usuario servidor = criarUsuarioComPerfil(Perfil.SERVIDOR, unidadeServidor);
            Subprocesso sub = criarSubprocessoComUnidade(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, unidadeSubprocesso);

            // Act
            SubprocessoPermissoesDto permissoes = service.calcularPermissoes(sub, servidor);

            // Assert
            assertThat(permissoes.isPodeEditarMapa()).isFalse();
        }

        @Test
        @DisplayName("Deve permitir GESTOR acessar unidade subordinada")
        void devePermitirGestorAcessarUnidadeSubordinada() {
            // Arrange
            Unidade unidadePai = criarUnidade(1L, null);
            Unidade unidadeFilha = criarUnidade(10L, unidadePai);
            Usuario gestor = criarUsuarioComPerfil(Perfil.GESTOR, unidadePai);
            Subprocesso sub = criarSubprocessoComUnidade(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, unidadeFilha);

            // Act
            SubprocessoPermissoesDto permissoes = service.calcularPermissoes(sub, gestor);

            // Assert
            assertThat(permissoes.isPodeEditarMapa()).isTrue();
        }

        @Test
        @DisplayName("Gestor não deve ter acesso quando unidade não é subordinada na hierarquia")
        void gestorNaoDeveAcessarUnidadeNaoSubordinadaComHierarquia() {
            // Arrange - Hierarquia: avo -> pai -> filha
            // Gestor está em 'tio' (irmão do pai) -> não é superior da filha
            Unidade avo = criarUnidade(1L, null);
            Unidade pai = criarUnidade(2L, avo);
            Unidade tio = criarUnidade(4L, avo);
            Unidade filha = criarUnidade(3L, pai);

            Usuario gestor = criarUsuarioComPerfil(Perfil.GESTOR, tio);
            Subprocesso sub = criarSubprocessoComUnidade(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, filha);

            // Act
            SubprocessoPermissoesDto permissoes = service.calcularPermissoes(sub, gestor);

            // Assert
            assertThat(permissoes.isPodeEditarMapa()).isFalse();
        }
    }

    @Nested
    @DisplayName("Cálculo de Permissões por Situação")
    class PermissoesPorSituacao {
        
        @ParameterizedTest
        @EnumSource(value = SituacaoSubprocesso.class, names = {
            "NAO_INICIADO",
            "MAPEAMENTO_CADASTRO_EM_ANDAMENTO",
            "MAPEAMENTO_CADASTRO_HOMOLOGADO",
            "MAPEAMENTO_MAPA_CRIADO",
            "MAPEAMENTO_MAPA_COM_SUGESTOES",
            "REVISAO_CADASTRO_EM_ANDAMENTO",
            "REVISAO_CADASTRO_HOMOLOGADA",
            "REVISAO_MAPA_AJUSTADO",
            "REVISAO_MAPA_COM_SUGESTOES",
            "DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO"
        })
        @DisplayName("Deve permitir edição em situações válidas")
        void devePermitirEdicaoEmSituacoesValidas(SituacaoSubprocesso situacao) {
            // Arrange
            Usuario admin = criarUsuarioComPerfil(Perfil.ADMIN, null);
            Subprocesso sub = criarSubprocesso(situacao);

            // Act
            SubprocessoPermissoesDto permissoes = service.calcularPermissoes(sub, admin);

            // Assert
            assertThat(permissoes.isPodeEditarMapa()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = SituacaoSubprocesso.class, names = {
            "MAPEAMENTO_MAPA_VALIDADO",
            "MAPEAMENTO_MAPA_HOMOLOGADO",
            "REVISAO_MAPA_VALIDADO",
            "REVISAO_MAPA_HOMOLOGADO"
        })
        @DisplayName("Não deve permitir edição em situações finalizadas")
        void naoDevePermitirEdicaoEmSituacoesFinalizadas(SituacaoSubprocesso situacao) {
            // Arrange
            Usuario admin = criarUsuarioComPerfil(Perfil.ADMIN, null);
            Subprocesso sub = criarSubprocesso(situacao);

            // Act
            SubprocessoPermissoesDto permissoes = service.calcularPermissoes(sub, admin);

            // Assert
            assertThat(permissoes.isPodeEditarMapa()).isFalse();
        }

        @Test
        @DisplayName("Deve permitir autoavaliação apenas em situação correta")
        void devePermitirAutoavaliacaoApenasEmSituacaoCorreta() {
            // Arrange
            Usuario admin = criarUsuarioComPerfil(Perfil.ADMIN, null);
            Subprocesso subCorreto = criarSubprocesso(SituacaoSubprocesso.DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO);
            Subprocesso subIncorreto = criarSubprocesso(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

            // Act
            SubprocessoPermissoesDto permissoesCorretas = service.calcularPermissoes(subCorreto, admin);
            SubprocessoPermissoesDto permissoesIncorretas = service.calcularPermissoes(subIncorreto, admin);

            // Assert
            assertThat(permissoesCorretas.isPodeRealizarAutoavaliacao()).isTrue();
            assertThat(permissoesIncorretas.isPodeRealizarAutoavaliacao()).isFalse();
        }
    }

    @Nested
    @DisplayName("Visualização de Impacto")
    class VisualizacaoImpacto {
        
        @Test
        @DisplayName("Deve permitir visualizar impacto para Admin em situação correta")
        void devePermitirVisualizarImpactoParaAdminEmSituacaoCorreta() {
            // Arrange
            Usuario admin = criarUsuarioComPerfil(Perfil.ADMIN, null);
            Subprocesso sub = criarSubprocesso(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);

            // Act
            SubprocessoPermissoesDto permissoes = service.calcularPermissoes(sub, admin);

            // Assert
            assertThat(permissoes.isPodeVisualizarImpacto()).isTrue();
        }

        @Test
        @DisplayName("Não deve permitir visualizar impacto para Admin em situação incorreta")
        void naoDevePermitirVisualizarImpactoParaAdminEmSituacaoIncorreta() {
            // Arrange
            Usuario admin = criarUsuarioComPerfil(Perfil.ADMIN, null);
            Subprocesso sub = criarSubprocesso(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);

            // Act
            SubprocessoPermissoesDto permissoes = service.calcularPermissoes(sub, admin);

            // Assert
            assertThat(permissoes.isPodeVisualizarImpacto()).isFalse();
        }

        @Test
        @DisplayName("Deve permitir visualizar impacto em revisão para usuário não iniciado")
        void devePermitirVisualizarImpactoEmRevisaoNaoIniciado() {
            // Arrange
            Usuario admin = criarUsuarioComPerfil(Perfil.ADMIN, null);
            Subprocesso sub = criarSubprocessoRevisao(SituacaoSubprocesso.NAO_INICIADO);

            // Act
            SubprocessoPermissoesDto permissoes = service.calcularPermissoes(sub, admin);

            // Assert
            assertThat(permissoes.isPodeVisualizarImpacto()).isTrue();
        }

        @Test
        @DisplayName("Não deve permitir visualizar impacto para não admin mesmo em situação correta")
        void naoDevePermitirVisualizarImpactoParaNaoAdmin() {
            // Arrange
            Unidade unidadeOutra = criarUnidade(99L, null);
            Usuario gestor = criarUsuarioComPerfil(Perfil.GESTOR, unidadeOutra);
            Subprocesso sub = criarSubprocesso(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);

            // Act
            SubprocessoPermissoesDto permissoes = service.calcularPermissoes(sub, gestor);

            // Assert
            assertThat(permissoes.isPodeVisualizarImpacto()).isFalse();
        }

        @ParameterizedTest
        @EnumSource(value = SituacaoSubprocesso.class, names = {
            "MAPEAMENTO_CADASTRO_HOMOLOGADO",
            "REVISAO_CADASTRO_EM_ANDAMENTO",
            "REVISAO_CADASTRO_DISPONIBILIZADA",
            "REVISAO_CADASTRO_HOMOLOGADA",
            "REVISAO_MAPA_AJUSTADO"
        })
        @DisplayName("Deve permitir visualizar impacto para Admin em cada situação válida")
        void devePermitirVisualizarImpactoEmCadaSituacaoValida(SituacaoSubprocesso situacao) {
            // Arrange
            Usuario admin = criarUsuarioComPerfil(Perfil.ADMIN, null);
            Subprocesso sub = criarSubprocesso(situacao);

            // Act
            SubprocessoPermissoesDto permissoes = service.calcularPermissoes(sub, admin);

            // Assert
            assertThat(permissoes.isPodeVisualizarImpacto()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = SituacaoSubprocesso.class, names = {
            "NAO_INICIADO",
            "MAPEAMENTO_CADASTRO_EM_ANDAMENTO",
            "MAPEAMENTO_MAPA_CRIADO",
            "MAPEAMENTO_MAPA_COM_SUGESTOES",
            "MAPEAMENTO_MAPA_VALIDADO",
            "MAPEAMENTO_MAPA_HOMOLOGADO"
        })
        @DisplayName("Não deve permitir visualizar impacto em situações inválidas para mapeamento")
        void naoDevePermitirVisualizarImpactoEmSituacoesInvalidasMapeamento(SituacaoSubprocesso situacao) {
            // Arrange
            Usuario admin = criarUsuarioComPerfil(Perfil.ADMIN, null);
            Subprocesso sub = criarSubprocesso(situacao);

            // Act
            SubprocessoPermissoesDto permissoes = service.calcularPermissoes(sub, admin);

            // Assert
            assertThat(permissoes.isPodeVisualizarImpacto()).isFalse();
        }
    }

    @Nested
    @DisplayName("Hierarquia de Unidades (isSubordinada)")
    class HierarquiaUnidades {

        @Test
        @DisplayName("Gestor não deve ter acesso quando sua unidade é null")
        void gestorNaoDeveAcessarQuandoSuaUnidadeNull() {
            // Arrange - Gestor com atribuição onde unidade é null
            Usuario gestor = criarUsuarioComPerfilUnidadeNull(Perfil.GESTOR);
            Unidade unidadeSubprocesso = criarUnidade(10L, null);
            Subprocesso sub = criarSubprocessoComUnidade(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, unidadeSubprocesso);

            // Act
            SubprocessoPermissoesDto permissoes = service.calcularPermissoes(sub, gestor);

            // Assert
            assertThat(permissoes.isPodeEditarMapa()).isFalse();
        }

        @Test
        @DisplayName("Gestor não deve ter acesso quando código da sua unidade é null")
        void gestorNaoDeveAcessarQuandoCodigoUnidadeNull() {
            // Arrange - Gestor com unidade existente mas código null
            Unidade unidadeSemCodigo = new Unidade();
            unidadeSemCodigo.setCodigo(null);
            Usuario gestor = criarUsuarioComPerfil(Perfil.GESTOR, unidadeSemCodigo);
            Unidade unidadeSubprocesso = criarUnidade(10L, null);
            Subprocesso sub = criarSubprocessoComUnidade(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, unidadeSubprocesso);

            // Act
            SubprocessoPermissoesDto permissoes = service.calcularPermissoes(sub, gestor);

            // Assert
            assertThat(permissoes.isPodeEditarMapa()).isFalse();
        }

        @Test
        @DisplayName("Gestor deve acessar unidade subordinada de 2 níveis")
        void gestorDeveAcessarUnidadeSubordinadaDoisNiveis() {
            // Arrange - Hierarquia: avo -> pai -> filha
            Unidade avo = criarUnidade(1L, null);
            Unidade pai = criarUnidade(2L, avo);
            Unidade filha = criarUnidade(3L, pai);
            Usuario gestor = criarUsuarioComPerfil(Perfil.GESTOR, avo);
            Subprocesso sub = criarSubprocessoComUnidade(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, filha);

            // Act
            SubprocessoPermissoesDto permissoes = service.calcularPermissoes(sub, gestor);

            // Assert
            assertThat(permissoes.isPodeEditarMapa()).isTrue();
        }

        @Test
        @DisplayName("Gestor não deve acessar quando unidade do subprocesso não tem superior")
        void gestorNaoDeveAcessarQuandoSubprocessoSemUnidadeSuperior() {
            // Arrange - Duas unidades raiz sem relação
            Unidade unidadeGestor = criarUnidade(1L, null);
            Unidade unidadeSubprocesso = criarUnidade(99L, null); // Sem superior
            Usuario gestor = criarUsuarioComPerfil(Perfil.GESTOR, unidadeGestor);
            Subprocesso sub = criarSubprocessoComUnidade(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, unidadeSubprocesso);

            // Act
            SubprocessoPermissoesDto permissoes = service.calcularPermissoes(sub, gestor);

            // Assert
            assertThat(permissoes.isPodeEditarMapa()).isFalse();
        }

        @Test
        @DisplayName("Gestor da mesma unidade deve ter acesso mesmo com hierarquia complexa")
        void gestorMesmaUnidadeDeveAcessarComHierarquia() {
            // Arrange - Gestor é da mesma unidade (não precisa ser subordinada)
            Unidade pai = criarUnidade(1L, null);
            Unidade unidade = criarUnidade(10L, pai);
            Usuario gestor = criarUsuarioComPerfil(Perfil.GESTOR, unidade);
            Subprocesso sub = criarSubprocessoComUnidade(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, unidade);

            // Act
            SubprocessoPermissoesDto permissoes = service.calcularPermissoes(sub, gestor);

            // Assert
            assertThat(permissoes.isPodeEditarMapa()).isTrue();
        }
    }

    @Nested
    @DisplayName("Permissões Devolver/Aceitar Cadastro")
    class PermissoesDevolverAceitar {

        @Test
        @DisplayName("Apenas Admin e Gestor podem devolver cadastro")
        void apenasAdminEGestorPodemDevolverCadastro() {
            // Arrange
            Unidade unidade = criarUnidade(10L, null);
            Usuario admin = criarUsuarioComPerfil(Perfil.ADMIN, null);
            Usuario gestor = criarUsuarioComPerfil(Perfil.GESTOR, unidade);
            Usuario chefe = criarUsuarioComPerfil(Perfil.CHEFE, unidade);
            Usuario servidor = criarUsuarioComPerfil(Perfil.SERVIDOR, unidade);
            Subprocesso sub = criarSubprocessoComUnidade(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, unidade);

            // Act & Assert
            assertThat(service.calcularPermissoes(sub, admin).isPodeDevolverCadastro()).isTrue();
            assertThat(service.calcularPermissoes(sub, gestor).isPodeDevolverCadastro()).isTrue();
            assertThat(service.calcularPermissoes(sub, chefe).isPodeDevolverCadastro()).isFalse();
            assertThat(service.calcularPermissoes(sub, servidor).isPodeDevolverCadastro()).isFalse();
        }

        @Test
        @DisplayName("Apenas Admin e Gestor podem aceitar cadastro")
        void apenasAdminEGestorPodemAceitarCadastro() {
            // Arrange
            Unidade unidade = criarUnidade(10L, null);
            Usuario admin = criarUsuarioComPerfil(Perfil.ADMIN, null);
            Usuario gestor = criarUsuarioComPerfil(Perfil.GESTOR, unidade);
            Usuario chefe = criarUsuarioComPerfil(Perfil.CHEFE, unidade);
            Usuario servidor = criarUsuarioComPerfil(Perfil.SERVIDOR, unidade);
            Subprocesso sub = criarSubprocessoComUnidade(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, unidade);

            // Act & Assert
            assertThat(service.calcularPermissoes(sub, admin).isPodeAceitarCadastro()).isTrue();
            assertThat(service.calcularPermissoes(sub, gestor).isPodeAceitarCadastro()).isTrue();
            assertThat(service.calcularPermissoes(sub, chefe).isPodeAceitarCadastro()).isFalse();
            assertThat(service.calcularPermissoes(sub, servidor).isPodeAceitarCadastro()).isFalse();
        }
    }

    // === Helper Methods ===
    
    private Usuario criarUsuarioComPerfil(Perfil perfil, Unidade unidade) {
        Usuario usuario = mock(Usuario.class);
        Set<UsuarioPerfil> atribuicoes = new HashSet<>();
        atribuicoes.add(UsuarioPerfil.builder().perfil(perfil).unidade(unidade).build());
        when(usuario.getTodasAtribuicoes()).thenReturn(atribuicoes);
        return usuario;
    }

    private Usuario criarUsuarioComPerfilUnidadeNull(Perfil perfil) {
        Usuario usuario = mock(Usuario.class);
        Set<UsuarioPerfil> atribuicoes = new HashSet<>();
        atribuicoes.add(UsuarioPerfil.builder().perfil(perfil).unidade(null).build());
        when(usuario.getTodasAtribuicoes()).thenReturn(atribuicoes);
        return usuario;
    }

    private Unidade criarUnidade(Long codigo, Unidade superior) {
        Unidade unidade = new Unidade();
        unidade.setCodigo(codigo);
        unidade.setUnidadeSuperior(superior);
        return unidade;
    }

    private Subprocesso criarSubprocesso(SituacaoSubprocesso situacao) {
        Subprocesso sub = mock(Subprocesso.class);
        when(sub.getSituacao()).thenReturn(situacao);
        when(sub.getUnidade()).thenReturn(new Unidade());
        Processo processo = mock(Processo.class);
        when(processo.getTipo()).thenReturn(TipoProcesso.MAPEAMENTO);
        when(sub.getProcesso()).thenReturn(processo);
        return sub;
    }

    private Subprocesso criarSubprocessoRevisao(SituacaoSubprocesso situacao) {
        Subprocesso sub = mock(Subprocesso.class);
        when(sub.getSituacao()).thenReturn(situacao);
        when(sub.getUnidade()).thenReturn(new Unidade());
        Processo processo = mock(Processo.class);
        when(processo.getTipo()).thenReturn(TipoProcesso.REVISAO);
        when(sub.getProcesso()).thenReturn(processo);
        return sub;
    }

    private Subprocesso criarSubprocessoComUnidade(SituacaoSubprocesso situacao, Unidade unidade) {
        Subprocesso sub = mock(Subprocesso.class);
        when(sub.getSituacao()).thenReturn(situacao);
        when(sub.getUnidade()).thenReturn(unidade);
        Processo processo = mock(Processo.class);
        when(processo.getTipo()).thenReturn(TipoProcesso.MAPEAMENTO);
        when(sub.getProcesso()).thenReturn(processo);
        return sub;
    }
}

