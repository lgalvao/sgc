package sgc.organizacao;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
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
import sgc.organizacao.model.Administrador;

import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.SituacaoUnidade;
import sgc.organizacao.model.Unidade;

import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioPerfil;

import sgc.organizacao.service.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes de Cobertura para UsuarioFacade")
class UsuarioFacadeCoverageTest {

    private UsuarioFacade usuarioFacade;

    @Mock
    private UsuarioConsultaService usuarioConsultaService;
    @Mock
    private UsuarioPerfilService usuarioPerfilService;
    @Mock
    private AdministradorService administradorService;
    @Mock
    private UnidadeConsultaService unidadeConsultaService;

    @BeforeEach
    void setUp() {
        usuarioFacade = new UsuarioFacade(
            usuarioConsultaService,
            usuarioPerfilService,
            administradorService,
            unidadeConsultaService
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve retornar vazio se não houver autenticação ou for anônima")
    void deveRetornarVazioSeSemAutenticacao() {
        SecurityContext ctx = mock(SecurityContext.class);
        SecurityContextHolder.setContext(ctx);

        // Caso 1: authentication null
        when(ctx.getAuthentication()).thenReturn(null);
        assertThat(usuarioFacade.obterUsuarioAutenticadoOuNull()).isNull();

        // Caso 2: authentication nao autenticada
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);
        when(ctx.getAuthentication()).thenReturn(auth);
        assertThat(usuarioFacade.obterUsuarioAutenticadoOuNull()).isNull();

        // Caso 3: AnonymousAuthenticationToken
        AnonymousAuthenticationToken anon = mock(AnonymousAuthenticationToken.class);
        when(anon.isAuthenticated()).thenReturn(true);
        when(ctx.getAuthentication()).thenReturn(anon);
        assertThat(usuarioFacade.obterUsuarioAutenticadoOuNull()).isNull();
    }

    @Test
    @DisplayName("Deve lançar erro de acesso negado ao obter usuário autenticado sem contexto")
    void deveLancarErroAoObterUsuarioAutenticadoSemContexto() {
        SecurityContext ctx = mock(SecurityContext.class);
        SecurityContextHolder.setContext(ctx);
        when(ctx.getAuthentication()).thenReturn(null);

        var erro = assertThrows(ErroAcessoNegado.class, () -> usuarioFacade.obterUsuarioAutenticado());
        assertThat(erro).isNotNull();
    }

    @Test
    @DisplayName("Deve retornar vazio se usuário não encontrado ao buscar unidades de responsabilidade")
    void deveRetornarVaziaSeUsuarioNaoEncontradoNoResponsavel() {
        when(usuarioConsultaService.buscarPorIdComAtribuicoesOpcional("T")).thenReturn(Optional.empty());
        assertThat(usuarioFacade.buscarUnidadesOndeEhResponsavel("T")).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar nulo se principal for desconhecido")
    void deveExtrairTituloUsuarioDesconhecido() {
        assertThat(usuarioFacade.extrairTituloUsuario(null)).isNull();
        assertThat(usuarioFacade.extrairTituloUsuario(123)).isEqualTo("123");

        Usuario u = new Usuario();
        u.setTituloEleitoral("TITULO");
        assertThat(usuarioFacade.extrairTituloUsuario(u)).isEqualTo("TITULO");
    }

    @Test
    @DisplayName("Deve buscar por id delegando para servico")
    void deveBuscarPorId() {
        Usuario u = new Usuario();
        u.setTituloEleitoral("T");
        when(usuarioConsultaService.buscarPorId("T")).thenReturn(u);

        Usuario result = usuarioFacade.buscarPorId("T");
        assertThat(result.getTituloEleitoral()).isEqualTo("T");
    }

    @Test
    @DisplayName("Deve carregar usuário para autenticação")
    void deveCarregarUsuarioParaAutenticacao() {
        Usuario u = new Usuario();
        u.setTituloEleitoral("T");
        when(usuarioConsultaService.buscarPorIdComAtribuicoesOpcional("T")).thenReturn(Optional.of(u));

        Usuario result = usuarioFacade.carregarUsuarioParaAutenticacao("T");
        assertThat(result).isNotNull();
        verify(usuarioPerfilService).carregarAuthorities(u);
    }

    @Test
    @DisplayName("Deve lançar erro se usuário não encontrado por login")
    void deveLancarErroSeUsuarioNaoEncontradoPorLogin() {
        when(usuarioConsultaService.buscarPorIdComAtribuicoes("T")).thenThrow(new ErroEntidadeNaoEncontrada("Usuário", "T"));
        assertThrows(ErroEntidadeNaoEncontrada.class, () -> usuarioFacade.buscarPorLogin("T"));
    }

    @Test
    @DisplayName("Deve buscar usuário por título")
    void deveBuscarUsuarioPorTitulo() {
        Usuario u = new Usuario();
        u.setTituloEleitoral("T");
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        u.setUnidadeLotacao(unidade);
        when(usuarioConsultaService.buscarPorIdOpcional("T")).thenReturn(Optional.of(u));

        Optional<UsuarioDto> dto = usuarioFacade.buscarUsuarioPorTitulo("T");
        assertThat(dto).isPresent();
        assertThat(dto.get().tituloEleitoral()).isEqualTo("T");
    }

    @Test
    @DisplayName("Deve buscar usuários por unidade")
    void deveBuscarUsuariosPorUnidade() {
        Usuario u = new Usuario();
        u.setTituloEleitoral("T");
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        u.setUnidadeLotacao(unidade);

        when(usuarioConsultaService.buscarPorUnidadeLotacao(1L)).thenReturn(List.of(u));

        List<UsuarioDto> lista = usuarioFacade.buscarUsuariosPorUnidade(1L);
        assertThat(lista).hasSize(1);
    }

    @Test
    @DisplayName("Deve retornar vazio ao buscar perfis de usuário inexistente")
    void deveRetornarVazioAoBuscarPerfisDeUsuarioInexistente() {
        when(usuarioConsultaService.buscarPorIdComAtribuicoesOpcional("T")).thenReturn(Optional.empty());
        assertThat(usuarioFacade.buscarPerfisUsuario("T")).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar perfis do usuário")
    void deveBuscarPerfisDoUsuario() {
        Usuario u = new Usuario();
        u.setTituloEleitoral("T");

        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setNome("U1");
        unidade.setSituacao(SituacaoUnidade.ATIVA);

        UsuarioPerfil up = new UsuarioPerfil();
        up.setUsuario(u);
        up.setUnidade(unidade);
        up.setPerfil(Perfil.CHEFE);

        when(usuarioConsultaService.buscarPorIdComAtribuicoesOpcional("T")).thenReturn(Optional.of(u));
        when(usuarioPerfilService.buscarPorUsuario("T")).thenReturn(List.of(up));

        List<PerfilDto> perfis = usuarioFacade.buscarPerfisUsuario("T");
        assertThat(perfis).hasSize(1);
        assertThat(perfis.get(0).perfil()).isEqualTo("CHEFE");
    }

    @Test
    @DisplayName("Deve buscar responsável atual")
    void deveBuscarResponsavelAtual() {
        Unidade un = new Unidade();
        un.setCodigo(1L);
        when(unidadeConsultaService.buscarPorSigla("SIGLA")).thenReturn(un);

        Usuario chefeSimples = new Usuario();
        chefeSimples.setTituloEleitoral("C");
        when(usuarioConsultaService.buscarChefePorUnidade(1L, "SIGLA")).thenReturn(chefeSimples);

        Usuario chefeCompleto = new Usuario();
        chefeCompleto.setTituloEleitoral("C");
        when(usuarioConsultaService.buscarPorIdComAtribuicoes("C")).thenReturn(chefeCompleto);

        Usuario res = usuarioFacade.buscarResponsavelAtual("SIGLA");
        assertThat(res).isNotNull();
    }

    @Test
    @DisplayName("Deve buscar responsável por código da unidade")
    void deveBuscarResponsavelPorCodigoUnidade() {
        Usuario u = new Usuario();
        u.setTituloEleitoral("T");
        u.setNome("Nome");
        when(usuarioConsultaService.buscarChefesPorUnidades(List.of(1L))).thenReturn(List.of(u));

        UnidadeResponsavelDto resp = usuarioFacade.buscarResponsavelUnidade(1L);
        assertThat(resp.titularTitulo()).isEqualTo("T");
    }

    @Test
    @DisplayName("Deve buscar responsáveis de múltiplas unidades")
    void deveBuscarResponsaveisMultiplasUnidades() {
        Usuario u = new Usuario();
        u.setTituloEleitoral("T");
        u.setNome("Nome");

        Unidade un = new Unidade();
        un.setCodigo(1L);
        un.setSituacao(SituacaoUnidade.ATIVA);

        UsuarioPerfil up = new UsuarioPerfil();
        up.setUsuario(u);
        up.setUsuarioTitulo("T");
        up.setUnidadeCodigo(1L);
        up.setUnidade(un);
        up.setPerfil(Perfil.CHEFE);

        when(usuarioConsultaService.buscarChefesPorUnidades(List.of(1L))).thenReturn(List.of(u));
        when(usuarioConsultaService.buscarPorIdsComAtribuicoes(List.of("T"))).thenReturn(List.of(u));
        when(usuarioPerfilService.buscarAtribuicoesParaCache("T")).thenReturn(Set.of(up));

        Map<Long, UnidadeResponsavelDto> map = usuarioFacade.buscarResponsaveisUnidades(List.of(1L));
        assertThat(map).containsKey(1L);
    }

    @Test
    @DisplayName("Deve buscar usuário por email")
    void deveBuscarUsuarioPorEmail() {
        Usuario u = new Usuario();
        u.setTituloEleitoral("T");
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        u.setUnidadeLotacao(unidade);
        when(usuarioConsultaService.buscarPorEmail("email")).thenReturn(Optional.of(u));

        assertThat(usuarioFacade.buscarUsuarioPorEmail("email")).isPresent();
    }

    @Test
    @DisplayName("Deve buscar usuários ativos")
    void deveBuscarUsuariosAtivos() {
        when(usuarioConsultaService.buscarTodos()).thenReturn(Collections.emptyList());
        assertThat(usuarioFacade.buscarUsuariosAtivos()).isEmpty();
    }

    @Test
    @DisplayName("Deve verificar se usuário tem perfil")
    void deveVerificarUsuarioTemPerfil() {
        Usuario u = new Usuario();
        u.setTituloEleitoral("T");

        Unidade un = new Unidade();
        un.setCodigo(1L);
        un.setSituacao(SituacaoUnidade.ATIVA);

        UsuarioPerfil up = new UsuarioPerfil();
        up.setUsuario(u);
        up.setUnidadeCodigo(1L);
        up.setUnidade(un);
        up.setPerfil(Perfil.CHEFE);

        when(usuarioConsultaService.buscarPorIdComAtribuicoesOpcional("T")).thenReturn(Optional.of(u));
        when(usuarioPerfilService.buscarPorUsuario("T")).thenReturn(List.of(up));

        assertThat(usuarioFacade.usuarioTemPerfil("T", "CHEFE", 1L)).isTrue();
    }

    @Test
    @DisplayName("Deve buscar unidades por perfil")
    void deveBuscarUnidadesPorPerfil() {
        Usuario u = new Usuario();
        u.setTituloEleitoral("T");
        UsuarioPerfil up = new UsuarioPerfil();
        up.setUsuario(u);
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setSituacao(SituacaoUnidade.ATIVA);
        up.setUnidade(unidade);
        up.setPerfil(Perfil.CHEFE);

        when(usuarioConsultaService.buscarPorIdComAtribuicoesOpcional("T")).thenReturn(Optional.of(u));
        when(usuarioPerfilService.buscarPorUsuario("T")).thenReturn(List.of(up));

        assertThat(usuarioFacade.buscarUnidadesPorPerfil("T", "CHEFE")).contains(1L);
    }

    @Test
    @DisplayName("Deve listar administradores")
    void deveListarAdministradores() {
        Administrador admin = new Administrador();
        admin.setUsuarioTitulo("T");
        when(administradorService.listarTodos()).thenReturn(List.of(admin));

        Usuario u = new Usuario();
        u.setTituloEleitoral("T");
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        u.setUnidadeLotacao(unidade);
        when(usuarioConsultaService.buscarPorIdOpcional("T")).thenReturn(Optional.of(u));

        List<AdministradorDto> admins = usuarioFacade.listarAdministradores();
        assertThat(admins).hasSize(1);
    }

    @Test
    @DisplayName("Deve adicionar administrador")
    void deveAdicionarAdministrador() {
        Usuario u = new Usuario();
        u.setTituloEleitoral("T");
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        u.setUnidadeLotacao(unidade);

        when(usuarioConsultaService.buscarPorId("T")).thenReturn(u);

        usuarioFacade.adicionarAdministrador("T");

        verify(administradorService).adicionar("T");
    }

    @Test
    @DisplayName("Deve remover administrador")
    void deveRemoverAdministrador() {
        usuarioFacade.removerAdministrador("Outro", "Eu");
        verify(administradorService).remover("Outro");
    }

    @Test
    @DisplayName("Deve verificar se é administrador")
    void deveVerificarSeEhAdmin() {
        when(administradorService.isAdministrador("T")).thenReturn(true);
        assertThat(usuarioFacade.isAdministrador("T")).isTrue();
    }

    @Test
    @DisplayName("Deve buscar usuarios por titulos")
    void deveBuscarUsuariosPorTitulos() {
        Usuario u = new Usuario();
        u.setTituloEleitoral("T");
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        u.setUnidadeLotacao(unidade);
        when(usuarioConsultaService.buscarTodosPorIds(List.of("T"))).thenReturn(List.of(u));

        Map<String, UsuarioDto> map = usuarioFacade.buscarUsuariosPorTitulos(List.of("T"));
        assertThat(map).containsKey("T");
    }
}
