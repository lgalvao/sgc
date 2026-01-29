package sgc.organizacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.organizacao.dto.UsuarioDto;
import sgc.organizacao.model.*;
import sgc.organizacao.service.AdministradorRepositoryService;
import sgc.organizacao.service.UnidadeRepositoryService;
import sgc.organizacao.service.UsuarioRepositoryService;

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
    @InjectMocks
    private UsuarioFacade service;
    
    @Mock
    private UsuarioRepositoryService usuarioRepositoryService;
    
    @Mock
    private AdministradorRepositoryService administradorService;
    
    @Mock
    private UnidadeRepositoryService unidadeRepositoryService;

    // ========== MÉTODOS DE BUSCA E MAPEAMENTO ==========

    @Nested
    @DisplayName("Autenticação e Carregamento de Usuário")
    class AutenticacaoCarregamento {

        @Test
        @DisplayName("Deve retornar nulo se usuário não encontrado ao carregar para autenticação")
        void deveRetornarNuloQuandoUsuarioNaoEncontrado() {
            when(usuarioRepositoryService.findByIdWithAtribuicoes("user")).thenReturn(Optional.empty());

            assertThat(service.carregarUsuarioParaAutenticacao("user")).isNull();
        }

        @Test
        @DisplayName("Deve carregar atribuições se usuário encontrado")
        void deveCarregarAtribuicoesQuandoEncontrado() {
            Usuario usuario = mock(Usuario.class);
            when(usuario.getTituloEleitoral()).thenReturn("user");
            when(usuarioRepositoryService.findByIdWithAtribuicoes("user")).thenReturn(Optional.of(usuario));
            when(usuarioRepositoryService.findByUsuarioTitulo("user")).thenReturn(Collections.emptyList());

            Usuario result = service.carregarUsuarioParaAutenticacao("user");

            assertThat(result).isNotNull();
            verify(usuario).getAuthorities();
            verify(usuario).setAtribuicoesPermanentes(any());
        }
    }

    @Nested
    @DisplayName("Busca de Usuários")
    class BuscaUsuarios {

        @Test
        @DisplayName("Deve retornar empty se usuário não encontrado por título")
        void deveRetornarEmptyQuandoNaoEncontradoPorTitulo() {
            when(usuarioRepositoryService.findById("user")).thenReturn(Optional.empty());

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
            when(usuarioRepositoryService.findById("user")).thenReturn(Optional.of(u));

            Optional<UsuarioDto> res = service.buscarUsuarioPorTitulo("user");

            assertThat(res).isPresent();
            assertThat(res.get().tituloEleitoral()).isEqualTo("user");
        }

        @Test
        @DisplayName("Deve retornar lista vazia se não há usuários na unidade")
        void deveRetornarListaVaziaSeNaoHaUsuariosNaUnidade() {
            when(usuarioRepositoryService.findByUnidadeLotacaoCodigo(1L)).thenReturn(Collections.emptyList());

            assertThat(service.buscarUsuariosPorUnidade(1L)).isEmpty();
        }

        @Test
        @DisplayName("Deve lançar erro se usuário não encontrado por ID")
        void deveLancarErroSeUsuarioNaoEncontradoPorId() {
            when(usuarioRepositoryService.buscarPorId("user")).thenThrow(new ErroEntidadeNaoEncontrada("Usuario", "user"));

            assertThatThrownBy(() -> service.buscarPorId("user"))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve lançar erro se usuário não encontrado por login")
        void deveLancarErroSeUsuarioNaoEncontradoPorLogin() {
            when(usuarioRepositoryService.findByIdWithAtribuicoes("user")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarPorLogin("user"))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve buscar usuário por login com sucesso")
        void deveBuscarUsuarioPorLoginComSucesso() {
            Usuario u = new Usuario();
            u.setTituloEleitoral("user");
            when(usuarioRepositoryService.findByIdWithAtribuicoes("user")).thenReturn(Optional.of(u));
            when(usuarioRepositoryService.findByUsuarioTitulo("user")).thenReturn(Collections.emptyList());

            Usuario res = service.buscarPorLogin("user");

            assertThat(res).isNotNull();
        }

        @Test
        @DisplayName("Deve retornar empty se usuário não encontrado por email")
        void deveRetornarEmptySeUsuarioNaoEncontradoPorEmail() {
            when(usuarioRepositoryService.findByEmail("email")).thenReturn(Optional.empty());

            assertThat(service.buscarUsuarioPorEmail("email")).isEmpty();
        }

        @Test
        @DisplayName("Deve retornar lista vazia se não há usuários ativos")
        void deveRetornarListaVaziaSeNaoHaUsuariosAtivos() {
            when(usuarioRepositoryService.findAll()).thenReturn(Collections.emptyList());

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
            when(usuarioRepositoryService.findAllById(anyList())).thenReturn(List.of(u));

            var map = service.buscarUsuariosPorTitulos(List.of("u"));

            assertThat(map).containsKey("u");
        }

        @Test
        @DisplayName("Deve retornar lista vazia se usuário não encontrado ao buscar unidades onde é responsável")
        void deveRetornarListaVaziaSeUsuarioNaoEncontradoAoBuscarUnidadesOndeEhResponsavel() {
            when(usuarioRepositoryService.findByIdWithAtribuicoes("user")).thenReturn(Optional.empty());

            assertThat(service.buscarUnidadesOndeEhResponsavel("user")).isEmpty();
        }

        @Test
        @DisplayName("Deve retornar lista vazia se usuário não encontrado ao buscar unidades por perfil")
        void deveRetornarListaVaziaSeUsuarioNaoEncontradoAoBuscarUnidadesPorPerfil() {
            when(usuarioRepositoryService.findByIdWithAtribuicoes("user")).thenReturn(Optional.empty());

            assertThat(service.buscarUnidadesPorPerfil("user", String.valueOf(Perfil.GESTOR))).isEmpty();
        }

        @Test
        @DisplayName("Deve retornar false se usuário não encontrado ao verificar perfil")
        void deveRetornarFalseSeUsuarioNaoEncontradoAoVerificarPerfil() {
            when(usuarioRepositoryService.findByIdWithAtribuicoes("user")).thenReturn(Optional.empty());

            assertThat(service.usuarioTemPerfil("user", String.valueOf(Perfil.GESTOR), 1L)).isFalse();
        }

        @Test
        @DisplayName("Deve retornar empty se usuário não encontrado ao buscar perfis")
        void deveRetornarEmptySeUsuarioNaoEncontradoAoBuscarPerfis() {
            when(usuarioRepositoryService.findByIdWithAtribuicoes("user")).thenReturn(Optional.empty());

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
            when(administradorService.listarTodos()).thenReturn(List.of(admin));
            when(usuarioRepositoryService.findById("user")).thenReturn(Optional.empty());

            assertThat(service.listarAdministradores()).isEmpty();
        }

        @Test
        @DisplayName("Deve lançar erro ao adicionar administrador que já existe")
        void deveLancarErroAoAdicionarAdministradorQueJaExiste() {
            Usuario u = new Usuario();
            u.setTituloEleitoral("user");
            when(usuarioRepositoryService.buscarPorId("user")).thenReturn(u);
            when(administradorService.existePorTitulo("user")).thenReturn(true);

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

            when(usuarioRepositoryService.buscarPorId("user")).thenReturn(u);
            when(administradorService.existePorTitulo("user")).thenReturn(false);

            service.adicionarAdministrador("user");

            verify(administradorService).salvar(any());
        }

        @Test
        @DisplayName("Deve lançar erro ao remover não-administrador")
        void deveLancarErroAoRemoverNaoAdministrador() {
            when(administradorService.existePorTitulo("user")).thenReturn(false);

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
            when(administradorService.existePorTitulo("user")).thenReturn(true);
            when(administradorService.contar()).thenReturn(1L);

            assertThatThrownBy(() -> service.removerAdministrador("user", "other"))
                    .isInstanceOf(ErroValidacao.class);
        }

        @Test
        @DisplayName("Deve remover administrador com sucesso")
        void deveRemoverAdministradorComSucesso() {
            when(administradorService.existePorTitulo("user")).thenReturn(true);
            when(administradorService.contar()).thenReturn(2L);

            service.removerAdministrador("user", "other");

            verify(administradorService).removerPorTitulo("user");
        }

        @Test
        @DisplayName("Deve verificar se é administrador")
        void deveVerificarSeEhAdministrador() {
            when(administradorService.existePorTitulo("user")).thenReturn(true);

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
            org.springframework.security.core.context.SecurityContextHolder.clearContext();

            var erro = assertThrows(ErroAccessoNegado.class, () -> service.obterUsuarioAutenticado());
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