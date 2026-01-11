package sgc.mapa.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.mapa.dto.ImpactoMapaDto;
import sgc.mapa.model.*;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.seguranca.acesso.AccessControlService;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoFacade;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes para ImpactoMapaService")
class ImpactoMapaServiceTest {
    @InjectMocks
    private ImpactoMapaService impactoMapaService;

    @Mock
    private SubprocessoFacade subprocessoFacade;

    @Mock
    private MapaRepo mapaRepo;

    @Mock
    private CompetenciaRepo competenciaRepo;

    @Mock
    private AtividadeService atividadeService;

    @Mock
    private AccessControlService accessControlService;

    private Usuario chefe;
    private Usuario gestor;
    private Usuario admin;
    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        chefe = new Usuario();
        chefe.setTituloEleitoral("111");
        addAtribuicao(chefe, Perfil.CHEFE);

        gestor = new Usuario();
        gestor.setTituloEleitoral("222");
        addAtribuicao(gestor, Perfil.GESTOR);

        admin = new Usuario();
        admin.setTituloEleitoral("333");
        addAtribuicao(admin, Perfil.ADMIN);

        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);

        subprocesso = new Subprocesso();
        subprocesso.setCodigo(1L);
        subprocesso.setUnidade(unidade);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void addAtribuicao(Usuario u, Perfil p) {
        java.util.Set<sgc.organizacao.model.UsuarioPerfil> attrs = new java.util.HashSet<>();
        attrs.add(
                        sgc.organizacao.model.UsuarioPerfil.builder()
                                .usuario(u)
                                .unidade(new Unidade())
                                .perfil(p)
                                .build());
        u.setAtribuicoes(attrs);
    }

    @Nested
    @DisplayName("Testes de verificação de acesso")
    class AcessoTestes {
        // Access tests are now covered by AccessControlService and SubprocessoAccessPolicy
        // Here we just verify that the service is called

        @Test
        @DisplayName("Deve chamar AccessControlService para verificar acesso")
        void deveChamarServicoAcesso() {
            // Após refatoração de segurança, verificação de acesso é feita via AccessControlService
            when(subprocessoFacade.buscarSubprocesso(1L)).thenReturn(subprocesso);

            // Scenario 1: Access Granted (delegation happens)
            when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.empty());
            assertDoesNotThrow(() -> impactoMapaService.verificarImpactos(1L, chefe));
            org.mockito.Mockito.verify(accessControlService).verificarPermissao(
                org.mockito.Mockito.eq(chefe), 
                org.mockito.Mockito.eq(sgc.seguranca.acesso.Acao.VERIFICAR_IMPACTOS), 
                org.mockito.Mockito.eq(subprocesso)
            );
        }

        @Test
        @DisplayName("Deve propagar erro se acesso negado")
        void devePropagarErroAcesso() {
            when(subprocessoFacade.buscarSubprocesso(1L)).thenReturn(subprocesso);
            org.mockito.Mockito.doThrow(new ErroAccessoNegado("Acesso negado"))
                    .when(accessControlService).verificarPermissao(
                        org.mockito.Mockito.any(Usuario.class),
                        org.mockito.Mockito.any(sgc.seguranca.acesso.Acao.class),
                        org.mockito.Mockito.any()
                    );

            assertThrows(
                    ErroAccessoNegado.class, () -> impactoMapaService.verificarImpactos(1L, chefe));
        }
    }

    @Nested
    @DisplayName("Testes de detecção de impactos")
    class ImpactoTestes {

        @Test
        @DisplayName("Deve retornar sem impacto se não houver mapa vigente")
        void semImpactoSeNaoHouverMapaVigente() {
            when(subprocessoFacade.buscarSubprocesso(1L)).thenReturn(subprocesso);
            when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.empty());

            ImpactoMapaDto resultado = impactoMapaService.verificarImpactos(1L, chefe);

            assertNotNull(resultado);
            assertFalse(resultado.isTemImpactos());
            assertNotNull(resultado.getAtividadesInseridas());
            assertTrue(resultado.getAtividadesInseridas().isEmpty());
            assertNotNull(resultado.getAtividadesRemovidas());
            assertTrue(resultado.getAtividadesRemovidas().isEmpty());
            assertNotNull(resultado.getAtividadesAlteradas());
            assertTrue(resultado.getAtividadesAlteradas().isEmpty());
        }

        @Test
        @DisplayName("Deve detectar impactos quando há diferenças entre mapas")
        void comImpacto() {
            Mapa mapaVigente = new Mapa();
            mapaVigente.setCodigo(1L);
            Mapa mapaSubprocesso = new Mapa();
            mapaSubprocesso.setCodigo(2L);

            when(subprocessoFacade.buscarSubprocesso(1L)).thenReturn(subprocesso);
            when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(mapaVigente));
            when(mapaRepo.findBySubprocessoCodigo(1L)).thenReturn(Optional.of(mapaSubprocesso));

            // Setup atividades - vigente tem A1, subprocesso está vazio (A1 foi removida)
            Atividade a1 = new Atividade();
            a1.setCodigo(100L);
            a1.setDescricao("Ativ 1");
            a1.setConhecimentos(new ArrayList<>());

            when(atividadeService.buscarPorMapaCodigoComConhecimentos(1L)).thenReturn(List.of(a1)); // Vigente
            when(atividadeService.buscarPorMapaCodigoComConhecimentos(2L)).thenReturn(List.of()); // Subprocesso (vazio)

            // Setup competências - sem competências impactadas para simplificar
            when(competenciaRepo.findByMapaCodigo(anyLong())).thenReturn(List.of());

            ImpactoMapaDto resultado = impactoMapaService.verificarImpactos(1L, chefe);

            assertNotNull(resultado);
            // Verificar que os campos não são nulos
            assertNotNull(resultado.getAtividadesInseridas());
            assertNotNull(resultado.getAtividadesRemovidas());
            assertNotNull(resultado.getAtividadesAlteradas());
            assertNotNull(resultado.getCompetenciasImpactadas());
            // Verificar que detectou a remoção
            assertFalse(resultado.getAtividadesRemovidas().isEmpty());
            assertEquals(1, resultado.getAtividadesRemovidas().size());
            assertEquals("Ativ 1", resultado.getAtividadesRemovidas().get(0).getDescricao());
        }
    }
}
