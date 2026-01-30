package sgc.seguranca.login;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.erros.ErroAcessoNegado;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.mapper.UsuarioMapper;
import sgc.organizacao.model.*;
import sgc.organizacao.service.UsuarioPerfilService;
import sgc.seguranca.login.dto.EntrarRequest;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import sgc.organizacao.dto.UnidadeDto;

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
}