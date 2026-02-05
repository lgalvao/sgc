package sgc.seguranca.login;

import org.junit.jupiter.api.DisplayName;
import org.springframework.test.util.ReflectionTestUtils;
import sgc.comum.erros.ErroAutenticacao;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroAcessoNegado;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.dto.UnidadeDto;
import sgc.organizacao.mapper.UsuarioMapper;
import sgc.organizacao.model.*;
import sgc.organizacao.service.UsuarioPerfilService;
import sgc.seguranca.login.dto.EntrarRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("LoginFacade - Gap Logic Tests")
class LoginFacadeGapTest {

    @Mock private UsuarioFacade usuarioService;
    @Mock private GerenciadorJwt gerenciadorJwt;
    @Mock private ClienteAcessoAd clienteAcessoAd;
    @Mock private UnidadeFacade unidadeService;
    @Mock private UsuarioMapper usuarioMapper;
    @Mock private UsuarioPerfilService usuarioPerfilService;

    @InjectMocks
    private LoginFacade loginFacade;

    @Test
    @DisplayName("Deve negar acesso se perfil não corresponder (branch coverage)")
    void deveNegarSePerfilNaoCorresponder() {
        // Request pede ADMIN na unidade 1
        EntrarRequest req = new EntrarRequest("123", "ADMIN", 1L);

        // Usuario tem GESTOR na unidade 1
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123");
        
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setSituacao(SituacaoUnidade.ATIVA);

        UsuarioPerfil up = new UsuarioPerfil();
        up.setPerfil(Perfil.GESTOR);
        up.setUnidade(unidade);
        up.setUnidadeCodigo(1L);
        up.setUsuario(usuario);

        when(usuarioService.carregarUsuarioParaAutenticacao("123")).thenReturn(usuario);
        when(usuarioPerfilService.buscarPorUsuario("123")).thenReturn(List.of(up));
        
        // Mock mapper (para evitar NPE no stream map)
        when(usuarioMapper.toUnidadeDtoComElegibilidadeCalculada(unidade))
            .thenReturn(UnidadeDto.builder().codigo(1L).build());

        assertThatThrownBy(() -> loginFacade.entrar(req))
                .isInstanceOf(ErroAcessoNegado.class);
    }

    @Test
    @DisplayName("Deve negar acesso se unidade não corresponder (branch coverage)")
    void deveNegarSeUnidadeNaoCorresponder() {
        // Request pede ADMIN na unidade 1
        EntrarRequest req = new EntrarRequest("123", "ADMIN", 1L);

        // Usuario tem ADMIN na unidade 2
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123");
        
        Unidade unidade = new Unidade();
        unidade.setCodigo(2L);
        unidade.setSituacao(SituacaoUnidade.ATIVA);

        UsuarioPerfil up = new UsuarioPerfil();
        up.setPerfil(Perfil.ADMIN);
        up.setUnidade(unidade);
        up.setUnidadeCodigo(2L);
        up.setUsuario(usuario);

        when(usuarioService.carregarUsuarioParaAutenticacao("123")).thenReturn(usuario);
        when(usuarioPerfilService.buscarPorUsuario("123")).thenReturn(List.of(up));

        // Mock mapper
        when(usuarioMapper.toUnidadeDtoComElegibilidadeCalculada(unidade))
            .thenReturn(UnidadeDto.builder().codigo(2L).build());

        assertThatThrownBy(() -> loginFacade.entrar(req))
                .isInstanceOf(ErroAcessoNegado.class);
    }

    @Test
    @DisplayName("Deve negar acesso se unidade estiver inativa (filtro de situação)")
    void deveNegarAcessoComUnidadeInativa() {
        // Request pede ADMIN na unidade 1 que está INATIVA
        EntrarRequest req = new EntrarRequest("123", "ADMIN", 1L);

        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("123");
        
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setSituacao(SituacaoUnidade.INATIVA); // Unidade inativa será filtrada

        UsuarioPerfil up = new UsuarioPerfil();
        up.setPerfil(Perfil.ADMIN);
        up.setUnidade(unidade);
        up.setUnidadeCodigo(1L);
        up.setUsuario(usuario);

        when(usuarioService.carregarUsuarioParaAutenticacao("123")).thenReturn(usuario);
        when(usuarioPerfilService.buscarPorUsuario("123")).thenReturn(List.of(up));

        // Não precisa mockar o mapper porque unidade será filtrada antes do map

        // Deve negar porque unidade está INATIVA (será filtrada linha 132)
        assertThatThrownBy(() -> loginFacade.entrar(req))
                .isInstanceOf(ErroAcessoNegado.class);
    }

    @Test
    @DisplayName("Deve lançar ErroAutenticacao quando usuário não é encontrado")
    void deveLancarErroQuandoUsuarioNaoEncontrado() {
        when(usuarioService.carregarUsuarioParaAutenticacao("999")).thenReturn(null);
        
        assertThatThrownBy(() -> loginFacade.autorizar("999"))
                .isInstanceOf(ErroAutenticacao.class)
                .hasMessageContaining("Credenciais inválidas");
    }

    @Test
    @DisplayName("Deve delegar para ClienteAcessoAd quando não estiver em ambiente de testes")
    void deveDelegarParaLdapEmProducao() {
        // Simular ambiente de produção
        ReflectionTestUtils.setField(loginFacade, "ambienteTestes", false);
        
        when(clienteAcessoAd.autenticar("789", "senha")).thenReturn(true);
        
        boolean resultado = loginFacade.autenticar("789", "senha");
        
        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando falha autenticação no AD")
    void deveRetornarFalseQuandoFalhaAd() {
        ReflectionTestUtils.setField(loginFacade, "ambienteTestes", false);
        
        when(clienteAcessoAd.autenticar("789", "senha")).thenThrow(new ErroAutenticacao("Erro AD"));
        
        boolean resultado = loginFacade.autenticar("789", "senha");
        
        assertThat(resultado).isFalse();
    }

    @Test
    @DisplayName("Deve retornar false quando ClienteAcessoAd é nulo em produção")
    void deveRetornarFalseQuandoClienteAdNulo() {
        LoginFacade localFacade = new LoginFacade(usuarioService, gerenciadorJwt, null, unidadeService, usuarioMapper, usuarioPerfilService);
        ReflectionTestUtils.setField(localFacade, "ambienteTestes", false);
        
        boolean resultado = localFacade.autenticar("123", "senha");
        
        assertThat(resultado).isFalse();
    }
}