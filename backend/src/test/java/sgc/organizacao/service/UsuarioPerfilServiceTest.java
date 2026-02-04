package sgc.organizacao.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioPerfil;
import sgc.organizacao.model.UsuarioPerfilRepo;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do UsuarioPerfilService")
class UsuarioPerfilServiceTest {

    @Mock
    private UsuarioPerfilRepo usuarioPerfilRepo;

    @InjectMocks
    private UsuarioPerfilService usuarioPerfilService;

    @Test
    @DisplayName("Deve buscar por usuário")
    void deveBuscarPorUsuario() {
        when(usuarioPerfilRepo.findByUsuarioTitulo("user")).thenReturn(List.of());
        assertTrue(usuarioPerfilService.buscarPorUsuario("user").isEmpty());
    }

    @Test
    @DisplayName("Deve carregar authorities")
    void deveCarregarAuthorities() {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("user");
        UsuarioPerfil up = new UsuarioPerfil();
        up.setPerfil(Perfil.ADMIN);
        
        when(usuarioPerfilRepo.findByUsuarioTitulo("user")).thenReturn(List.of(up));
        
        usuarioPerfilService.carregarAuthorities(usuario);
        
        assertNotNull(usuario.getAuthorities());
        assertEquals(1, usuario.getAuthorities().size());
    }

    @Test
    @DisplayName("Deve buscar atribuições para cache")
    void deveBuscarAtribuicoesParaCache() {
        UsuarioPerfil up = new UsuarioPerfil();
        when(usuarioPerfilRepo.findByUsuarioTitulo("user")).thenReturn(List.of(up));
        
        Set<UsuarioPerfil> result = usuarioPerfilService.buscarAtribuicoesParaCache("user");
        assertEquals(1, result.size());
        assertTrue(result.contains(up));
    }
}
