package sgc.organizacao.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.repo.ComumRepo;
import sgc.organizacao.model.ResponsabilidadeRepo;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioRepo;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do UsuarioConsultaService")
class UsuarioConsultaServiceTest {

    @Mock
    private UsuarioRepo usuarioRepo;

    @Mock
    private ResponsabilidadeRepo responsabilidadeRepo;

    @Mock
    private ComumRepo repo;

    @InjectMocks
    private UsuarioConsultaService usuarioConsultaService;

    @Test
    @DisplayName("Deve buscar usuário por ID")
    void deveBuscarPorId() {
        Usuario usuario = new Usuario();
        when(repo.buscar(Usuario.class, "user")).thenReturn(usuario);
        assertEquals(usuario, usuarioConsultaService.buscarPorId("user"));
    }

    @Test
    @DisplayName("Deve buscar usuário por ID opcional")
    void deveBuscarPorIdOpcional() {
        Usuario usuario = new Usuario();
        when(usuarioRepo.findById("user")).thenReturn(Optional.of(usuario));
        assertTrue(usuarioConsultaService.buscarPorIdOpcional("user").isPresent());
    }

    @Test
    @DisplayName("Deve buscar por unidade de lotação")
    void deveBuscarPorUnidadeLotacao() {
        usuarioConsultaService.buscarPorUnidadeLotacao(1L);
        when(usuarioRepo.findByUnidadeLotacaoCodigo(1L)).thenReturn(List.of());
        assertTrue(usuarioConsultaService.buscarPorUnidadeLotacao(1L).isEmpty());
    }

    @Test
    @DisplayName("Deve buscar por email")
    void deveBuscarPorEmail() {
        usuarioConsultaService.buscarPorEmail("email");
        when(usuarioRepo.findByEmail("email")).thenReturn(Optional.empty());
        assertFalse(usuarioConsultaService.buscarPorEmail("email").isPresent());
    }

    @Test
    @DisplayName("Deve buscar todos")
    void deveBuscarTodos() {
        usuarioConsultaService.buscarTodos();
        when(usuarioRepo.findAll()).thenReturn(List.of());
        assertTrue(usuarioConsultaService.buscarTodos().isEmpty());
    }

    @Test
    @DisplayName("Deve buscar todos por IDs")
    void deveBuscarTodosPorIds() {
        usuarioConsultaService.buscarTodosPorIds(List.of("u"));
        when(usuarioRepo.findAllById(List.of("u"))).thenReturn(List.of());
        assertTrue(usuarioConsultaService.buscarTodosPorIds(List.of("u")).isEmpty());
    }
    
    @Test
    @DisplayName("Deve buscar por ID com atribuições")
    void deveBuscarPorIdComAtribuicoes() {
        Usuario u = new Usuario();
        when(repo.buscar(Usuario.class, "u")).thenReturn(u);
        assertEquals(u, usuarioConsultaService.buscarPorIdComAtribuicoes("u"));
    }

    @Test
    @DisplayName("Deve buscar por ID com atribuições opcional")
    void deveBuscarPorIdComAtribuicoesOpcional() {
        Usuario u = new Usuario();
        when(usuarioRepo.findByIdWithAtribuicoes("u")).thenReturn(Optional.of(u));
        assertTrue(usuarioConsultaService.buscarPorIdComAtribuicoesOpcional("u").isPresent());
    }
}
