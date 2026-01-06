package sgc.subprocesso.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.mapa.service.AtividadeService;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Mapa;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioPerfil;
import sgc.subprocesso.dto.SubprocessoPermissoesDto;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.organizacao.model.Unidade;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubprocessoPermissoesServiceTest {

    @Mock
    private AtividadeService atividadeService;

    @InjectMocks
    private SubprocessoPermissoesService service;

    @Nested
    @DisplayName("Validar Ação")
    class ValidarTests {

        @Test
        @DisplayName("Deve validar acesso da unidade com sucesso")
        void deveValidarAcessoUnidadeComSucesso() {
            Subprocesso sub = mock(Subprocesso.class);
            Unidade unidade = mock(Unidade.class);
            when(unidade.getCodigo()).thenReturn(1L);
            when(sub.getUnidade()).thenReturn(unidade);
            when(sub.getSituacao()).thenReturn(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);

            assertDoesNotThrow(() -> service.validar(sub, 1L, "ENVIAR_REVISAO"));
        }

        @Test
        @DisplayName("Deve lançar erro quando unidade sem acesso")
        void deveLancarErroQuandoUnidadeSemAcesso() {
            Subprocesso sub = mock(Subprocesso.class);
            Unidade unidade = mock(Unidade.class);
            when(unidade.getCodigo()).thenReturn(1L);
            when(sub.getUnidade()).thenReturn(unidade);

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
            Subprocesso sub = mock(Subprocesso.class);
            Unidade unidade = mock(Unidade.class);
            when(unidade.getCodigo()).thenReturn(1L);
            when(sub.getUnidade()).thenReturn(unidade);
            when(sub.getSituacao()).thenReturn(SituacaoSubprocesso.NAO_INICIADO);

            assertThatThrownBy(() -> service.validar(sub, 1L, acao))
                    .isInstanceOf(ErroAccessoNegado.class)
                    .hasMessageContaining("Ação '" + acao + "' inválida")
                    .hasNoCause();
        }

        @Test
        @DisplayName("Deve lançar erro quando mapa vazio ao ajustar mapa em situação revisão mapa ajustado")
        void deveLancarErroQuandoMapaVazioAoAjustarMapaEmSituacaoRevisaoMapaAjustado() {
            Subprocesso sub = mock(Subprocesso.class);
            Unidade unidade = mock(Unidade.class);
            when(unidade.getCodigo()).thenReturn(1L);
            when(sub.getUnidade()).thenReturn(unidade);
            when(sub.getCodigo()).thenReturn(123L);
            when(sub.getSituacao()).thenReturn(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);

            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sub.getMapa()).thenReturn(mapa);

            when(atividadeService.buscarPorMapaCodigo(10L)).thenReturn(Collections.emptyList());

            assertThatThrownBy(() -> service.validar(sub, 1L, "AJUSTAR_MAPA"))
                    .isInstanceOf(ErroAccessoNegado.class)
                    .hasMessageContaining("mapa do subprocesso '123' está vazio")
                    .hasNoCause();
        }

        @Test
        @DisplayName("Deve validar ajuste de mapa com sucesso")
        void deveValidarAjusteMapaComSucesso() {
            Subprocesso sub = mock(Subprocesso.class);
            Unidade unidade = mock(Unidade.class);
            when(unidade.getCodigo()).thenReturn(1L);
            when(sub.getUnidade()).thenReturn(unidade);
            when(sub.getSituacao()).thenReturn(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);

            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sub.getMapa()).thenReturn(mapa);

            when(atividadeService.buscarPorMapaCodigo(10L)).thenReturn(List.of(new Atividade()));

            assertDoesNotThrow(() -> service.validar(sub, 1L, "AJUSTAR_MAPA"));
        }

        @Test
        @DisplayName("Deve ignorar ação desconhecida sem lançar exceção")
        void deveIgnorarAcaoDesconhecida() {
            Subprocesso sub = mock(Subprocesso.class);
            Unidade unidade = mock(Unidade.class);
            when(unidade.getCodigo()).thenReturn(1L);
            when(sub.getUnidade()).thenReturn(unidade);
            when(sub.getSituacao()).thenReturn(SituacaoSubprocesso.NAO_INICIADO);

            // Ação desconhecida não deve lançar exceção (cai no else implícito)
            assertDoesNotThrow(() -> service.validar(sub, 1L, "ACAO_DESCONHECIDA"));
        }

        @Test
        @DisplayName("Deve validar ajuste de mapa quando mapa é null")
        void deveValidarAjusteMapaQuandoMapaNull() {
            Subprocesso sub = mock(Subprocesso.class);
            Unidade unidade = mock(Unidade.class);
            when(unidade.getCodigo()).thenReturn(1L);
            when(sub.getUnidade()).thenReturn(unidade);
            when(sub.getSituacao()).thenReturn(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
            when(sub.getMapa()).thenReturn(null);

            // Mapa null não deve entrar na verificação de atividades vazias
            assertDoesNotThrow(() -> service.validar(sub, 1L, "AJUSTAR_MAPA"));
        }

        @Test
        @DisplayName("Deve validar ajuste de mapa em REVISAO_CADASTRO_HOMOLOGADA mesmo com atividades vazias")
        void deveValidarAjusteMapaEmCadastroHomologadaComAtividadesVazias() {
            Subprocesso sub = mock(Subprocesso.class);
            Unidade unidade = mock(Unidade.class);
            when(unidade.getCodigo()).thenReturn(1L);
            when(sub.getUnidade()).thenReturn(unidade);
            when(sub.getSituacao()).thenReturn(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);

            Mapa mapa = mock(Mapa.class);
            when(mapa.getCodigo()).thenReturn(10L);
            when(sub.getMapa()).thenReturn(mapa);

            when(atividadeService.buscarPorMapaCodigo(10L)).thenReturn(Collections.emptyList());

            // Em REVISAO_CADASTRO_HOMOLOGADA, mesmo com atividades vazias, deve ser permitido
            assertDoesNotThrow(() -> service.validar(sub, 1L, "AJUSTAR_MAPA"));
        }
    }

    @Nested
    @DisplayName("Calcular Permissões")
    class PermissoesTests {

        private Usuario criarUsuario(Perfil perfil, Long unidadeId) {
            Usuario usuario = mock(Usuario.class);
            Set<UsuarioPerfil> atribuicoes = new HashSet<>();
            Unidade unidade = new Unidade();
            if (unidadeId != null) {
                unidade.setCodigo(unidadeId);
            }
            atribuicoes.add(UsuarioPerfil.builder().perfil(perfil).unidade(unidade).build());
            when(usuario.getTodasAtribuicoes()).thenReturn(atribuicoes);
            return usuario;
        }

        private Subprocesso criarSubprocesso(Long unidadeId, SituacaoSubprocesso situacao, TipoProcesso tipo) {
            Subprocesso sub = mock(Subprocesso.class);
            Unidade unidade = new Unidade();
            unidade.setCodigo(unidadeId);
            // Configurar hierarquia básica para testes de subordinação
            // Unidade 2 é superior de Unidade 1
            if (unidadeId.equals(1L)) {
                Unidade superior = new Unidade();
                superior.setCodigo(2L);
                unidade.setUnidadeSuperior(superior);
            }

            when(sub.getUnidade()).thenReturn(unidade);
            when(sub.getSituacao()).thenReturn(situacao);

            Processo processo = mock(Processo.class);
            when(processo.getTipo()).thenReturn(tipo);
            when(sub.getProcesso()).thenReturn(processo);

            return sub;
        }

        @Test
        @DisplayName("Admin deve ter acesso de edição em situações permitidas")
        void adminDeveTerAcessoEdicao() {
            Usuario admin = criarUsuario(Perfil.ADMIN, null);
            Subprocesso sub = criarSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO, TipoProcesso.MAPEAMENTO);

            SubprocessoPermissoesDto permissoes = service.calcularPermissoes(sub, admin);

            assertThat(permissoes.isPodeEditarMapa()).isTrue();
            assertThat(permissoes.isPodeVisualizarImpacto()).isTrue();
        }

        @Test
        @DisplayName("Admin não deve ter acesso de edição em situações finalizadas")
        void adminNaoDeveTerAcessoEdicaoEmSituacaoInvalida() {
            Usuario admin = criarUsuario(Perfil.ADMIN, null);
            Subprocesso sub = criarSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO, TipoProcesso.MAPEAMENTO);

            SubprocessoPermissoesDto permissoes = service.calcularPermissoes(sub, admin);

            assertThat(permissoes.isPodeEditarMapa()).isFalse();
            assertThat(permissoes.isPodeVisualizarImpacto()).isFalse();
        }

        @Test
        @DisplayName("Gestor da mesma unidade deve ter acesso")
        void gestorMesmaUnidadeDeveTerAcesso() {
            Usuario gestor = criarUsuario(Perfil.GESTOR, 1L);
            Subprocesso sub = criarSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO, TipoProcesso.MAPEAMENTO);

            SubprocessoPermissoesDto permissoes = service.calcularPermissoes(sub, gestor);

            assertThat(permissoes.isPodeEditarMapa()).isTrue();
        }

        @Test
        @DisplayName("Gestor de unidade superior deve ter acesso")
        void gestorSuperiorDeveTerAcesso() {
            Usuario gestorSuperior = criarUsuario(Perfil.GESTOR, 2L);
            Subprocesso sub = criarSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO, TipoProcesso.MAPEAMENTO);

            SubprocessoPermissoesDto permissoes = service.calcularPermissoes(sub, gestorSuperior);

            assertThat(permissoes.isPodeEditarMapa()).isTrue();
        }

        @Test
        @DisplayName("Gestor de outra unidade (não superior) não deve ter acesso")
        void gestorOutraUnidadeNaoDeveTerAcesso() {
            Usuario gestorOutro = criarUsuario(Perfil.GESTOR, 99L);
            Subprocesso sub = criarSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO, TipoProcesso.MAPEAMENTO);

            SubprocessoPermissoesDto permissoes = service.calcularPermissoes(sub, gestorOutro);

            assertThat(permissoes.isPodeEditarMapa()).isFalse();
            assertThat(permissoes.isPodeVisualizarImpacto()).isFalse();
        }

        @Test
        @DisplayName("Chefe/Servidor da mesma unidade deve ter acesso")
        void servidorMesmaUnidadeDeveTerAcesso() {
            Usuario servidor = criarUsuario(Perfil.SERVIDOR, 1L);
            Subprocesso sub = criarSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO, TipoProcesso.MAPEAMENTO);

            SubprocessoPermissoesDto permissoes = service.calcularPermissoes(sub, servidor);

            assertThat(permissoes.isPodeEditarMapa()).isTrue();
        }

        @Test
        @DisplayName("Chefe/Servidor de unidade superior NÃO deve ter acesso (diferente de Gestor)")
        void servidorSuperiorNaoDeveTerAcesso() {
            Usuario servidorSuperior = criarUsuario(Perfil.SERVIDOR, 2L);
            Subprocesso sub = criarSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO, TipoProcesso.MAPEAMENTO);

            SubprocessoPermissoesDto permissoes = service.calcularPermissoes(sub, servidorSuperior);

            assertThat(permissoes.isPodeEditarMapa()).isFalse();
        }

        @ParameterizedTest
        @CsvSource({
            "NAO_INICIADO, REVISAO, true",
            "NAO_INICIADO, MAPEAMENTO, false",
            "MAPEAMENTO_CADASTRO_HOMOLOGADO, MAPEAMENTO, true",
            "REVISAO_CADASTRO_EM_ANDAMENTO, REVISAO, true",
            "REVISAO_CADASTRO_DISPONIBILIZADA, REVISAO, true",
            "REVISAO_CADASTRO_HOMOLOGADA, REVISAO, true",
            "REVISAO_MAPA_AJUSTADO, REVISAO, true",
            "MAPEAMENTO_MAPA_CRIADO, MAPEAMENTO, false"
        })
        @DisplayName("Validação de visualização de impacto por situação e tipo")
        void deveValidarVisualizacaoImpacto(SituacaoSubprocesso situacao, TipoProcesso tipo, boolean esperado) {
            Usuario admin = criarUsuario(Perfil.ADMIN, null);
            Subprocesso sub = criarSubprocesso(1L, situacao, tipo);

            SubprocessoPermissoesDto permissoes = service.calcularPermissoes(sub, admin);

            assertThat(permissoes.isPodeVisualizarImpacto()).isEqualTo(esperado);
        }

        @Test
        @DisplayName("Usuário sem permissão de edição nunca vê impacto")
        void usuarioSemPermissaoEdicaoNaoVeImpacto() {
            Usuario usuario = criarUsuario(Perfil.SERVIDOR, 99L);
            Subprocesso sub = criarSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO, TipoProcesso.MAPEAMENTO);

            SubprocessoPermissoesDto permissoes = service.calcularPermissoes(sub, usuario);

            assertThat(permissoes.isPodeVisualizarImpacto()).isFalse();
        }

        @Test
        @DisplayName("Deve tratar subprocesso com unidade null")
        void deveTratarSubprocessoComUnidadeNull() {
            Usuario admin = criarUsuario(Perfil.ADMIN, null);
            Subprocesso sub = mock(Subprocesso.class);
            when(sub.getUnidade()).thenReturn(null);
            when(sub.getSituacao()).thenReturn(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);

            Processo processo = mock(Processo.class);
            when(processo.getTipo()).thenReturn(TipoProcesso.MAPEAMENTO);
            when(sub.getProcesso()).thenReturn(processo);

            SubprocessoPermissoesDto permissoes = service.calcularPermissoes(sub, admin);

            // Admin ainda tem acesso mesmo com unidade null
            assertThat(permissoes.isPodeEditarMapa()).isTrue();
        }

        @Test
        @DisplayName("Gestor com atribuição sem unidade não deve ter acesso")
        void gestorComAtribuicaoSemUnidadeNaoDeveTerAcesso() {
            Usuario usuario = mock(Usuario.class);
            Set<UsuarioPerfil> atribuicoes = new HashSet<>();
            // Atribuição de gestor sem unidade definida
            atribuicoes.add(UsuarioPerfil.builder().perfil(Perfil.GESTOR).unidade(null).build());
            when(usuario.getTodasAtribuicoes()).thenReturn(atribuicoes);

            Subprocesso sub = criarSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO, TipoProcesso.MAPEAMENTO);

            SubprocessoPermissoesDto permissoes = service.calcularPermissoes(sub, usuario);

            assertThat(permissoes.isPodeEditarMapa()).isFalse();
        }

        @Test
        @DisplayName("Servidor com atribuição com unidade sem código não deve ter acesso")
        void servidorComUnidadeSemCodigoNaoDeveTerAcesso() {
            Usuario usuario = mock(Usuario.class);
            Set<UsuarioPerfil> atribuicoes = new HashSet<>();
            Unidade unidadeSemCodigo = new Unidade();
            unidadeSemCodigo.setCodigo(null);
            atribuicoes.add(UsuarioPerfil.builder().perfil(Perfil.SERVIDOR).unidade(unidadeSemCodigo).build());
            when(usuario.getTodasAtribuicoes()).thenReturn(atribuicoes);

            Subprocesso sub = criarSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO, TipoProcesso.MAPEAMENTO);

            SubprocessoPermissoesDto permissoes = service.calcularPermissoes(sub, usuario);

            assertThat(permissoes.isPodeEditarMapa()).isFalse();
        }

        @Test
        @DisplayName("Chefe da mesma unidade deve ter acesso")
        void chefeMesmaUnidadeDeveTerAcesso() {
            Usuario chefe = criarUsuario(Perfil.CHEFE, 1L);
            Subprocesso sub = criarSubprocesso(1L, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO, TipoProcesso.MAPEAMENTO);

            SubprocessoPermissoesDto permissoes = service.calcularPermissoes(sub, chefe);

            assertThat(permissoes.isPodeEditarMapa()).isTrue();
        }

        @Test
        @DisplayName("Deve permitir autoavaliação em situação DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO")
        void devePermitirAutoavaliacaoEmSituacaoCorreta() {
            Usuario admin = criarUsuario(Perfil.ADMIN, null);
            Subprocesso sub = criarSubprocesso(1L, SituacaoSubprocesso.DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO, TipoProcesso.DIAGNOSTICO);

            SubprocessoPermissoesDto permissoes = service.calcularPermissoes(sub, admin);

            assertThat(permissoes.isPodeRealizarAutoavaliacao()).isTrue();
            assertThat(permissoes.isPodeEditarMapa()).isTrue();
        }
    }
}
