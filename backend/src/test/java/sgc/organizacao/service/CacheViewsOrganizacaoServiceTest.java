package sgc.organizacao.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.organizacao.dto.UsuarioResumoDto;
import sgc.organizacao.model.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CacheViewsOrganizacaoServiceTest {

    @Mock
    private UnidadeRepo unidadeRepo;
    @Mock
    private UsuarioRepo usuarioRepo;
    @Mock
    private ResponsabilidadeRepo responsabilidadeRepo;
    @Mock
    private UsuarioPerfilRepo usuarioPerfilRepo;

    @InjectMocks
    private CacheViewsOrganizacaoService cacheService;

    @Test
    @DisplayName("deve listar todas as unidades através do repositório")
    void listarTodasUnidades() {
        UnidadeHierarquiaLeitura uhl = new UnidadeHierarquiaLeitura(1L, "Nome", "SIGLA", "123", TipoUnidade.OPERACIONAL, SituacaoUnidade.ATIVA, null);
        when(unidadeRepo.listarEstruturasAtivas()).thenReturn(List.of(uhl));

        List<UnidadeHierarquiaLeitura> result = cacheService.listarTodasUnidades();

        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().codigo());
    }

    @Test
    @DisplayName("deve evictar unidades sem erros")
    void evictarUnidades() {
        assertDoesNotThrow(() -> cacheService.evictarUnidades());
    }

    @Test
    @DisplayName("deve listar todos os usuários e mapear para dto")
    void listarTodosUsuarios() {
        Usuario usuario = new Usuario();
        usuario.setTituloEleitoral("12345");
        usuario.setMatricula("mat1");
        usuario.setNome("Joao");
        usuario.setEmail("joao@test.com");
        usuario.setRamal("1234");

        when(usuarioRepo.findAll()).thenReturn(List.of(usuario));

        List<UsuarioResumoDto> result = cacheService.listarTodosUsuarios();

        assertEquals(1, result.size());
        assertEquals("12345", result.getFirst().tituloEleitoral());
    }

    @Test
    @DisplayName("deve evictar usuários sem erros")
    void evictarUsuarios() {
        assertDoesNotThrow(() -> cacheService.evictarUsuarios());
    }

    @Test
    @DisplayName("deve listar todas as responsabilidades e mapear")
    void listarTodasResponsabilidades() {
        Responsabilidade r = new Responsabilidade();
        r.setUnidadeCodigo(10L);
        r.setUsuarioTitulo("titulo1");

        when(responsabilidadeRepo.findAll()).thenReturn(List.of(r));

        List<ResponsabilidadeLeitura> result = cacheService.listarTodasResponsabilidades();

        assertEquals(1, result.size());
        assertEquals(10L, result.getFirst().unidadeCodigo());
        assertEquals("titulo1", result.getFirst().usuarioTitulo());
    }

    @Test
    @DisplayName("deve evictar responsabilidades sem erros")
    void evictarResponsabilidades() {
        assertDoesNotThrow(() -> cacheService.evictarResponsabilidades());
    }

    @Test
    @DisplayName("deve listar todos perfis de unidade")
    void listarTodosPerfisUnidade() {
        UsuarioPerfil up = new UsuarioPerfil("titulo", 1L, Perfil.ADMIN);
        when(usuarioPerfilRepo.findAll()).thenReturn(List.of(up));

        List<UsuarioPerfil> result = cacheService.listarTodosPerfisUnidade();

        assertEquals(1, result.size());
        assertEquals("titulo", result.getFirst().getUsuarioTitulo());
    }

    @Test
    @DisplayName("deve evictar perfis de unidade sem erros")
    void evictarPerfisUnidade() {
        assertDoesNotThrow(() -> cacheService.evictarPerfisUnidade());
    }
}
