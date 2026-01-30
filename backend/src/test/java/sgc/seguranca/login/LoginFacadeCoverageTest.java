package sgc.seguranca.login;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroAutenticacao;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.mapper.UsuarioMapper;
import sgc.organizacao.model.*;
import sgc.organizacao.service.UsuarioPerfilService;
import sgc.seguranca.login.dto.EntrarRequest;
import sgc.seguranca.login.dto.PerfilUnidadeDto;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.mockito.Mockito;
import sgc.comum.erros.ErroAcessoNegado;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginFacadeCoverageTest")
class LoginFacadeCoverageTest {

    @Mock
    private UsuarioFacade usuarioService;
    @Mock
    private GerenciadorJwt gerenciadorJwt;
    @Mock
    private ClienteAcessoAd clienteAcessoAd;
    @Mock
    private UnidadeFacade unidadeService;
    @Mock
    private UsuarioMapper usuarioMapper;
    @Mock
    private UsuarioPerfilService usuarioPerfilService;

    @InjectMocks
    private LoginFacade facade;

    @BeforeEach
    void setUp() {
        Mockito.lenient().when(usuarioMapper.toUnidadeDtoComElegibilidadeCalculada(any(Unidade.class)))
                .thenAnswer(inv -> {
                    Unidade u = inv.getArgument(0);
                    if (u == null)
                        return null;
                    Unidade superior = u.getUnidadeSuperior();
                    return UnidadeDto.builder()
                            .codigo(u.getCodigo())
                            .nome(u.getNome())
                            .sigla(u.getSigla())
                            .codigoPai(superior != null ? superior.getCodigo() : null)
                            .tipo(u.getTipo() != null ? u.getTipo().name() : null)
                            .isElegivel(u.getTipo() != TipoUnidade.INTERMEDIARIA)
                            .build();
                });

        // Stubbing generico para evitar NPE se chamado
        Mockito.lenient().when(unidadeService.buscarEntidadePorId(any())).thenAnswer(inv -> {
            Unidade u = new Unidade();
            u.setCodigo(inv.getArgument(0));
            u.setSituacao(SituacaoUnidade.ATIVA);
            return u;
        });
    }

    @Test
    @DisplayName("buscarAutorizacoesInterno - Usuario Nao Encontrado")
    void buscarAutorizacoesInterno_UsuarioNaoEncontrado() {
        String titulo = "123";
        // Setup authentication state
        when(clienteAcessoAd.autenticar(any(), any())).thenReturn(true);
        facade.autenticar(titulo, "pass");

        // Mock user not found
        when(usuarioService.carregarUsuarioParaAutenticacao(titulo)).thenReturn(null);

        var exception = assertThrows(ErroAutenticacao.class, () -> facade.autorizar(titulo));
        assertNotNull(exception);
    }

    @Test
    @DisplayName("toUnidadeDto - Coverage for Branches")
    void toUnidadeDto_Coverage() {
        String titulo = "123";
        when(clienteAcessoAd.autenticar(any(), any())).thenReturn(true);
        facade.autenticar(titulo, "pass");

        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral(titulo);

        Unidade u1 = new Unidade();
        u1.setCodigo(1L);
        u1.setTipo(TipoUnidade.OPERACIONAL);
        u1.setSituacao(SituacaoUnidade.ATIVA);

        Unidade u2 = new Unidade();
        u2.setCodigo(2L);
        u2.setUnidadeSuperior(u1);
        u2.setTipo(TipoUnidade.INTERMEDIARIA);
        u2.setSituacao(SituacaoUnidade.ATIVA);

        UsuarioPerfil p1 = new UsuarioPerfil();
        p1.setPerfil(Perfil.SERVIDOR);
        p1.setUnidade(u1);
        p1.setUnidadeCodigo(1L);

        UsuarioPerfil p2 = new UsuarioPerfil();
        p2.setPerfil(Perfil.SERVIDOR);
        p2.setUnidade(u2);
        p2.setUnidadeCodigo(2L);


        when(usuarioService.carregarUsuarioParaAutenticacao(titulo)).thenReturn(usuario);
        when(usuarioPerfilService.buscarPorUsuario(titulo)).thenReturn(List.of(p1, p2));

        List<PerfilUnidadeDto> result = facade.autorizar(titulo);

        assertEquals(2, result.size());

        UnidadeDto dto1 = result.stream().filter(r -> r.unidade().getCodigo().equals(1L)).findFirst().get()
                .unidade();
        assertNull(dto1.getCodigoPai());
        assertTrue(dto1.isElegivel());

        UnidadeDto dto2 = result.stream().filter(r -> r.unidade().getCodigo().equals(2L)).findFirst().get()
                .unidade();
        assertEquals(1L, dto2.getCodigoPai());
        assertFalse(dto2.isElegivel());
    }

