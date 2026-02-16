package sgc.organizacao;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import sgc.comum.erros.ErroAcessoNegado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.organizacao.dto.UsuarioDto;
import sgc.organizacao.model.Administrador;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.service.UnidadeResponsavelService;
import sgc.organizacao.service.UsuarioService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioFacade - Testes Unitários")
class UsuarioServiceUnitTest {
    
    private UsuarioFacade service;
    
    @Mock
    private UsuarioService usuarioService;

    @Mock
    private UnidadeResponsavelService unidadeResponsavelService;

    @BeforeEach
    void setUp() {
        service = new UsuarioFacade(
            usuarioService,
            unidadeResponsavelService
        );
    }

    // ========== MÉTODOS DE BUSCA E MAPEAMENTO ==========

    @Nested
    @DisplayName("Autenticação e Carregamento de Usuário")
    class AutenticacaoCarregamento {

        @Test
        @DisplayName("Deve retornar nulo se usuário não encontrado ao carregar para autenticação")
        void deveRetornarNuloQuandoUsuarioNaoEncontrado() {
            when(usuarioService.buscarPorIdComAtribuicoesOpcional("user")).thenReturn(Optional.empty());

            assertThat(service.carregarUsuarioParaAutenticacao("user")).isNull();
        }

        @Test
        @DisplayName("Deve carregar atribuições se usuário encontrado")
        void deveCarregarAtribuicoesQuandoEncontrado() {
            Usuario usuario = mock(Usuario.class);
            when(usuarioService.buscarPorIdComAtribuicoesOpcional("user")).thenReturn(Optional.of(usuario));

            Usuario result = service.carregarUsuarioParaAutenticacao("user");

            assertThat(result).isNotNull();
            verify(usuarioService).carregarAuthorities(usuario);
        }
    }

    @Nested
    @DisplayName("Busca de Usuários")
    class BuscaUsuarios {

        @Test
        @DisplayName("Deve retornar empty se usuário não encontrado por título")
        void deveRetornarEmptyQuandoNaoEncontradoPorTitulo() {
            when(usuarioService.buscarPorIdOpcional("user")).thenReturn(Optional.empty());

            assertThat(service.buscarUsuarioPorTitulo("user")).isEmpty();
        }

        @Test
        @DisplayName("Deve retornar DTO quando usuário encontrado por título")
        void deveRetornarDtoQuandoEncontradoPorTitulo() {
            Usuario u = new Usuario();
            u.setTituloEleitoral("user");
            Unidade lotacao = Unidade.builder().build();
            lotacao.setCodigo(1L);
            u.setUnidadeLotacao(lotacao);
            when(usuarioService.buscarPorIdOpcional("user")).thenReturn(Optional.of(u));

            Optional<UsuarioDto> res = service.buscarUsuarioPorTitulo("user");

            assertThat(res).isPresent();
            assertThat(res.get().tituloEleitoral()).isEqualTo("user");
        }

        @Test
        @DisplayName("Deve retornar lista vazia se não há usuários na unidade")
        void deveRetornarListaVaziaSeNaoHaUsuariosNaUnidade() {
            when(usuarioService.buscarPorUnidadeLotacao(1L)).thenReturn(Collections.emptyList());

            assertThat(service.buscarUsuariosPorUnidade(1L)).isEmpty();
        }

