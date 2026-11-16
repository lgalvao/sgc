package sesel.sgc.seguranca;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import sesel.sgc.servidor.ServidorRepository;
import sesel.sgc.servidor.Servidor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
class CustomUserDetailsServiceTest {
    @MockitoBean
    private ServidorRepository servidorRepository;

    @MockitoSpyBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void deveCarregarUsuarioPorEmailComSucesso() {
        String email = "teste@exemplo.com";
        Servidor servidor = new Servidor();
        when(servidorRepository.findByEmail(email)).thenReturn(Optional.of(servidor));
        assertEquals(servidor, customUserDetailsService.loadUserByUsername(email));
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        String email = "naoexiste@exemplo.com";
        when(servidorRepository.findByEmail(email)).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> customUserDetailsService.loadUserByUsername(email));
    }
}