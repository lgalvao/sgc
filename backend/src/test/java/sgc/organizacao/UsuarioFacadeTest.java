package sgc.organizacao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import sgc.comum.erros.ErroAcessoNegado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.organizacao.dto.AdministradorDto;
import sgc.organizacao.dto.PerfilDto;
import sgc.organizacao.dto.UnidadeResponsavelDto;
import sgc.organizacao.dto.UsuarioDto;
import sgc.organizacao.model.*;
import sgc.organizacao.service.AdministradorService;
import sgc.organizacao.service.UnidadeConsultaService;
import sgc.organizacao.service.UnidadeResponsavelService;
import sgc.organizacao.service.UsuarioConsultaService;
import sgc.organizacao.service.UsuarioPerfilService;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("UsuarioFacade - Testes Unitários")
class UsuarioFacadeTest {

    @Mock
    private UsuarioConsultaService usuarioConsultaService;
    
    @Mock
    private UsuarioPerfilService usuarioPerfilService;
    
    @Mock
    private AdministradorService administradorService;
    
    @Mock
    private UnidadeConsultaService unidadeConsultaService;
    
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
            when(usuarioConsultaService.buscarPorIdComAtribuicoes(titulo)).thenReturn(usuario);

            // Act
            Usuario resultado = facade.obterUsuarioAutenticado();

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getTituloEleitoral()).isEqualTo(titulo);
            verify(usuarioPerfilService).carregarAuthorities(usuario);
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

            when(usuarioConsultaService.buscarPorIdComAtribuicoesOpcional(titulo))
                    .thenReturn(Optional.of(usuario));
            when(usuarioPerfilService.buscarPorUsuario(titulo))
                    .thenReturn(List.of(atribuicao));

            // Act
            boolean resultado = facade.usuarioTemPerfil(titulo, "CHEFE", 1L);
            List<Long> unidades = facade.buscarUnidadesPorPerfil(titulo, "CHEFE");

            // Assert
            assertThat(resultado).isFalse();
            assertThat(unidades).isEmpty();
        }

        @Test
        @DisplayName("Deve lançar exceção quando não houver usuário autenticado")
        void deveLancarExcecaoQuandoNaoHouverUsuarioAutenticado() {
            // Arrange
            SecurityContextHolder.clearContext();

            // Act & Assert
            assertThatThrownBy(() -> facade.obterUsuarioAutenticado())
                    .isInstanceOf(ErroAcessoNegado.class)
                    .hasMessageContaining("Nenhum usuário autenticado no contexto");
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

        @Test
        @DisplayName("Deve retornar null quando autenticação for anônima")
        void deveRetornarNullQuandoAutenticacaoForAnonima() {
            // Arrange
            SecurityContext context = mock(SecurityContext.class);
            AnonymousAuthenticationToken authToken = mock(AnonymousAuthenticationToken.class);
            when(authToken.isAuthenticated()).thenReturn(true);
            when(context.getAuthentication()).thenReturn(authToken);
            SecurityContextHolder.setContext(context);

            // Act
            Usuario resultado = facade.obterUsuarioAutenticadoOuNull();

            // Assert
            assertThat(resultado).isNull();
            SecurityContextHolder.clearContext();
        }

        @Test
        @DisplayName("Deve retornar null quando não houver autenticação (Contexto vazio)")
        void deveRetornarNullQuandoContextoVazio() {
            // Arrange
            SecurityContext context = mock(SecurityContext.class);
            when(context.getAuthentication()).thenReturn(null);
            SecurityContextHolder.setContext(context);

            // Act
            Usuario resultado = facade.obterUsuarioAutenticadoOuNull();

            // Assert
            assertThat(resultado).isNull();
            SecurityContextHolder.clearContext();
        }

        @Test
        @DisplayName("Deve retornar null quando autenticação não estiver autenticada")
        void deveRetornarNullQuandoNaoAutenticado() {
            // Arrange
            SecurityContext context = mock(SecurityContext.class);
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(false);
            when(context.getAuthentication()).thenReturn(auth);
            SecurityContextHolder.setContext(context);

            // Act
            Usuario resultado = facade.obterUsuarioAutenticadoOuNull();

            // Assert
            assertThat(resultado).isNull();
            SecurityContextHolder.clearContext();
        }

        @Test
        @DisplayName("Deve obter usuário autenticado ou retornar null com sucesso")
        void deveObterUsuarioAutenticadoOuNull() {
            // Arrange
            String titulo = "123456";
            Usuario usuario = criarUsuario(titulo);
            
            configurarAutenticacao(titulo);
            when(usuarioConsultaService.buscarPorIdComAtribuicoes(titulo)).thenReturn(usuario);

            // Act
            Usuario resultado = facade.obterUsuarioAutenticadoOuNull();

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getTituloEleitoral()).isEqualTo(titulo);
            SecurityContextHolder.clearContext();
        }
    }

    @Nested
    @DisplayName("Carregamento de Usuário para Autenticação")
    class CarregamentoAutenticacao {
        @Test
        @DisplayName("Deve carregar usuário para autenticação quando encontrado")
        void deveCarregarUsuarioQuandoEncontrado() {
            // Arrange
            String titulo = "123456";
            Usuario usuario = criarUsuario(titulo);
            
            when(usuarioConsultaService.buscarPorIdComAtribuicoesOpcional(titulo))
                    .thenReturn(Optional.of(usuario));

            // Act
            Usuario resultado = facade.carregarUsuarioParaAutenticacao(titulo);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getTituloEleitoral()).isEqualTo(titulo);
            verify(usuarioPerfilService).carregarAuthorities(usuario);
        }

        @Test
        @DisplayName("Deve retornar null quando usuário não for encontrado")
        void deveRetornarNullQuandoUsuarioNaoEncontrado() {
            // Arrange
            String titulo = "inexistente";
            when(usuarioConsultaService.buscarPorIdComAtribuicoesOpcional(titulo))
                    .thenReturn(Optional.empty());

            // Act
            Usuario resultado = facade.carregarUsuarioParaAutenticacao(titulo);

            // Assert
            assertThat(resultado).isNull();
            verify(usuarioPerfilService, never()).carregarAuthorities(any());
        }
    }

    @Nested
    @DisplayName("Busca de Responsáveis de Unidades")
    class BuscaResponsaveis {

        @Test
        @DisplayName("Deve retornar mapa vazio quando lista de códigos for vazia")
        void deveRetornarMapaVazioQuandoListaVazia() {
            // Act
            Map<Long, UnidadeResponsavelDto> resultado = facade.buscarResponsaveisUnidades(Collections.emptyList());

            // Assert
            assertThat(resultado).isEmpty();
            verify(unidadeResponsavelService, never()).buscarResponsaveisUnidades(anyList());
        }

        @Test
        @DisplayName("Deve retornar mapa vazio quando não houver chefes")
        void deveRetornarMapaVazioQuandoNaoHouverChefes() {
            // Arrange
            List<Long> codigos = List.of(1L, 2L);
            when(unidadeResponsavelService.buscarResponsaveisUnidades(codigos))
                    .thenReturn(Collections.emptyMap());

            // Act
            Map<Long, UnidadeResponsavelDto> resultado = facade.buscarResponsaveisUnidades(codigos);

            // Assert
            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("Deve buscar responsáveis de unidades com sucesso")
        void deveBuscarResponsaveisComSucesso() {
            // Arrange
            Long codigoUnidade = 1L;
            String titulo = "123456";
            UnidadeResponsavelDto dto = UnidadeResponsavelDto.builder()
                    .unidadeCodigo(codigoUnidade)
                    .titularTitulo(titulo)
                    .titularNome("Nome")
                    .substitutoTitulo(null)
                    .substitutoNome(null)
                    .build();

            when(unidadeResponsavelService.buscarResponsaveisUnidades(List.of(codigoUnidade)))
                    .thenReturn(Map.of(codigoUnidade, dto));

            // Act
            Map<Long, UnidadeResponsavelDto> resultado = facade.buscarResponsaveisUnidades(List.of(codigoUnidade));

            // Assert
            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(codigoUnidade)).isNotNull();
            assertThat(resultado.get(codigoUnidade).titularTitulo()).isEqualTo(titulo);
        }

        @Test
        @DisplayName("Deve buscar responsável único de unidade com sucesso")
        void deveBuscarResponsavelUnicoComSucesso() {
            // Arrange
            Long codigoUnidade = 1L;
            String titulo = "123456";
            UnidadeResponsavelDto dto = UnidadeResponsavelDto.builder()
                    .unidadeCodigo(codigoUnidade)
                    .titularTitulo(titulo)
                    .titularNome("Nome")
                    .build();

            when(unidadeResponsavelService.buscarResponsavelUnidade(codigoUnidade))
                    .thenReturn(dto);

            // Act
            UnidadeResponsavelDto resultado = facade.buscarResponsavelUnidade(codigoUnidade);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.titularTitulo()).isEqualTo(titulo);
        }

        @Test
        @DisplayName("Deve lançar exceção quando não encontrar responsável de unidade")
        void deveLancarExcecaoQuandoNaoEncontrarResponsavel() {
            // Arrange
            Long codigoUnidade = 999L;
            when(unidadeResponsavelService.buscarResponsavelUnidade(codigoUnidade))
                    .thenThrow(new ErroEntidadeNaoEncontrada("Responsável da unidade", codigoUnidade));

            // Act & Assert
            assertThatThrownBy(() -> facade.buscarResponsavelUnidade(codigoUnidade))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                    .hasMessageContaining("Responsável da unidade");
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

            when(administradorService.listarTodos()).thenReturn(List.of(admin));
            when(usuarioConsultaService.buscarPorIdOpcional(titulo)).thenReturn(Optional.of(usuario));

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

            when(usuarioConsultaService.buscarPorId(titulo)).thenReturn(usuario);

            // Act
            AdministradorDto resultado = facade.adicionarAdministrador(titulo);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.tituloEleitoral()).isEqualTo(titulo);
            verify(administradorService).adicionar(titulo);
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
            verify(administradorService).remover(tituloRemover);
        }

        @Test
        @DisplayName("Deve lançar exceção ao tentar remover a si mesmo como administrador")
        void deveLancarExcecaoAoRemoverASiMesmo() {
            // Arrange
            String titulo = "123456";

            // Act & Assert
            assertThatThrownBy(() -> facade.removerAdministrador(titulo, titulo))
                    .isInstanceOf(ErroValidacao.class)
                    .hasMessageContaining("Não é permitido remover a si mesmo como administrador");
            
            verify(administradorService, never()).remover(any());
        }

        @Test
        @DisplayName("Deve verificar se usuário é administrador")
        void deveVerificarSeEhAdministrador() {
            // Arrange
            String titulo = "123456";
            when(administradorService.isAdministrador(titulo)).thenReturn(true);

            // Act
            boolean resultado = facade.isAdministrador(titulo);

            // Assert
            assertThat(resultado).isTrue();
        }
    }

    @Nested
    @DisplayName("Extração de Título de Usuário")
    class ExtracaoTitulo {

        @Test
        @DisplayName("Deve extrair título quando principal for String")
        void deveExtrairTituloQuandoString() {
            // Arrange
            String titulo = "123456";

            // Act
            String resultado = facade.extrairTituloUsuario(titulo);

            // Assert
            assertThat(resultado).isEqualTo(titulo);
        }

        @Test
        @DisplayName("Deve extrair título quando principal for Usuario")
        void deveExtrairTituloQuandoUsuario() {
            // Arrange
            String titulo = "123456";
            Usuario usuario = criarUsuario(titulo);

            // Act
            String resultado = facade.extrairTituloUsuario(usuario);

            // Assert
            assertThat(resultado).isEqualTo(titulo);
        }

        @Test
        @DisplayName("Deve converter para string quando principal for outro tipo")
        void deveConverterParaStringQuandoOutroTipo() {
            // Arrange
            Long principal = 12345L;

            // Act
            String resultado = facade.extrairTituloUsuario(principal);

            // Assert
            assertThat(resultado).isEqualTo("12345");
        }

        @Test
        @DisplayName("Deve retornar null quando principal for null")
        void deveRetornarNullQuandoPrincipalNull() {
            // Act
            String resultado = facade.extrairTituloUsuario(null);

            // Assert
            assertThat(resultado).isNull();
        }
    }

    @Nested
    @DisplayName("Busca de Perfis e Atribuições")
    class BuscaPerfis {

        @Test
        @DisplayName("Deve buscar perfis de usuário com sucesso")
        void deveBuscarPerfisComSucesso() {
            // Arrange
            String titulo = "123456";
            Usuario usuario = criarUsuario(titulo);
            Unidade unidade = criarUnidade(1L, "UNID1");
            UsuarioPerfil atribuicao = criarAtribuicao(usuario, unidade, Perfil.CHEFE);

            when(usuarioConsultaService.buscarPorIdComAtribuicoesOpcional(titulo))
                    .thenReturn(Optional.of(usuario));
            when(usuarioPerfilService.buscarPorUsuario(titulo))
                    .thenReturn(List.of(atribuicao));

            // Act
            List<PerfilDto> resultado = facade.buscarPerfisUsuario(titulo);

            // Assert
            assertThat(resultado).hasSize(1);
            assertThat(resultado.getFirst().perfil()).isEqualTo("CHEFE");
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando usuário não for encontrado")
        void deveRetornarListaVaziaQuandoUsuarioNaoEncontrado() {
            // Arrange
            String titulo = "inexistente";
            when(usuarioConsultaService.buscarPorIdComAtribuicoesOpcional(titulo))
                    .thenReturn(Optional.empty());

            // Act
            List<PerfilDto> resultado = facade.buscarPerfisUsuario(titulo);

            // Assert
            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("Deve buscar unidades onde é responsável")
        void deveBuscarUnidadesOndeEhResponsavel() {
            // Arrange
            String titulo = "123456";
            
            when(unidadeResponsavelService.buscarUnidadesOndeEhResponsavel(titulo))
                    .thenReturn(List.of(1L));

            // Act
            List<Long> resultado = facade.buscarUnidadesOndeEhResponsavel(titulo);

            // Assert
            assertThat(resultado).hasSize(1).contains(1L);
        }

        @Test
        @DisplayName("Deve verificar se usuário tem perfil específico")
        void deveVerificarSeUsuarioTemPerfil() {
            // Arrange
            String titulo = "123456";
            Usuario usuario = criarUsuario(titulo);
            Unidade unidade = criarUnidade(1L, "UNID1");
            UsuarioPerfil atribuicao = criarAtribuicao(usuario, unidade, Perfil.CHEFE);

            when(usuarioConsultaService.buscarPorIdComAtribuicoesOpcional(titulo))
                    .thenReturn(Optional.of(usuario));
            when(usuarioPerfilService.buscarPorUsuario(titulo))
                    .thenReturn(List.of(atribuicao));

            // Act
            boolean resultado = facade.usuarioTemPerfil(titulo, "CHEFE", 1L);

            // Assert
            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("Deve retornar false quando usuário não tem perfil")
        void deveRetornarFalseQuandoUsuarioNaoTemPerfil() {
            // Arrange
            String titulo = "123456";
            when(usuarioConsultaService.buscarPorIdComAtribuicoesOpcional(titulo))
                    .thenReturn(Optional.empty());

            // Act
            boolean resultado = facade.usuarioTemPerfil(titulo, "CHEFE", 1L);

            // Assert
            assertThat(resultado).isFalse();
        }

        @Test
        @DisplayName("Deve buscar unidades por perfil")
        void deveBuscarUnidadesPorPerfil() {
            // Arrange
            String titulo = "123456";
            Usuario usuario = criarUsuario(titulo);
            Unidade unidade = criarUnidade(1L, "UNID1");
            UsuarioPerfil atribuicao = criarAtribuicao(usuario, unidade, Perfil.SERVIDOR);

            when(usuarioConsultaService.buscarPorIdComAtribuicoesOpcional(titulo))
                    .thenReturn(Optional.of(usuario));
            when(usuarioPerfilService.buscarPorUsuario(titulo))
                    .thenReturn(List.of(atribuicao));

            // Act
            List<Long> resultado = facade.buscarUnidadesPorPerfil(titulo, "SERVIDOR");

            // Assert
            assertThat(resultado).hasSize(1).contains(1L);
        }

        @Test
        @DisplayName("Deve filtrar unidades inativas ao buscar perfis")
        void deveFiltrarUnidadesInativasEmBuscaPerfis() {
            // Arrange
            String titulo = "123456";
            Usuario usuario = criarUsuario(titulo);
            Unidade unidadeInativa = criarUnidade(1L, "UNID1");
            unidadeInativa.setSituacao(SituacaoUnidade.INATIVA);

            UsuarioPerfil atribuicao = criarAtribuicao(usuario, unidadeInativa, Perfil.CHEFE);

            when(usuarioConsultaService.buscarPorIdComAtribuicoesOpcional(titulo))
                    .thenReturn(Optional.of(usuario));
            when(usuarioPerfilService.buscarPorUsuario(titulo))
                    .thenReturn(List.of(atribuicao));

            // Act
            List<PerfilDto> resultado = facade.buscarPerfisUsuario(titulo);

            // Assert
            assertThat(resultado).isEmpty();
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

            when(usuarioConsultaService.buscarPorIdOpcional(titulo))
                    .thenReturn(Optional.of(usuario));

            // Act
            Optional<UsuarioDto> resultado = facade.buscarUsuarioPorTitulo(titulo);

            // Assert
            assertThat(resultado).isPresent();
            assertThat(resultado.get().tituloEleitoral()).isEqualTo(titulo);
        }

        @Test
        @DisplayName("Deve buscar usuário por email")
        void deveBuscarUsuarioPorEmail() {
            // Arrange
            String email = "usuario@email.com";
            Usuario usuario = criarUsuario("123456");
            usuario.setEmail(email);

            when(usuarioConsultaService.buscarPorEmail(email))
                    .thenReturn(Optional.of(usuario));

            // Act
            Optional<UsuarioDto> resultado = facade.buscarUsuarioPorEmail(email);

            // Assert
            assertThat(resultado).isPresent();
            assertThat(resultado.get().email()).isEqualTo(email);
        }

        @Test
        @DisplayName("Deve buscar usuários por unidade")
        void deveBuscarUsuariosPorUnidade() {
            // Arrange
            Long codigoUnidade = 1L;
            Usuario usuario = criarUsuario("123456");

            when(usuarioConsultaService.buscarPorUnidadeLotacao(codigoUnidade))
                    .thenReturn(List.of(usuario));

            // Act
            List<UsuarioDto> resultado = facade.buscarUsuariosPorUnidade(codigoUnidade);

            // Assert
            assertThat(resultado).hasSize(1);
        }

        @Test
        @DisplayName("Deve buscar usuários ativos")
        void deveBuscarUsuariosAtivos() {
            // Arrange
            Usuario usuario = criarUsuario("123456");

            when(usuarioConsultaService.buscarTodos())
                    .thenReturn(List.of(usuario));

            // Act
            List<UsuarioDto> resultado = facade.buscarUsuariosAtivos();

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

            when(usuarioConsultaService.buscarTodosPorIds(titulos))
                    .thenReturn(List.of(usuario1, usuario2));

            // Act
            Map<String, UsuarioDto> resultado = facade.buscarUsuariosPorTitulos(titulos);

            // Assert
            assertThat(resultado).hasSize(2).containsKeys("111111", "222222");
        }

        @Test
        @DisplayName("Deve buscar usuário por ID")
        void deveBuscarUsuarioPorId() {
            // Arrange
            String titulo = "123456";
            Usuario usuario = criarUsuario(titulo);

            when(usuarioConsultaService.buscarPorId(titulo)).thenReturn(usuario);

            // Act
            Usuario resultado = facade.buscarPorId(titulo);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getTituloEleitoral()).isEqualTo(titulo);
        }

        @Test
        @DisplayName("Deve buscar usuário por login")
        void deveBuscarUsuarioPorLogin() {
            // Arrange
            String login = "123456";
            Usuario usuario = criarUsuario(login);

            when(usuarioConsultaService.buscarPorIdComAtribuicoes(login)).thenReturn(usuario);

            // Act
            Usuario resultado = facade.buscarPorLogin(login);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getTituloEleitoral()).isEqualTo(login);
            verify(usuarioPerfilService).carregarAuthorities(usuario);
        }

        @Test
        @DisplayName("Deve buscar responsável atual por sigla")
        void deveBuscarResponsavelAtual() {
            // Arrange
            String sigla = "UNID1";
            Usuario chefe = criarUsuario("123456");

            when(unidadeResponsavelService.buscarResponsavelAtual(sigla)).thenReturn(chefe);

            // Act
            Usuario resultado = facade.buscarResponsavelAtual(sigla);

            // Assert
            assertThat(resultado).isNotNull();
            assertThat(resultado.getTituloEleitoral()).isEqualTo("123456");
        }
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