        @Test
        @DisplayName("Deve lançar erro se usuário não encontrado por ID")
        void deveLancarErroSeUsuarioNaoEncontradoPorId() {
            when(usuarioService.buscarPorId("user")).thenThrow(new ErroEntidadeNaoEncontrada("Usuário", "user"));

            assertThatThrownBy(() -> service.buscarPorId("user"))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve lançar erro se usuário não encontrado por login")
        void deveLancarErroSeUsuarioNaoEncontradoPorLogin() {
            when(usuarioService.buscarPorIdComAtribuicoes("user")).thenThrow(new ErroEntidadeNaoEncontrada("Usuário", "user"));

            assertThatThrownBy(() -> service.buscarPorLogin("user"))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve buscar usuário por login com sucesso")
        void deveBuscarUsuarioPorLoginComSucesso() {
            Usuario u = new Usuario();
            u.setTituloEleitoral("user");
            when(usuarioService.buscarPorIdComAtribuicoes("user")).thenReturn(u);

            Usuario res = service.buscarPorLogin("user");

            assertThat(res).isNotNull();
        }

        @Test
        @DisplayName("Deve retornar empty se usuário não encontrado por email")
        void deveRetornarEmptySeUsuarioNaoEncontradoPorEmail() {
            when(usuarioService.buscarPorEmail("email")).thenReturn(Optional.empty());

            assertThat(service.buscarUsuarioPorEmail("email")).isEmpty();
        }

        @Test
        @DisplayName("Deve retornar lista vazia se não há usuários ativos")
        void deveRetornarListaVaziaSeNaoHaUsuariosAtivos() {
            when(usuarioService.buscarTodos()).thenReturn(Collections.emptyList());

            assertThat(service.buscarUsuariosAtivos()).isEmpty();
        }

        @Test
        @DisplayName("Deve retornar mapa de usuários por títulos")
        void deveRetornarMapaDeUsuariosPorTitulos() {
            Usuario u = new Usuario();
            u.setTituloEleitoral("u");
            Unidade lotacao = new Unidade();
            lotacao.setCodigo(1L);
            u.setUnidadeLotacao(lotacao);
            when(usuarioService.buscarTodosPorIds(anyList())).thenReturn(List.of(u));

            var map = service.buscarUsuariosPorTitulos(List.of("u"));

            assertThat(map).containsKey("u");
        }

        @Test
        @DisplayName("Deve retornar lista vazia se usuário não encontrado ao buscar unidades onde é responsável")
        void deveRetornarListaVaziaSeUsuarioNaoEncontradoAoBuscarUnidadesOndeEhResponsavel() {
            when(unidadeResponsavelService.buscarUnidadesOndeEhResponsavel("user")).thenReturn(Collections.emptyList());

            assertThat(service.buscarUnidadesOndeEhResponsavel("user")).isEmpty();
        }

        @Test
        @DisplayName("Deve retornar lista vazia se usuário não encontrado ao buscar unidades por perfil")
        void deveRetornarListaVaziaSeUsuarioNaoEncontradoAoBuscarUnidadesPorPerfil() {
            when(usuarioService.buscarPorIdComAtribuicoesOpcional("user")).thenReturn(Optional.empty());

            assertThat(service.buscarUnidadesPorPerfil("user", String.valueOf(Perfil.GESTOR))).isEmpty();
        }

        @Test
        @DisplayName("Deve retornar false se usuário não encontrado ao verificar perfil")
        void deveRetornarFalseSeUsuarioNaoEncontradoAoVerificarPerfil() {
            when(usuarioService.buscarPorIdComAtribuicoesOpcional("user")).thenReturn(Optional.empty());

            assertThat(service.usuarioTemPerfil("user", String.valueOf(Perfil.GESTOR), 1L)).isFalse();
        }

        @Test
        @DisplayName("Deve retornar empty se usuário não encontrado ao buscar perfis")
        void deveRetornarEmptySeUsuarioNaoEncontradoAoBuscarPerfis() {
            when(usuarioService.buscarPorIdComAtribuicoesOpcional("user")).thenReturn(Optional.empty());

            assertThat(service.buscarPerfisUsuario("user")).isEmpty();
        }
    }


    // ========== ADMINISTRAÇÃO DE USUÁRIOS ==========

    @Nested
    @DisplayName("Administração")
    class Administracao {

        @Test
        @DisplayName("Deve ignorar administradores inexistentes ao listar")
        void deveIgnorarAdministradoresInexistentesAoListar() {
            Administrador admin = Administrador.builder().usuarioTitulo("user").build();
            when(usuarioService.listarAdministradores()).thenReturn(List.of(admin));
            when(usuarioService.buscarPorIdOpcional("user")).thenReturn(Optional.empty());

            assertThat(service.listarAdministradores()).isEmpty();
        }

        @Test
        @DisplayName("Deve lançar erro ao adicionar administrador que já existe")
        void deveLancarErroAoAdicionarAdministradorQueJaExiste() {
            Usuario u = new Usuario();
            u.setTituloEleitoral("user");
            when(usuarioService.buscarPorId("user")).thenReturn(u);
            doThrow(new ErroValidacao("Já existe")).when(usuarioService).adicionarAdministrador("user");

            assertThatThrownBy(() -> service.adicionarAdministrador("user"))
                    .isInstanceOf(ErroValidacao.class);
        }

        @Test
        @DisplayName("Deve adicionar administrador com sucesso")
        void deveAdicionarAdministradorComSucesso() {
            Usuario u = new Usuario();
            u.setTituloEleitoral("user");
            Unidade unidade = Unidade.builder().nome("Nome").sigla("SIGLA").build();
            unidade.setCodigo(1L);
            u.setUnidadeLotacao(unidade);

            when(usuarioService.buscarPorId("user")).thenReturn(u);

            service.adicionarAdministrador("user");

            verify(usuarioService).adicionarAdministrador("user");
        }

        @Test
        @DisplayName("Deve lançar erro ao remover não-administrador")
        void deveLancarErroAoRemoverNaoAdministrador() {
            doThrow(new ErroValidacao("Não é admin")).when(usuarioService).removerAdministrador("user");

            assertThatThrownBy(() -> service.removerAdministrador("user", "other"))
                    .isInstanceOf(ErroValidacao.class);
        }

        @Test
        @DisplayName("Deve lançar erro ao remover a si mesmo")
        void deveLancarErroAoRemoverASiMesmo() {
            assertThatThrownBy(() -> service.removerAdministrador("user", "user"))
                    .isInstanceOf(ErroValidacao.class);
        }

        @Test
        @DisplayName("Deve lançar erro ao remover único administrador")
        void deveLancarErroAoRemoverUnicoAdministrador() {
            doThrow(new ErroValidacao("Único admin")).when(usuarioService).removerAdministrador("user");

            assertThatThrownBy(() -> service.removerAdministrador("user", "other"))
                    .isInstanceOf(ErroValidacao.class);
        }

        @Test
        @DisplayName("Deve remover administrador com sucesso")
        void deveRemoverAdministradorComSucesso() {
            service.removerAdministrador("user", "other");

            verify(usuarioService).removerAdministrador("user");
        }

        @Test
        @DisplayName("Deve verificar se é administrador")
        void deveVerificarSeEhAdministrador() {
            when(usuarioService.isAdministrador("user")).thenReturn(true);

            assertThat(service.isAdministrador("user")).isTrue();
        }
    }

    // ========== GAPS DE COBERTURA ==========

    @Nested
    @DisplayName("Gaps de Cobertura")
    class GapsCobertura {

        @Test
        @DisplayName("Deve falhar ao obter usuário autenticado sem contexto")
        void deveFalharSemContexto() {
            SecurityContextHolder.clearContext();

            var erro = assertThrows(ErroAcessoNegado.class, () -> service.obterUsuarioAutenticado());
            assertThat(erro).isNotNull();
        }


        @Test
        @DisplayName("Deve extrair título de diferentes tipos de principal")
        void deveExtrairTitulo() {
            // String direto
            assertEquals("123", service.extrairTituloUsuario("123"));

            // Usuario objeto
            Usuario u = new Usuario();
            u.setTituloEleitoral("456");
            assertEquals("456", service.extrairTituloUsuario(u));

            // Objeto genérico (toString)
            assertEquals("789", service.extrairTituloUsuario(new Object() {
                @Override
                public String toString() {
                    return "789";
                }
            }));

            assertNull(service.extrairTituloUsuario(null));
        }
    }
}