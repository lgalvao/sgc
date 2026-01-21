package sgc.organizacao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.comum.repo.RepositorioComum;
import sgc.organizacao.dto.AdministradorDto;
import sgc.organizacao.dto.PerfilDto;
import sgc.organizacao.dto.ResponsavelDto;
import sgc.organizacao.dto.UsuarioDto;
import sgc.organizacao.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes de Cobertura para UsuarioFacade")
class UsuarioFacadeCoverageTest {

    @InjectMocks
    private UsuarioFacade usuarioFacade;

    @Mock private UsuarioRepo usuarioRepo;
    @Mock private UsuarioPerfilRepo usuarioPerfilRepo;
    @Mock private AdministradorRepo administradorRepo;
    @Mock private RepositorioComum repo;
    @Mock private UnidadeRepo unidadeRepo;

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

        assertThrows(ErroAccessoNegado.class, () -> usuarioFacade.obterUsuarioAutenticado());
    }

    @Test
    @DisplayName("Deve retornar lista vazia se usuário não encontrado ao buscar unidades de responsabilidade")
    void deveRetornarVaziaSeUsuarioNaoEncontradoNoResponsavel() {
        when(usuarioRepo.findByIdWithAtribuicoes("T")).thenReturn(Optional.empty());
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
    @DisplayName("Deve buscar por id delegando para repositório comum")
    void deveBuscarPorId() {
        Usuario u = new Usuario();
        u.setTituloEleitoral("T");
        when(repo.buscar(Usuario.class, "T")).thenReturn(u);
        
        Usuario result = usuarioFacade.buscarPorId("T");
        assertThat(result.getTituloEleitoral()).isEqualTo("T");
    }

    @Test
    @DisplayName("Deve carregar usuário para autenticação")
    void deveCarregarUsuarioParaAutenticacao() {
        Usuario u = mock(Usuario.class);
        when(u.getTituloEleitoral()).thenReturn("T");
        when(usuarioRepo.findByIdWithAtribuicoes("T")).thenReturn(Optional.of(u));
        when(usuarioPerfilRepo.findByUsuarioTitulo("T")).thenReturn(Collections.emptyList());

        Usuario result = usuarioFacade.carregarUsuarioParaAutenticacao("T");
        assertThat(result).isNotNull();
        verify(u).getAuthorities(); // Garante cobertura da linha usuario.getAuthorities()
    }

    @Test
    @DisplayName("Deve lançar erro se usuário não encontrado por login")
    void deveLancarErroSeUsuarioNaoEncontradoPorLogin() {
        when(usuarioRepo.findByIdWithAtribuicoes("T")).thenReturn(Optional.empty());
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
        when(usuarioRepo.findById("T")).thenReturn(Optional.of(u));

        Optional<UsuarioDto> dto = usuarioFacade.buscarUsuarioPorTitulo("T");
        assertThat(dto).isPresent();
        assertThat(dto.get().getTituloEleitoral()).isEqualTo("T");
    }

    @Test
    @DisplayName("Deve buscar usuários por unidade")
    void deveBuscarUsuariosPorUnidade() {
        Usuario u = new Usuario();
        u.setTituloEleitoral("T");
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        u.setUnidadeLotacao(unidade);

        when(usuarioRepo.findByUnidadeLotacaoCodigo(1L)).thenReturn(List.of(u));

        List<UsuarioDto> lista = usuarioFacade.buscarUsuariosPorUnidade(1L);
        assertThat(lista).hasSize(1);
    }

    @Test
    @DisplayName("Deve retornar vazio ao buscar perfis de usuário inexistente")
    void deveRetornarVazioAoBuscarPerfisDeUsuarioInexistente() {
        when(usuarioRepo.findByIdWithAtribuicoes("T")).thenReturn(Optional.empty());
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

        UsuarioPerfil up = new UsuarioPerfil();
        up.setUsuario(u);
        up.setUnidade(unidade);
        up.setPerfil(Perfil.CHEFE);

        // Simula usuario encontrado mas sem atribuicoes carregadas inicialmente
        when(usuarioRepo.findByIdWithAtribuicoes("T")).thenReturn(Optional.of(u));
        when(usuarioPerfilRepo.findByUsuarioTitulo("T")).thenReturn(List.of(up));

        List<PerfilDto> perfis = usuarioFacade.buscarPerfisUsuario("T");
        assertThat(perfis).hasSize(1);
        assertThat(perfis.getFirst().getPerfil()).isEqualTo("CHEFE");
    }

    @Test
    @DisplayName("Deve buscar responsável atual")
    void deveBuscarResponsavelAtual() {
        Unidade un = new Unidade();
        un.setCodigo(1L);
        when(unidadeRepo.findBySigla("SIGLA")).thenReturn(Optional.of(un));

        Usuario chefeSimples = new Usuario();
        chefeSimples.setTituloEleitoral("C");
        when(usuarioRepo.chefePorCodUnidade(1L)).thenReturn(Optional.of(chefeSimples));

        Usuario chefeCompleto = new Usuario();
        chefeCompleto.setTituloEleitoral("C");
        when(usuarioRepo.findByIdWithAtribuicoes("C")).thenReturn(Optional.of(chefeCompleto));

        Usuario res = usuarioFacade.buscarResponsavelAtual("SIGLA");
        assertThat(res).isNotNull();
    }

    @Test
    @DisplayName("Deve lançar erro ao buscar responsável de unidade inexistente")
    void deveLancarErroResponsavelUnidadeInexistente() {
        when(unidadeRepo.findBySigla("SIGLA")).thenReturn(Optional.empty());
        assertThrows(ErroEntidadeNaoEncontrada.class, () -> usuarioFacade.buscarResponsavelAtual("SIGLA"));
    }

    @Test
    @DisplayName("Deve lançar erro ao buscar responsável não encontrado")
    void deveLancarErroResponsavelNaoEncontrado() {
        Unidade un = new Unidade();
        un.setCodigo(1L);
        when(unidadeRepo.findBySigla("SIGLA")).thenReturn(Optional.of(un));
        when(usuarioRepo.chefePorCodUnidade(1L)).thenReturn(Optional.empty());

        assertThrows(ErroEntidadeNaoEncontrada.class, () -> usuarioFacade.buscarResponsavelAtual("SIGLA"));
    }

    @Test
    @DisplayName("Deve lançar erro ao carregar dados do responsável")
    void deveLancarErroCarregarDadosResponsavel() {
        Unidade un = new Unidade();
        un.setCodigo(1L);
        when(unidadeRepo.findBySigla("SIGLA")).thenReturn(Optional.of(un));

        Usuario chefeSimples = new Usuario();
        chefeSimples.setTituloEleitoral("C");
        when(usuarioRepo.chefePorCodUnidade(1L)).thenReturn(Optional.of(chefeSimples));
        when(usuarioRepo.findByIdWithAtribuicoes("C")).thenReturn(Optional.empty());

        assertThrows(ErroEntidadeNaoEncontrada.class, () -> usuarioFacade.buscarResponsavelAtual("SIGLA"));
    }

    @Test
    @DisplayName("Deve buscar responsável por código da unidade")
    void deveBuscarResponsavelPorCodigoUnidade() {
        Usuario u = new Usuario();
        u.setTituloEleitoral("T");
        u.setNome("Nome");
        when(usuarioRepo.findChefesByUnidadesCodigos(List.of(1L))).thenReturn(List.of(u));

        ResponsavelDto resp = usuarioFacade.buscarResponsavelUnidade(1L);
        assertThat(resp.getTitularTitulo()).isEqualTo("T");
    }

    @Test
    @DisplayName("Deve lançar erro se responsável por código não encontrado")
    void deveLancarErroResponsavelPorCodigoNaoEncontrado() {
        when(usuarioRepo.findChefesByUnidadesCodigos(List.of(1L))).thenReturn(Collections.emptyList());
        assertThrows(ErroEntidadeNaoEncontrada.class, () -> usuarioFacade.buscarResponsavelUnidade(1L));
    }

    @Test
    @DisplayName("Deve retornar mapa vazio ao buscar responsáveis de lista vazia")
    void deveRetornarMapaVazioResponsaveis() {
        assertThat(usuarioFacade.buscarResponsaveisUnidades(Collections.emptyList())).isEmpty();

        when(usuarioRepo.findChefesByUnidadesCodigos(List.of(1L))).thenReturn(Collections.emptyList());
        assertThat(usuarioFacade.buscarResponsaveisUnidades(List.of(1L))).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar responsáveis de múltiplas unidades")
    void deveBuscarResponsaveisMultiplasUnidades() {
        Usuario u = new Usuario();
        u.setTituloEleitoral("T");
        u.setNome("Nome");

        UsuarioPerfil up = new UsuarioPerfil();
        up.setUsuario(u);
        up.setUsuarioTitulo("T");
        up.setUnidadeCodigo(1L);
        up.setPerfil(Perfil.CHEFE);
        u.setAtribuicoes(Set.of(up));

        when(usuarioRepo.findChefesByUnidadesCodigos(List.of(1L))).thenReturn(List.of(u));
        when(usuarioRepo.findByIdInWithAtribuicoes(List.of("T"))).thenReturn(List.of(u));
        // carregarAtribuicoesEmLote vai chamar findByUsuarioTituloIn
        when(usuarioPerfilRepo.findByUsuarioTituloIn(List.of("T"))).thenReturn(List.of(up));

        Map<Long, ResponsavelDto> map = usuarioFacade.buscarResponsaveisUnidades(List.of(1L));
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
        when(usuarioRepo.findByEmail("email")).thenReturn(Optional.of(u));

        assertThat(usuarioFacade.buscarUsuarioPorEmail("email")).isPresent();
    }

    @Test
    @DisplayName("Deve buscar usuários ativos")
    void deveBuscarUsuariosAtivos() {
        when(usuarioRepo.findAll()).thenReturn(Collections.emptyList());
        assertThat(usuarioFacade.buscarUsuariosAtivos()).isEmpty();
    }

    @Test
    @DisplayName("Deve verificar se usuário tem perfil")
    void deveVerificarUsuarioTemPerfil() {
        when(usuarioRepo.findByIdWithAtribuicoes("T")).thenReturn(Optional.empty());
        assertThat(usuarioFacade.usuarioTemPerfil("T", "CHEFE", 1L)).isFalse();

        Usuario u = new Usuario();
        u.setTituloEleitoral("T");
        UsuarioPerfil up = new UsuarioPerfil();
        up.setUsuario(u);
        up.setUnidadeCodigo(1L);
        up.setPerfil(Perfil.CHEFE);

        when(usuarioRepo.findByIdWithAtribuicoes("T")).thenReturn(Optional.of(u));
        when(usuarioPerfilRepo.findByUsuarioTitulo("T")).thenReturn(List.of(up));

        assertThat(usuarioFacade.usuarioTemPerfil("T", "CHEFE", 1L)).isTrue();
    }

    @Test
    @DisplayName("Deve buscar unidades por perfil")
    void deveBuscarUnidadesPorPerfil() {
        when(usuarioRepo.findByIdWithAtribuicoes("T")).thenReturn(Optional.empty());
        assertThat(usuarioFacade.buscarUnidadesPorPerfil("T", "CHEFE")).isEmpty();

        Usuario u = new Usuario();
        u.setTituloEleitoral("T");
        UsuarioPerfil up = new UsuarioPerfil();
        up.setUsuario(u);
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        up.setUnidade(unidade);
        up.setPerfil(Perfil.CHEFE);

        when(usuarioRepo.findByIdWithAtribuicoes("T")).thenReturn(Optional.of(u));
        when(usuarioPerfilRepo.findByUsuarioTitulo("T")).thenReturn(List.of(up));

        assertThat(usuarioFacade.buscarUnidadesPorPerfil("T", "CHEFE")).contains(1L);
    }

    @Test
    @DisplayName("Deve listar administradores")
    void deveListarAdministradores() {
        Administrador admin = new Administrador();
        admin.setUsuarioTitulo("T");
        when(administradorRepo.findAll()).thenReturn(List.of(admin));

        Usuario u = new Usuario();
        u.setTituloEleitoral("T");
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        u.setUnidadeLotacao(unidade);
        when(usuarioRepo.findById("T")).thenReturn(Optional.of(u));

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

        when(repo.buscar(Usuario.class, "T")).thenReturn(u);
        when(administradorRepo.existsById("T")).thenReturn(false);

        usuarioFacade.adicionarAdministrador("T");

        verify(administradorRepo).save(any(Administrador.class));
    }

    @Test
    @DisplayName("Deve lançar erro ao adicionar administrador existente")
    void deveLancarErroAdicionarAdminExistente() {
        when(repo.buscar(Usuario.class, "T")).thenReturn(new Usuario());
        when(administradorRepo.existsById("T")).thenReturn(true);

        assertThrows(ErroValidacao.class, () -> usuarioFacade.adicionarAdministrador("T"));
    }

    @Test
    @DisplayName("Deve remover administrador")
    void deveRemoverAdministrador() {
        when(administradorRepo.existsById("Outro")).thenReturn(true);
        when(administradorRepo.count()).thenReturn(2L);

        usuarioFacade.removerAdministrador("Outro", "Eu");

        verify(administradorRepo).deleteById("Outro");
    }

    @Test
    @DisplayName("Deve lançar erro ao remover a si mesmo")
    void deveLancarErroRemoverSiMesmo() {
        assertThrows(ErroValidacao.class, () -> usuarioFacade.removerAdministrador("Eu", "Eu"));
    }

    @Test
    @DisplayName("Deve lançar erro ao remover não admin")
    void deveLancarErroRemoverNaoAdmin() {
        when(administradorRepo.existsById("Outro")).thenReturn(false);
        assertThrows(ErroValidacao.class, () -> usuarioFacade.removerAdministrador("Outro", "Eu"));
    }

    @Test
    @DisplayName("Deve lançar erro ao remover único admin")
    void deveLancarErroRemoverUnicoAdmin() {
        when(administradorRepo.existsById("Outro")).thenReturn(true);
        when(administradorRepo.count()).thenReturn(1L);
        assertThrows(ErroValidacao.class, () -> usuarioFacade.removerAdministrador("Outro", "Eu"));
    }

    @Test
    @DisplayName("Deve verificar se é administrador")
    void deveVerificarSeEhAdmin() {
        when(administradorRepo.existsById("T")).thenReturn(true);
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
        when(usuarioRepo.findAllById(List.of("T"))).thenReturn(List.of(u));

        Map<String, UsuarioDto> map = usuarioFacade.buscarUsuariosPorTitulos(List.of("T"));
        assertThat(map).containsKey("T");
    }
}