    @Test
    @DisplayName("entrar - Sucesso")
    void entrar_Sucesso() {
        String titulo = "123";
        Long codUnidade = 1L;
        String perfilName = "SERVIDOR";

        when(clienteAcessoAd.autenticar(any(), any())).thenReturn(true);
        facade.autenticar(titulo, "pass");

        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral(titulo);
        Unidade u1 = new Unidade();
        u1.setCodigo(codUnidade);
        u1.setSituacao(SituacaoUnidade.ATIVA);

        UsuarioPerfil p1 = new UsuarioPerfil();
        p1.setPerfil(Perfil.SERVIDOR);
        p1.setUnidade(u1);
        p1.setUnidadeCodigo(codUnidade);

        when(usuarioService.carregarUsuarioParaAutenticacao(titulo)).thenReturn(usuario);
        when(usuarioPerfilService.buscarPorUsuario(titulo)).thenReturn(List.of(p1));
        when(gerenciadorJwt.gerarToken(titulo, Perfil.SERVIDOR, codUnidade)).thenReturn("jwt-token");

        EntrarRequest request = EntrarRequest.builder()
                .tituloEleitoral(titulo)
                .perfil(perfilName)
                .unidadeCodigo(codUnidade)
                .build();

        String token = facade.entrar(request);
        assertEquals("jwt-token", token);
    }

    @Test
    @DisplayName("entrar - Acesso Negado")
    void entrar_AcessoNegado() {
        String titulo = "123";
        when(clienteAcessoAd.autenticar(any(), any())).thenReturn(true);
        facade.autenticar(titulo, "pass");

        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral(titulo);
        Unidade u1 = new Unidade();
        u1.setCodigo(1L);
        u1.setSituacao(SituacaoUnidade.ATIVA);

        UsuarioPerfil p1 = new UsuarioPerfil();
        p1.setPerfil(Perfil.SERVIDOR);
        p1.setUnidade(u1);
        p1.setUnidadeCodigo(1L);

        when(usuarioService.carregarUsuarioParaAutenticacao(titulo)).thenReturn(usuario);
        when(usuarioPerfilService.buscarPorUsuario(titulo)).thenReturn(List.of(p1));

        EntrarRequest request = EntrarRequest.builder()
                .tituloEleitoral(titulo)
                .perfil("GESTOR") // Perfil diferente
                .unidadeCodigo(1L)
                .build();

        var exception = assertThrows(ErroAcessoNegado.class, () -> facade.entrar(request));
        assertNotNull(exception);
    }

    @Test
    @DisplayName("buscarAutorizacoesInterno - Filtra Unidades Inativas")
    void buscarAutorizacoesInterno_FiltraInativas() {
        String titulo = "123";
        when(clienteAcessoAd.autenticar(any(), any())).thenReturn(true);
        facade.autenticar(titulo, "pass");

        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral(titulo);

        Unidade uAtiva = new Unidade();
        uAtiva.setCodigo(1L);
        uAtiva.setSituacao(SituacaoUnidade.ATIVA);

        Unidade uInativa = new Unidade();
        uInativa.setCodigo(2L);
        uInativa.setSituacao(SituacaoUnidade.INATIVA);

        UsuarioPerfil p1 = new UsuarioPerfil();
        p1.setPerfil(Perfil.SERVIDOR);
        p1.setUnidade(uAtiva);
        p1.setUnidadeCodigo(1L);
        p1.setUsuarioTitulo(titulo);

        UsuarioPerfil p2 = new UsuarioPerfil();
        p2.setPerfil(Perfil.GESTOR);
        p2.setUnidade(uInativa);
        p2.setUnidadeCodigo(2L);
        p2.setUsuarioTitulo(titulo);


        when(usuarioService.carregarUsuarioParaAutenticacao(titulo)).thenReturn(usuario);
        when(usuarioPerfilService.buscarPorUsuario(titulo)).thenReturn(List.of(p1, p2));

        List<PerfilUnidadeDto> result = facade.autorizar(titulo);
        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().unidade().getCodigo());
    }

    @Test
    @DisplayName("entrar - Sessao Expirada ou Invalida")
    void entrar_SessaoExpirada() {
        EntrarRequest request = EntrarRequest.builder()
                .tituloEleitoral("123")
                .perfil("SERVIDOR")
                .unidadeCodigo(1L)
                .build();

        var exception = assertThrows(ErroAutenticacao.class, () -> facade.entrar(request));
        assertNotNull(exception);
    }

    @Test
    @DisplayName("autorizar - Sem Autenticacao")
    void autorizar_SemAutenticacao() {
        var exception = assertThrows(ErroAutenticacao.class, () -> facade.autorizar("123"));
        assertNotNull(exception);
    }
}