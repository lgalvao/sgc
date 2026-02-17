package sgc.organizacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import sgc.comum.erros.ErroValidacao;
import sgc.organizacao.dto.AdministradorDto;
import sgc.organizacao.dto.UnidadeResponsavelDto;
import sgc.organizacao.model.*;
import sgc.organizacao.service.UnidadeResponsavelService;
import sgc.organizacao.service.UsuarioService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("UsuarioFacade - Testes Unitários")
class UsuarioFacadeTest {

    @Mock
    private UsuarioService usuarioService;
    
    @Mock
    private UnidadeResponsavelService unidadeResponsavelService;
    
    @InjectMocks
    private UsuarioFacade facade;

    @Nested
    @DisplayName("Autenticação e Contexto")
    class AutenticacaoContexto {

        @Test
        @DisplayName("Deve obter usuário autenticado com sucesso")
        void deveObterUsuarioAutenticado() {
            // Arrange
            String titulo = "123456";
            Usuario usuario = criarUsuario(titulo);
            
            configurarAutenticacao(titulo);
            when(usuarioService.buscarPorIdComAtribuicoes(titulo)).thenReturn(usuario);

            // Act
            Usuario resultado = facade.obterUsuarioAutenticado();

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getTituloEleitoral()).isEqualTo(titulo);
            verify(usuarioService).carregarAuthorities(usuario);
            SecurityContextHolder.clearContext();
        }

        @Test
        @DisplayName("Deve obter usuário diretamente do principal se já for instância de Usuario")
        void deveObterUsuarioDiretamenteDoPrincipal() {
            // Arrange
            Usuario usuario = criarUsuario("123456");
            Authentication auth = mock(Authentication.class);
            when(auth.getPrincipal()).thenReturn(usuario);
            SecurityContext context = mock(SecurityContext.class);
            when(context.getAuthentication()).thenReturn(auth);
            SecurityContextHolder.setContext(context);

            // Act
            Usuario resultado = facade.obterUsuarioAutenticado();

            // Assert
            assertThat(resultado).isSameAs(usuario);
            verifyNoInteractions(usuarioService);
            SecurityContextHolder.clearContext();
        }

        @Test
        @DisplayName("Deve filtrar atribuições em unidades inativas")
        void deveFiltrarUnidadesInativas() {
            // Arrange
            String titulo = "123456";
            Usuario usuario = criarUsuario(titulo);
            Unidade unidadeInativa = criarUnidade(1L, "INATIVA");
            unidadeInativa.setSituacao(SituacaoUnidade.INATIVA);
            UsuarioPerfil atribuicao = criarAtribuicao(usuario, unidadeInativa, Perfil.CHEFE);

            when(usuarioService.buscarPorIdComAtribuicoesOpcional(titulo))
                    .thenReturn(Optional.of(usuario));
            when(usuarioService.buscarPerfis(titulo))
                    .thenReturn(List.of(atribuicao));

            // Act
            boolean resultado = facade.usuarioTemPerfil(titulo, "CHEFE", 1L);
            List<Long> unidades = facade.buscarUnidadesPorPerfil(titulo, "CHEFE");

            // Assert
            assertThat(resultado).isFalse();
            assertThat(unidades).isEmpty();
        }

