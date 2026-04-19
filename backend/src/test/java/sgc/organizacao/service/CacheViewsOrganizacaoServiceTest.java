package sgc.organizacao.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
        UnidadeHierarquiaLeitura uhl = UnidadeHierarquiaLeitura.builder()
                .codigo(1L)
                .nome("Nome")
                .sigla("SIGLA")
                .tituloTitular("123")
                .tipo(TipoUnidade.OPERACIONAL)
                .situacao(SituacaoUnidade.ATIVA)
                .build();
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
    @DisplayName("deve listar todos os usuários através do repositório")
    void listarTodosUsuarios() {
        UsuarioConsultaLeitura usuario = UsuarioConsultaLeitura.builder()
                .tituloEleitoral("12345")
                .matricula("mat1")
                .nome("Joao")
                .email("joao@test.com")
                .ramal("1234")
                .unidadeCodigo(1L)
                .unidadeNome("Unidade")
                .unidadeSigla("UNI")
                .build();

        when(usuarioRepo.listarTodasConsultas()).thenReturn(List.of(usuario));

        List<UsuarioConsultaLeitura> result = cacheService.listarTodosUsuarios();

        assertEquals(1, result.size());
        assertEquals("12345", result.getFirst().tituloEleitoral());
        assertEquals(1L, result.getFirst().unidadeCodigo());
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

        List<UsuarioPerfilLeitura> result = cacheService.listarTodosPerfisUnidade();

        assertEquals(1, result.size());
        assertEquals("titulo", result.getFirst().usuarioTitulo());
        assertEquals(1L, result.getFirst().unidadeCodigo());
        assertEquals(Perfil.ADMIN, result.getFirst().perfil());
    }

    @Test
    @DisplayName("deve evictar perfis de unidade sem erros")
    void evictarPerfisUnidade() {
        assertDoesNotThrow(() -> cacheService.evictarPerfisUnidade());
    }
}
