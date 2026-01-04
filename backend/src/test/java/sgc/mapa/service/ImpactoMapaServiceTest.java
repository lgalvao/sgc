package sgc.mapa.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoService;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Usuario;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
    private DetectorAtividadesService detectorAtividades;

    @Mock
    private AnalisadorCompetenciasService analisadorCompetencias;

    @Mock
    private MapaAcessoService mapaAcessoService;

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
        // Access tests are now covered by MapaAcessoServiceTest
        // Here we just verify that the service is called

        @Test
        @DisplayName("Deve chamar MapaAcessoService para verificar acesso")
        void deveChamarServicoAcesso() {
            // This test is kept to ensure integration/delegation, even though detailed logic is tested elsewhere
            // But since we are mocking everything, we assume the delegation works if verify passes.
            // However, ImpactoMapaService calls it before anything else relevant to this test scope.

            // mockSecurityContext(chefe); // Removed unnecessary stubbing
            // subprocesso.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
            when(subprocessoService.buscarSubprocesso(1L)).thenReturn(subprocesso);

            // Scenario 1: Access Granted (delegation happens)
            when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.empty());
            assertDoesNotThrow(() -> impactoMapaService.verificarImpactos(1L, chefe));
            org.mockito.Mockito.verify(mapaAcessoService).verificarAcessoImpacto(chefe, subprocesso);
        }

        @Test
        @DisplayName("Deve propagar erro se acesso negado")
        void devePropagarErroAcesso() {
            // mockSecurityContext(chefe); // Removed unnecessary stubbing
            // subprocesso.setSituacao(REVISAO_CADASTRO_DISPONIBILIZADA);
            when(subprocessoService.buscarSubprocesso(1L)).thenReturn(subprocesso);
            org.mockito.Mockito.doThrow(new ErroAccessoNegado("Acesso negado"))
                    .when(mapaAcessoService).verificarAcessoImpacto(chefe, subprocesso);

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
            // mockSecurityContext(chefe); // Removed unnecessary stubbing
            // subprocesso.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
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
            // mockSecurityContext(chefe); // Removed unnecessary stubbing
            // subprocesso.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
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

            when(detectorAtividades.detectarRemovidas(any(), any(), any())).thenReturn(List.of(removida));
            when(detectorAtividades.detectarInseridas(any(), any())).thenReturn(List.of());
            when(detectorAtividades.detectarAlteradas(any(), any(), any())).thenReturn(List.of());
            when(analisadorCompetencias.identificarCompetenciasImpactadas(any(), any(), any(), any())).thenReturn(List.of());


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
