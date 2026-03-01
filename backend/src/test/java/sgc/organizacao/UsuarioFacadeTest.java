package sgc.organizacao;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.*;
import sgc.comum.erros.*;
import sgc.organizacao.dto.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioFacade - Testes Unitários")
class UsuarioFacadeTest {

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private ResponsavelUnidadeService responsavelUnidadeService;

    @InjectMocks
    private UsuarioFacade facade;

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

    @Nested
    @DisplayName("Autenticação e Contexto")
    class AutenticacaoContexto {

        @Test
        @DisplayName("Deve obter usuário autenticado com sucesso")
        void deveObterUsuarioAutenticado() {

            String titulo = "123456";
            Usuario usuario = criarUsuario(titulo);

            configurarAutenticacao(titulo);
            when(usuarioService.buscarComAtribuicoes(titulo)).thenReturn(usuario);


            Usuario resultado = facade.usuarioAutenticado();


            assertThat(resultado).isNotNull();
            assertThat(resultado.getTituloEleitoral()).isEqualTo(titulo);
            verify(usuarioService).carregarAuthorities(usuario);
            SecurityContextHolder.clearContext();
        }

        @Test
        @DisplayName("Deve lançar ErroAcessoNegado se não autenticado")
        void deveLancarErroAcessoNegadoSeNaoAutenticado() {
            SecurityContextHolder.clearContext();
            assertThatThrownBy(() -> facade.usuarioAutenticado())
                    .isInstanceOf(ErroAcessoNegado.class);
        }

        @Test
        @DisplayName("Deve obter usuário diretamente do principal se já for instância de Usuario")
        void deveObterUsuarioDiretamenteDoPrincipal() {

            Usuario usuario = criarUsuario("123456");
            Authentication auth = mock(Authentication.class);
            when(auth.getPrincipal()).thenReturn(usuario);
            SecurityContext context = mock(SecurityContext.class);
            when(context.getAuthentication()).thenReturn(auth);
            SecurityContextHolder.setContext(context);


            Usuario resultado = facade.usuarioAutenticado();


            assertThat(resultado).isSameAs(usuario);
            verifyNoInteractions(usuarioService);
            SecurityContextHolder.clearContext();
        }
    }

    @Nested
    @DisplayName("Carregamento de Usuário para Autenticação")
    class CarregamentoAutenticacao {
        @Test
        @DisplayName("Deve carregar usuário para autenticação quando encontrado")
        void deveCarregarUsuarioParaAutenticacao() {

            String titulo = "123456";
            Usuario usuario = criarUsuario(titulo);

            when(usuarioService.buscarComAtribuicoesOpt(titulo))
                    .thenReturn(Optional.of(usuario));


            Usuario resultado = facade.carregarUsuarioParaAutenticacao(titulo);


            assertThat(resultado).isNotNull();
            assertThat(resultado.getTituloEleitoral()).isEqualTo(titulo);
            verify(usuarioService).carregarAuthorities(usuario);
        }
    }

    @Nested
    @DisplayName("Gestão de Administradores")
    class GestaoAdministradores {
        @Test
        @DisplayName("Deve listar administradores com sucesso")
        void deveListarAdministradores() {

            String titulo = "123456";
            Usuario usuario = criarUsuario(titulo);
            Administrador admin = new Administrador();
            admin.setUsuarioTitulo(titulo);

            when(usuarioService.buscarAdministradores()).thenReturn(List.of(admin));
            when(usuarioService.buscarOpt(titulo)).thenReturn(Optional.of(usuario));


            List<AdministradorDto> resultado = facade.listarAdministradores();


            assertThat(resultado).hasSize(1);
            assertThat(resultado.getFirst().tituloEleitoral()).isEqualTo(titulo);
        }

        @Test
        @DisplayName("Deve adicionar administrador com sucesso")
        void deveAdicionarAdministrador() {

            String titulo = "123456";
            Usuario usuario = criarUsuario(titulo);

            when(usuarioService.buscar(titulo)).thenReturn(usuario);


            AdministradorDto resultado = facade.adicionarAdministrador(titulo);


            assertThat(resultado).isNotNull();
            verify(usuarioService).adicionarAdministrador(titulo);
        }

        @Test
        @DisplayName("Deve remover administrador com sucesso")
        void deveRemoverAdministrador() {

            String tituloRemover = "111111";
            String tituloAtual = "222222";


            facade.removerAdministrador(tituloRemover, tituloAtual);


            verify(usuarioService).removerAdministrador(tituloRemover);
        }

        @Test
        @DisplayName("removerAdministrador deve falhar ao remover a si mesmo")
        void deveFalharAoRemoverSiMesmo() {
            assertThatThrownBy(() -> facade.removerAdministrador("111", "111"))
                    .isInstanceOf(ErroValidacao.class);
        }
    }

    @Nested
    @DisplayName("Busca de Usuários")
    class BuscaUsuarios {

        @Test
        @DisplayName("Deve buscar usuário por título")
        void deveBuscarUsuarioPorTitulo() {

            String titulo = "123456";
            Usuario usuario = criarUsuario(titulo);

            when(usuarioService.buscarOpt(titulo))
                    .thenReturn(Optional.of(usuario));


            Optional<Usuario> resultado = facade.buscarUsuarioPorTitulo(titulo);


            assertThat(resultado).isPresent();
            assertThat(resultado.get().getTituloEleitoral()).isEqualTo(titulo);
        }

        @Test
        @DisplayName("Deve buscar usuários por títulos")
        void deveBuscarUsuariosPorTitulos() {

            List<String> titulos = List.of("111111", "222222");
            Usuario usuario1 = criarUsuario("111111");
            Usuario usuario2 = criarUsuario("222222");

            when(usuarioService.buscarPorTitulos(titulos))
                    .thenReturn(List.of(usuario1, usuario2));


            Map<String, Usuario> resultado = facade.buscarUsuariosPorTitulos(titulos);


            assertThat(resultado).hasSize(2).containsKeys("111111", "222222");
        }
    }
}
