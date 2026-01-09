package sgc.mapa.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.mapa.dto.ImpactoMapaDto;
import sgc.mapa.model.AtividadeRepo;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.ConhecimentoRepo;
import sgc.mapa.model.MapaRepo;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.seguranca.acesso.AccessControlService;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para ImpactoMapaService")
class ImpactoMapaServiceTest {
    @InjectMocks
    private ImpactoMapaService impactoMapaService;

    @Mock
    private SubprocessoService subprocessoService;

    @Mock
    private MapaRepo mapaRepo;

    @Mock
    private AtividadeRepo atividadeRepo;

    @Mock
    private ConhecimentoRepo conhecimentoRepo;

    @Mock
    private CompetenciaRepo competenciaRepo;

    @Mock
    private AtividadeService atividadeService;

    @Mock
    private DetectorMudancasAtividadeService detectorAtividades;

    @Mock
    private DetectorImpactoCompetenciaService analisadorCompetencias;

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
            when(subprocessoService.buscarSubprocesso(1L)).thenReturn(subprocesso);

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
            when(subprocessoService.buscarSubprocesso(1L)).thenReturn(subprocesso);
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
            when(subprocessoService.buscarSubprocesso(1L)).thenReturn(subprocesso);
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
            sgc.mapa.model.Mapa mapaVigente = new sgc.mapa.model.Mapa();
            mapaVigente.setCodigo(1L);
            sgc.mapa.model.Mapa mapaSubprocesso = new sgc.mapa.model.Mapa();
            mapaSubprocesso.setCodigo(2L);

            when(subprocessoService.buscarSubprocesso(1L)).thenReturn(subprocesso);
            when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(mapaVigente));
            when(mapaRepo.findBySubprocessoCodigo(1L)).thenReturn(Optional.of(mapaSubprocesso));

            // Setup for new service structure
            sgc.mapa.model.Atividade a1 = new sgc.mapa.model.Atividade();
            a1.setCodigo(100L);
            a1.setDescricao("Ativ 1");

            // Using atividadeService now instead of repo directly
            when(atividadeService.buscarPorMapaCodigoComConhecimentos(1L)).thenReturn(List.of(a1)); // Vigente
            when(atividadeService.buscarPorMapaCodigoComConhecimentos(2L)).thenReturn(List.of()); // Subprocesso (vazio, então a1 foi removida)

            when(competenciaRepo.findByMapaCodigo(anyLong())).thenReturn(List.of());

            // Mocking specialized services behavior
            sgc.mapa.dto.AtividadeImpactadaDto removida = sgc.mapa.dto.AtividadeImpactadaDto.builder()
                    .codigo(100L)
                    .descricao("Ativ 1")
                    .tipoImpacto(sgc.mapa.model.TipoImpactoAtividade.REMOVIDA)
                    .build();

            // Use specific matchers to disambiguate overloaded methods
            when(detectorAtividades.detectarRemovidas(anyMap(), anyList(), anyMap())).thenReturn(List.of(removida));
            when(detectorAtividades.detectarInseridas(anyList(), anySet())).thenReturn(List.of());
            when(detectorAtividades.detectarAlteradas(anyList(), anyMap(), anyMap())).thenReturn(List.of());
            when(analisadorCompetencias.competenciasImpactadas(anyList(), anyList(), anyList(), anyList())).thenReturn(List.of());


            ImpactoMapaDto resultado = impactoMapaService.verificarImpactos(1L, chefe);

            assertNotNull(resultado);
            // Killing NullReturnValsMutator
            assertNotNull(resultado.getAtividadesInseridas());
            assertNotNull(resultado.getAtividadesRemovidas());
            assertNotNull(resultado.getAtividadesAlteradas());
            assertNotNull(resultado.getCompetenciasImpactadas());
            // Killing EmptyObjectReturnValsMutator
            assertFalse(resultado.getAtividadesRemovidas().isEmpty());
        }
    }
}