        @Test
        @DisplayName("Deve retornar null quando autenticação for nula")
        void deveRetornarNullQuandoAutenticacaoForNula() {
            // Arrange
            SecurityContextHolder.clearContext();

            // Act
            Usuario resultado = facade.obterUsuarioAutenticadoOuNull();

            // Assert
            assertThat(resultado).isNull();
        }
    }

    @Nested
    @DisplayName("Carregamento de Usuário para Autenticação")
    class CarregamentoAutenticacao {
        @Test
        @DisplayName("Deve carregar usuário para autenticação quando encontrado")
        void deveCarregarUsuarioParaAutenticacao() {
            // Arrange
            String titulo = "123456";
            Usuario usuario = criarUsuario(titulo);
            
            when(usuarioService.buscarPorIdComAtribuicoesOpcional(titulo))
                    .thenReturn(Optional.of(usuario));

            // Act
            Usuario resultado = facade.carregarUsuarioParaAutenticacao(titulo);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getTituloEleitoral()).isEqualTo(titulo);
            verify(usuarioService).carregarAuthorities(usuario);
        }
    }

    @Nested
    @DisplayName("Busca de Responsáveis de Unidades")
    class BuscaResponsaveis {

        @Test
        @DisplayName("Deve buscar responsáveis de unidades com sucesso")
        void deveBuscarResponsaveisComSucesso() {
            // Arrange
            Long codigoUnidade = 1L;
            String titulo = "123456";
            UnidadeResponsavelDto dto = UnidadeResponsavelDto.builder()
                    .unidadeCodigo(codigoUnidade)
                    .titularTitulo(titulo)
                    .build();

            when(unidadeResponsavelService.buscarResponsaveisUnidades(List.of(codigoUnidade)))
                    .thenReturn(Map.of(codigoUnidade, dto));

            // Act
            Map<Long, UnidadeResponsavelDto> resultado = facade.buscarResponsaveisUnidades(List.of(codigoUnidade));

            // Assert
            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(codigoUnidade).titularTitulo()).isEqualTo(titulo);
        }

        @Test
        @DisplayName("buscarResponsaveisUnidades deve retornar mapa vazio se lista vazia")
        void deveRetornarMapaVazioSeListaVazia() {
            Map<Long, UnidadeResponsavelDto> resultado = facade.buscarResponsaveisUnidades(List.of());
            assertThat(resultado).isEmpty();
            verifyNoInteractions(unidadeResponsavelService);
        }
    }

    @Nested
    @DisplayName("Gestão de Administradores")
    class GestaoAdministradores {
        @Test
        @DisplayName("Deve listar administradores com sucesso")
        void deveListarAdministradores() {
            // Arrange
            String titulo = "123456";
            Usuario usuario = criarUsuario(titulo);
            Administrador admin = new Administrador();
            admin.setUsuarioTitulo(titulo);

            when(usuarioService.listarAdministradores()).thenReturn(List.of(admin));
            when(usuarioService.buscarPorIdOpcional(titulo)).thenReturn(Optional.of(usuario));

            // Act
            List<AdministradorDto> resultado = facade.listarAdministradores();

            // Assert
            assertThat(resultado).hasSize(1);
            assertThat(resultado.getFirst().tituloEleitoral()).isEqualTo(titulo);
        }

        @Test
        @DisplayName("Deve adicionar administrador com sucesso")
        void deveAdicionarAdministrador() {
            // Arrange
            String titulo = "123456";
            Usuario usuario = criarUsuario(titulo);

            when(usuarioService.buscarPorId(titulo)).thenReturn(usuario);

            // Act
            AdministradorDto resultado = facade.adicionarAdministrador(titulo);

            // Assert
            assertThat(resultado).isNotNull();
            verify(usuarioService).adicionarAdministrador(titulo);
        }

        @Test
        @DisplayName("Deve remover administrador com sucesso")
        void deveRemoverAdministrador() {
            // Arrange
            String tituloRemover = "111111";
            String tituloAtual = "222222";

            // Act
            facade.removerAdministrador(tituloRemover, tituloAtual);

            // Assert
            verify(usuarioService).removerAdministrador(tituloRemover);
        }

        @Test
        @DisplayName("removerAdministrador deve falhar ao remover a si mesmo")
        void deveFalharAoRemoverSiMesmo() {
            assertThatThrownBy(() -> facade.removerAdministrador("111", "111"))
                    .isInstanceOf(ErroValidacao.class);
        }

        @Test
        @DisplayName("Deve verificar se é administrador")
        void deveVerificarSeAdministrador() {
            when(usuarioService.isAdministrador("111")).thenReturn(true);
            assertThat(facade.isAdministrador("111")).isTrue();
        }
    }

    @Nested
    @DisplayName("Busca de Usuários")
    class BuscaUsuarios {

        @Test
        @DisplayName("Deve buscar usuário por título")
        void deveBuscarUsuarioPorTitulo() {
            // Arrange
            String titulo = "123456";
            Usuario usuario = criarUsuario(titulo);

            when(usuarioService.buscarPorIdOpcional(titulo))
                    .thenReturn(Optional.of(usuario));

            // Act
            Optional<Usuario> resultado = facade.buscarUsuarioPorTitulo(titulo);

            // Assert
            assertThat(resultado).isPresent();
            assertThat(resultado.get().getTituloEleitoral()).isEqualTo(titulo);
        }

        @Test
        @DisplayName("Deve buscar usuários por unidade")
        void deveBuscarUsuariosPorUnidade() {
            // Arrange
            Long codigoUnidade = 1L;
            Usuario usuario = criarUsuario("123456");

            when(usuarioService.buscarPorUnidadeLotacao(codigoUnidade))
                    .thenReturn(List.of(usuario));

            // Act
            List<Usuario> resultado = facade.buscarUsuariosPorUnidade(codigoUnidade);

            // Assert
            assertThat(resultado).hasSize(1);
        }

        @Test
        @DisplayName("Deve buscar usuários ativos")
        void deveBuscarUsuariosAtivos() {
            // Arrange
            Usuario usuario = criarUsuario("123456");

            when(usuarioService.buscarTodos())
                    .thenReturn(List.of(usuario));

            // Act
            List<Usuario> resultado = facade.buscarUsuariosAtivos();

            // Assert
            assertThat(resultado).hasSize(1);
        }

        @Test
        @DisplayName("Deve buscar usuários por títulos")
        void deveBuscarUsuariosPorTitulos() {
            // Arrange
            List<String> titulos = List.of("111111", "222222");
            Usuario usuario1 = criarUsuario("111111");
            Usuario usuario2 = criarUsuario("222222");

            when(usuarioService.buscarTodosPorIds(titulos))
                    .thenReturn(List.of(usuario1, usuario2));

            // Act
            Map<String, Usuario> resultado = facade.buscarUsuariosPorTitulos(titulos);

            // Assert
            assertThat(resultado).hasSize(2).containsKeys("111111", "222222");
        }
    }

    @Test
    @DisplayName("extrairTituloUsuario deve lidar com tipos diferentes")
    void deveExtrairTituloUsuario() {
        assertThat(facade.extrairTituloUsuario("123")).isEqualTo("123");
        Usuario u = new Usuario(); u.setTituloEleitoral("456");
        assertThat(facade.extrairTituloUsuario(u)).isEqualTo("456");
        assertThat(facade.extrairTituloUsuario(123L)).isEqualTo("123");
        assertThat(facade.extrairTituloUsuario(null)).isNull();
    }

    // Métodos auxiliares
    private Usuario criarUsuario(String titulo) {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral(titulo);
        usuario.setNome("Usuário Teste");
        usuario.setEmail("usuario@test.com");
        usuario.setMatricula("12345");
        usuario.setUnidadeLotacao(criarUnidade(1L, "UNID1"));
        return usuario;
    }

    private Unidade criarUnidade(Long codigo, String sigla) {
        Unidade unidade = new Unidade();
        unidade.setCodigo(codigo);
        unidade.setSigla(sigla);
        unidade.setNome("Unidade Teste");
        unidade.setSituacao(SituacaoUnidade.ATIVA);
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        return unidade;
    }

    private UsuarioPerfil criarAtribuicao(Usuario usuario, Unidade unidade, Perfil perfil) {
        UsuarioPerfil atribuicao = new UsuarioPerfil();
        atribuicao.setUsuario(usuario);
        atribuicao.setUsuarioTitulo(usuario.getTituloEleitoral());
        atribuicao.setUnidade(unidade);
        atribuicao.setUnidadeCodigo(unidade.getCodigo());
        atribuicao.setPerfil(perfil);
        return atribuicao;
    }

    private void configurarAutenticacao(String titulo) {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                titulo, null, Collections.emptyList());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }
}
