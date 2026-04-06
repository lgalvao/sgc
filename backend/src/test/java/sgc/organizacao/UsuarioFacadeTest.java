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
@DisplayName("UsuarioFacade - Testes unitários")
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
        usuario.setNome("Usuário teste");
        usuario.setEmail("usuario@test.com");
        usuario.setMatricula("12345");
        usuario.setUnidadeLotacao(criarUnidadePadrao());
        return usuario;
    }

    private Unidade criarUnidadePadrao() {
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setSigla("UNID1");
        unidade.setNome("Unidade teste");
        unidade.setSituacao(SituacaoUnidade.ATIVA);
        unidade.setTipo(TipoUnidade.OPERACIONAL);
        return unidade;
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
            when(usuarioService.buscar(titulo)).thenReturn(usuario);
            when(usuarioService.buscarPerfisPorUsuarioTitulo(titulo)).thenReturn(List.of());

            Usuario resultado = facade.usuarioAutenticado();

            assertThat(resultado).isNotNull();
            assertThat(resultado.getTituloEleitoral()).isEqualTo(titulo);
            verify(usuarioService).buscarPerfisPorUsuarioTitulo(titulo);
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
        @DisplayName("Deve retornar vazio se auth for anônimo")
        void deveRetornarVazioSeAuthAnonimo() {
            Authentication auth = mock(AnonymousAuthenticationToken.class);
            when(auth.isAuthenticated()).thenReturn(true);
            SecurityContextHolder.getContext().setAuthentication(auth);
            
            assertThatThrownBy(() -> facade.usuarioAutenticado())
                    .isInstanceOf(ErroAcessoNegado.class);
            SecurityContextHolder.clearContext();
        }

        @Test
        @DisplayName("Deve lançar ErroAcessoNegado se autenticação estiver presente mas não autenticada")
        void deveLancarErroAcessoNegadoSeNaoAutenticadoMesmoComAuth() {
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(false);
            SecurityContextHolder.getContext().setAuthentication(auth);

            assertThatThrownBy(() -> facade.usuarioAutenticado())
                    .isInstanceOf(ErroAcessoNegado.class);

            SecurityContextHolder.clearContext();
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

            when(usuarioService.buscarOpt(titulo))
                    .thenReturn(Optional.of(usuario));
            when(usuarioService.buscarPerfisPorUsuarioTitulo(titulo)).thenReturn(List.of());

            Usuario resultado = facade.carregarUsuarioParaAutenticacao(titulo);

            assertThat(resultado).isNotNull();
            assertThat(resultado.getTituloEleitoral()).isEqualTo(titulo);
            verify(usuarioService).buscarPerfisPorUsuarioTitulo(titulo);
        }

        @Test
        @DisplayName("Deve retornar null se usuário não encontrado")
        void deveRetornarNullSeNaoEncontrado() {
            when(usuarioService.buscarOpt(any())).thenReturn(Optional.empty());
            assertThat(facade.carregarUsuarioParaAutenticacao("1")).isNull();
        }

        @Test
        @DisplayName("Deve buscar usuário por login com sucesso")
        void deveBuscarPorLogin() {
            String login = "user123";
            Usuario usuario = criarUsuario(login);
            when(usuarioService.buscar(login)).thenReturn(usuario);
            when(usuarioService.buscarPerfisPorUsuarioTitulo(login)).thenReturn(List.of());

            Usuario resultado = facade.buscarPorLogin(login);

            assertThat(resultado).isSameAs(usuario);
            verify(usuarioService).buscarPerfisPorUsuarioTitulo(login);
        }
    }

    @Nested
    @DisplayName("Responsabilidades e Perfis")
    class ResponsabilidadesEPerfis {
        @Test
        @DisplayName("Deve buscar responsabilidade detalhada delegando ao service")
        void deveBuscarResponsabilidadeDetalhada() {
            facade.buscarResponsabilidadeDetalhadaAtual("SIGLA");
            verify(responsavelUnidadeService).buscarResponsabilidadeDetalhadaAtual("SIGLA");
        }

        @Test
        @DisplayName("Deve buscar responsável atual delegando ao service")
        void deveBuscarResponsavelAtual() {
            Usuario usuario = criarUsuario("999");
            when(responsavelUnidadeService.buscarResponsavelAtual("UNI")).thenReturn(usuario);

            Usuario resultado = facade.buscarResponsavelAtual("UNI");

            assertThat(resultado).isSameAs(usuario);
            verify(responsavelUnidadeService).buscarResponsavelAtual("UNI");
        }

        @Test
        @DisplayName("Deve buscar perfis filtrando unidades inativas")
        void deveBuscarPerfisFiltrandoInativos() {
            String titulo = "123";
            Usuario user = criarUsuario(titulo);
            when(usuarioService.buscar(titulo)).thenReturn(user);
            
            when(usuarioService.buscarAutorizacoesPerfil(titulo)).thenReturn(List.of(
                    new UsuarioPerfilAutorizacaoLeitura(titulo, Perfil.CHEFE, 1L, "U1", "U1", TipoUnidade.OPERACIONAL, SituacaoUnidade.ATIVA),
                    new UsuarioPerfilAutorizacaoLeitura(titulo, Perfil.GESTOR, 2L, "U2", "U2", TipoUnidade.OPERACIONAL, SituacaoUnidade.INATIVA)
            ));
            
            List<PerfilDto> result = facade.buscarPerfisUsuario(titulo);
            
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().unidadeCodigo()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Deve falhar se usuário não encontrado ao buscar perfis")
        void deveFalharSeUsuarioNaoEncontradoAoBuscarPerfis() {
            when(usuarioService.buscar("999")).thenThrow(new sgc.comum.erros.ErroEntidadeNaoEncontrada("Usuario", "999"));

            assertThatThrownBy(() -> facade.buscarPerfisUsuario("999"))
                    .isInstanceOf(sgc.comum.erros.ErroInconsistenciaInterna.class)
                    .hasMessageContaining("inconsistencia interna do sistema");
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
            when(usuarioService.buscarOptComUnidadeLotacao(titulo)).thenReturn(Optional.of(usuario));

            List<AdministradorDto> resultado = facade.listarAdministradores();

            assertThat(resultado).hasSize(1);
            assertThat(resultado.getFirst().tituloEleitoral()).isEqualTo(titulo);
        }

        @Test
        @DisplayName("Deve ignorar administrador se usuário correspondente não for encontrado")
        void deveIgnorarAdministradorSeUsuarioNaoEncontrado() {
            Administrador admin = new Administrador();
            admin.setUsuarioTitulo("titulo-fantasma");

            when(usuarioService.buscarAdministradores()).thenReturn(List.of(admin));
            when(usuarioService.buscarOptComUnidadeLotacao("titulo-fantasma")).thenReturn(Optional.empty());

            List<AdministradorDto> resultado = facade.listarAdministradores();

            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("Deve adicionar administrador com sucesso")
        void deveAdicionarAdministrador() {

            String titulo = "123456";
            Usuario usuario = criarUsuario(titulo);

            when(usuarioService.buscarOptComUnidadeLotacao(titulo)).thenReturn(Optional.of(usuario));

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

        @Test
        @DisplayName("Deve lidar com duplicatas ao buscar usuários por títulos")
        void deveLidarComDuplicatasAoBuscarUsuariosPorTitulos() {
            List<String> titulos = List.of("1", "1");
            Usuario u1 = criarUsuario("1");

            when(usuarioService.buscarPorTitulos(anyList())).thenReturn(List.of(u1, u1));

            Map<String, Usuario> resultado = facade.buscarUsuariosPorTitulos(titulos);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get("1")).isEqualTo(u1);
        }
    }
}
