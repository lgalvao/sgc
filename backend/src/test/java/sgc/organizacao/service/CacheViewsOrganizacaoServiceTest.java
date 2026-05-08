package sgc.organizacao.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.beans.factory.*;
import org.springframework.core.env.*;
import sgc.organizacao.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheViewsOrganizacaoServiceTest {

    @Mock
    private UnidadeRepo unidadeRepo;
    @Mock
    private UsuarioRepo usuarioRepo;
    @Mock
    private ResponsabilidadeRepo responsabilidadeRepo;
    @Mock
    private AdministradorRepo administradorRepo;
    @Mock
    private UsuarioPerfilRepo usuarioPerfilRepo;
    @Mock
    private ObjectProvider<CacheViewsOrganizacaoService> selfProvider;
    @Mock
    private Environment environment;

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

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().codigo()).isEqualTo(1L);
    }

    @Test
    @DisplayName("deve evictar unidades sem erros")
    void evictarUnidades() {
        cacheService.evictarUnidades();
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

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().tituloEleitoral()).isEqualTo("12345");
        assertThat(result.getFirst().unidadeCodigo()).isEqualTo(1L);
    }

    @Test
    @DisplayName("deve evictar usuários sem erros")
    void evictarUsuarios() {
        cacheService.evictarUsuarios();
    }

    @Test
    @DisplayName("deve listar todas as responsabilidades e mapear")
    void listarTodasResponsabilidades() {
        Responsabilidade r = new Responsabilidade();
        r.setUnidadeCodigo(10L);
        r.setUsuarioTitulo("titulo1");

        when(responsabilidadeRepo.listarTodasLeituras()).thenReturn(List.of(
                new ResponsabilidadeLeitura(r.getUnidadeCodigo(), r.getUsuarioTitulo())
        ));

        List<ResponsabilidadeLeitura> result = cacheService.listarTodasResponsabilidades();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().unidadeCodigo()).isEqualTo(10L);
        assertThat(result.getFirst().usuarioTitulo()).isEqualTo("titulo1");
    }

    @Test
    @DisplayName("deve evictar responsabilidades sem erros")
    void evictarResponsabilidades() {
        cacheService.evictarResponsabilidades();
    }

    @Test
    @DisplayName("deve listar todos perfis de unidade")
    void listarTodosPerfisUnidade() {
        UsuarioConsultaLeitura administrador = UsuarioConsultaLeitura.builder()
                .tituloEleitoral("admin")
                .unidadeCompetenciaCodigo(10L)
                .build();
        UsuarioConsultaLeitura servidor = UsuarioConsultaLeitura.builder()
                .tituloEleitoral("servidor")
                .unidadeCompetenciaCodigo(10L)
                .build();
        when(usuarioRepo.listarTodasConsultas()).thenReturn(List.of(administrador, servidor));

        UnidadeHierarquiaLeitura unidadeAdmin = UnidadeHierarquiaLeitura.builder()
                .codigo(1L)
                .tipo(TipoUnidade.RAIZ)
                .situacao(SituacaoUnidade.ATIVA)
                .build();
        UnidadeHierarquiaLeitura unidadeOperacional = UnidadeHierarquiaLeitura.builder()
                .codigo(10L)
                .tipo(TipoUnidade.OPERACIONAL)
                .tituloTitular("chefe")
                .situacao(SituacaoUnidade.ATIVA)
                .build();
        UnidadeHierarquiaLeitura unidadeInteroperacional = UnidadeHierarquiaLeitura.builder()
                .codigo(20L)
                .tipo(TipoUnidade.INTEROPERACIONAL)
                .situacao(SituacaoUnidade.ATIVA)
                .build();
        when(unidadeRepo.listarEstruturasAtivas()).thenReturn(List.of(unidadeAdmin, unidadeOperacional, unidadeInteroperacional));

        Responsabilidade responsabilidadeOperacional = new Responsabilidade();
        responsabilidadeOperacional.setUnidadeCodigo(10L);
        responsabilidadeOperacional.setUsuarioTitulo("chefe");
        Responsabilidade responsabilidadeInteroperacional = new Responsabilidade();
        responsabilidadeInteroperacional.setUnidadeCodigo(20L);
        responsabilidadeInteroperacional.setUsuarioTitulo("gestor-chefe");
        when(responsabilidadeRepo.listarTodasLeituras()).thenReturn(List.of(
                new ResponsabilidadeLeitura(responsabilidadeOperacional.getUnidadeCodigo(), responsabilidadeOperacional.getUsuarioTitulo()),
                new ResponsabilidadeLeitura(responsabilidadeInteroperacional.getUnidadeCodigo(), responsabilidadeInteroperacional.getUsuarioTitulo())
        ));

        Administrador registroAdmin = new Administrador();
        registroAdmin.setUsuarioTitulo("admin");
        when(administradorRepo.findAll()).thenReturn(List.of(registroAdmin));
        when(selfProvider.getIfAvailable(org.mockito.ArgumentMatchers.any())).thenReturn(cacheService);
        when(environment.acceptsProfiles(Profiles.of("test"))).thenReturn(false);

        List<UsuarioPerfilLeitura> result = cacheService.listarTodosPerfisUnidade();

        assertThat(result).hasSize(6);
        assertThat(result)
                .contains(
                        new UsuarioPerfilLeitura("admin", 1L, Perfil.ADMIN),
                        new UsuarioPerfilLeitura("admin", 10L, Perfil.SERVIDOR),
                        new UsuarioPerfilLeitura("chefe", 10L, Perfil.CHEFE),
                        new UsuarioPerfilLeitura("gestor-chefe", 20L, Perfil.GESTOR),
                        new UsuarioPerfilLeitura("gestor-chefe", 20L, Perfil.CHEFE),
                        new UsuarioPerfilLeitura("servidor", 10L, Perfil.SERVIDOR)
                );
    }

    @Test
    @DisplayName("deve usar view de perfis como compatibilidade se bases nao gerarem perfis")
    void listarTodosPerfisUnidadeFallbackCompatibilidade() {
        UsuarioPerfil perfil = new UsuarioPerfil("titulo", 1L, Perfil.ADMIN);
        when(selfProvider.getIfAvailable(org.mockito.ArgumentMatchers.any())).thenReturn(cacheService);
        when(usuarioRepo.listarTodasConsultas()).thenReturn(List.of());
        when(unidadeRepo.listarEstruturasAtivas()).thenReturn(List.of());
        when(responsabilidadeRepo.listarTodasLeituras()).thenReturn(List.of());
        when(administradorRepo.findAll()).thenReturn(List.of());
        when(environment.acceptsProfiles(Profiles.of("test"))).thenReturn(true);
        when(usuarioPerfilRepo.findAll()).thenReturn(List.of(perfil));

        List<UsuarioPerfilLeitura> result = cacheService.listarTodosPerfisUnidade();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().usuarioTitulo()).isEqualTo("titulo");
        assertThat(result.getFirst().unidadeCodigo()).isEqualTo(1L);
        assertThat(result.getFirst().perfil()).isEqualTo(Perfil.ADMIN);
    }

    @Test
    @DisplayName("deve evictar perfis de unidade sem erros")
    void evictarPerfisUnidade() {
        cacheService.evictarPerfisUnidade();
    }
}
