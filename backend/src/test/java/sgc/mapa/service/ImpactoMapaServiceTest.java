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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("Testes para ImpactoMapaService")
class ImpactoMapaServiceTest {
    @InjectMocks
    private ImpactoMapaService impactoMapaService;

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

        @Test
        @DisplayName("Deve chamar AccessControlService para verificar acesso")
        void deveChamarServicoAcesso() {
            // Após refatoração de segurança, verificação de acesso é feita via AccessControlService
            
            // Scenario 1: Access Granted (delegation happens)
            when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.empty());
            assertDoesNotThrow(() -> impactoMapaService.verificarImpactos(subprocesso, chefe));
            verify(accessControlService).verificarPermissao(
                eq(chefe), 
                eq(sgc.seguranca.acesso.Acao.VERIFICAR_IMPACTOS), 
                eq(subprocesso)
            );
        }

        @Test
        @DisplayName("Deve propagar erro se acesso negado")
        void devePropagarErroAcesso() {
            doThrow(new ErroAccessoNegado("Acesso negado"))
                    .when(accessControlService).verificarPermissao(
                        any(Usuario.class),
                        any(sgc.seguranca.acesso.Acao.class),
                        any()
                    );

            assertThrows(
                    ErroAccessoNegado.class, () -> impactoMapaService.verificarImpactos(subprocesso, chefe));
        }
    }

    @Nested
    @DisplayName("Testes de detecção de impactos")
    class ImpactoTestes {

        @Test
        @DisplayName("Deve retornar sem impacto se não houver mapa vigente")
        void semImpactoSeNaoHouverMapaVigente() {
            when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.empty());

            ImpactoMapaDto resultado = impactoMapaService.verificarImpactos(subprocesso, chefe);

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

            when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(mapaVigente));
            when(mapaRepo.findBySubprocessoCodigo(1L)).thenReturn(Optional.of(mapaSubprocesso));

            // Setup atividades
            // Vigente: A1
            Atividade a1Vigente = new Atividade();
            a1Vigente.setCodigo(100L);
            a1Vigente.setDescricao("Ativ 1");
            a1Vigente.setConhecimentos(new ArrayList<>());

            // Subprocesso: A2 (Inserida), A1 com novo conhecimento (Alterada)
            Atividade a1Atual = new Atividade();
            a1Atual.setCodigo(101L);
            a1Atual.setDescricao("Ativ 1");
            a1Atual.setConhecimentos(List.of(new Conhecimento(1L, "C1", a1Atual)));

            Atividade a2Atual = new Atividade();
            a2Atual.setCodigo(200L);
            a2Atual.setDescricao("Ativ 2");
            a2Atual.setConhecimentos(new ArrayList<>());

            when(atividadeService.buscarPorMapaCodigoComConhecimentos(1L)).thenReturn(List.of(a1Vigente));
            when(atividadeService.buscarPorMapaCodigoComConhecimentos(2L)).thenReturn(List.of(a1Atual, a2Atual));

            // Setup competências: C1 ligada a A1Vigente
            Competencia c1 = new Competencia();
            c1.setCodigo(500L);
            c1.setDescricao("Comp 1");
            c1.setAtividades(new java.util.HashSet<>(List.of(a1Vigente)));

            when(competenciaRepo.findByMapaCodigo(1L)).thenReturn(List.of(c1));

            ImpactoMapaDto resultado = impactoMapaService.verificarImpactos(subprocesso, chefe);

            assertNotNull(resultado);
            // Inseriu A2
            assertEquals(1, resultado.getAtividadesInseridas().size());
            assertEquals("Ativ 2", resultado.getAtividadesInseridas().getFirst().getDescricao());
            
            // Alterou A1 (conhecimentos)
            assertEquals(1, resultado.getAtividadesAlteradas().size());
            assertEquals("Ativ 1", resultado.getAtividadesAlteradas().getFirst().getDescricao());

            // Impactou C1 (pois A1 ligada a ela foi alterada)
            assertEquals(1, resultado.getCompetenciasImpactadas().size());
            assertEquals("Comp 1", resultado.getCompetenciasImpactadas().getFirst().getDescricao());
        }

        @Test
        @DisplayName("Deve detectar remoção e seu impacto em competência")
        void detectarRemocaoEImpacto() {
            Mapa mapaVigente = new Mapa();
            mapaVigente.setCodigo(1L);
            Mapa mapaSubprocesso = new Mapa();
            mapaSubprocesso.setCodigo(2L);

            when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(mapaVigente));
            when(mapaRepo.findBySubprocessoCodigo(1L)).thenReturn(Optional.of(mapaSubprocesso));

            Atividade a1Vigente = new Atividade();
            a1Vigente.setCodigo(100L);
            a1Vigente.setDescricao("Ativ 1");

            when(atividadeService.buscarPorMapaCodigoComConhecimentos(1L)).thenReturn(List.of(a1Vigente));
            when(atividadeService.buscarPorMapaCodigoComConhecimentos(2L)).thenReturn(List.of());

            Competencia c1 = new Competencia();
            c1.setCodigo(500L);
            c1.setDescricao("Comp 1");
            c1.setAtividades(new java.util.HashSet<>(List.of(a1Vigente)));

            when(competenciaRepo.findByMapaCodigo(1L)).thenReturn(List.of(c1));

            ImpactoMapaDto resultado = impactoMapaService.verificarImpactos(subprocesso, chefe);

            assertEquals(1, resultado.getAtividadesRemovidas().size());
            assertEquals(1, resultado.getCompetenciasImpactadas().size());
            assertEquals(sgc.mapa.model.TipoImpactoCompetencia.ATIVIDADE_REMOVIDA, 
                         resultado.getCompetenciasImpactadas().getFirst().getTiposImpacto().getFirst());
        }
    }
}
