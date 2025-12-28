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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.mapa.dto.ImpactoMapaDto;
import sgc.mapa.model.AtividadeRepo;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.model.ConhecimentoRepo;
import sgc.mapa.model.MapaRepo;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoService;
import sgc.unidade.model.Unidade;
import sgc.usuario.model.Perfil;
import sgc.usuario.model.Usuario;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

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
        java.util.Set<sgc.usuario.model.UsuarioPerfil> attrs = new java.util.HashSet<>();
        attrs.add(
                        sgc.usuario.model.UsuarioPerfil.builder()
                                .usuario(u)
                                .unidade(new Unidade())
                                .perfil(p)
                                .build());
        u.setAtribuicoes(attrs);
    }

    private void mockSecurityContext(Usuario usuario) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(usuario);
        // Delegate getAuthorities to the real usuario object which calculates them from assignments
        org.mockito.Mockito.doAnswer(invocation -> usuario.getAuthorities()).when(authentication).getAuthorities();

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Nested
    @DisplayName("Testes de verificação de acesso")
    class AcessoTestes {

        @Test
        @DisplayName("CHEFE pode acessar quando situação for REVISAO_CADASTRO_EM_ANDAMENTO")
        void chefePodeAcessar() {
            mockSecurityContext(chefe);
            subprocesso.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
            when(subprocessoService.buscarSubprocesso(1L)).thenReturn(subprocesso);
            when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.empty());

            assertDoesNotThrow(() -> impactoMapaService.verificarImpactos(1L, chefe));
        }

        @Test
        @DisplayName(
                "CHEFE não pode acessar quando situação for diferente de"
                        + " REVISAO_CADASTRO_EM_ANDAMENTO")
        void chefeNaoPodeAcessar() {
            mockSecurityContext(chefe);
            subprocesso.setSituacao(REVISAO_CADASTRO_DISPONIBILIZADA);
            when(subprocessoService.buscarSubprocesso(1L)).thenReturn(subprocesso);

            assertThrows(
                    ErroAccessoNegado.class, () -> impactoMapaService.verificarImpactos(1L, chefe));
        }

        @Test
        @DisplayName("GESTOR pode acessar quando situação for REVISAO_CADASTRO_DISPONIBILIZADA")
        void gestorPodeAcessar() {
            mockSecurityContext(gestor);
            subprocesso.setSituacao(REVISAO_CADASTRO_DISPONIBILIZADA);
            when(subprocessoService.buscarSubprocesso(1L)).thenReturn(subprocesso);
            when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.empty());

            assertDoesNotThrow(() -> impactoMapaService.verificarImpactos(1L, gestor));
        }

        @Test
        @DisplayName(
                "GESTOR não pode acessar quando situação for diferente de"
                        + " REVISAO_CADASTRO_DISPONIBILIZADA")
        void gestorNaoPodeAcessar() {
            mockSecurityContext(gestor);
            subprocesso.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
            when(subprocessoService.buscarSubprocesso(1L)).thenReturn(subprocesso);

            assertThrows(
                    ErroAccessoNegado.class,
                    () -> impactoMapaService.verificarImpactos(1L, gestor));
        }

        @Test
        @DisplayName("ADMIN pode acessar quando situação for REVISAO_CADASTRO_DISPONIBILIZADA")
        void adminPodeAcessarDisponibilizada() {
            mockSecurityContext(admin);
            subprocesso.setSituacao(REVISAO_CADASTRO_DISPONIBILIZADA);
            when(subprocessoService.buscarSubprocesso(1L)).thenReturn(subprocesso);
            when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.empty());

            assertDoesNotThrow(() -> impactoMapaService.verificarImpactos(1L, admin));
        }

        @Test
        @DisplayName("ADMIN pode acessar quando situação for REVISAO_CADASTRO_HOMOLOGADA")
        void adminPodeAcessarHomologada() {
            mockSecurityContext(admin);
            subprocesso.setSituacao(REVISAO_CADASTRO_HOMOLOGADA);
            when(subprocessoService.buscarSubprocesso(1L)).thenReturn(subprocesso);
            when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.empty());

            assertDoesNotThrow(() -> impactoMapaService.verificarImpactos(1L, admin));
        }

        @Test
        @DisplayName("ADMIN pode acessar quando situação for REVISAO_MAPA_AJUSTADO")
        void adminPodeAcessarAjustado() {
            mockSecurityContext(admin);
            subprocesso.setSituacao(REVISAO_MAPA_AJUSTADO);
            when(subprocessoService.buscarSubprocesso(1L)).thenReturn(subprocesso);
            when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.empty());

            assertDoesNotThrow(() -> impactoMapaService.verificarImpactos(1L, admin));
        }

        @Test
        @DisplayName("ADMIN não pode acessar quando situação for diferente das permitidas")
        void adminNaoPodeAcessar() {
            mockSecurityContext(admin);
            subprocesso.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
            when(subprocessoService.buscarSubprocesso(1L)).thenReturn(subprocesso);

            assertThrows(
                    ErroAccessoNegado.class, () -> impactoMapaService.verificarImpactos(1L, admin));
        }
    }

    @Nested
    @DisplayName("Testes de detecção de impactos")
    class ImpactoTestes {

        @Test
        @DisplayName("Deve retornar sem impacto se não houver mapa vigente")
        void semImpactoSeNaoHouverMapaVigente() {
            mockSecurityContext(chefe);
            subprocesso.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
            when(subprocessoService.buscarSubprocesso(1L)).thenReturn(subprocesso);
            when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.empty());

            ImpactoMapaDto resultado = impactoMapaService.verificarImpactos(1L, chefe);

            assertFalse(resultado.isTemImpactos());
        }

        @Test
        @DisplayName("Deve detectar impactos quando há diferenças entre mapas")
        void comImpacto() {
            mockSecurityContext(chefe);
            subprocesso.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
            sgc.mapa.model.Mapa mapaVigente = new sgc.mapa.model.Mapa();
            mapaVigente.setCodigo(1L);
            sgc.mapa.model.Mapa mapaSubprocesso = new sgc.mapa.model.Mapa();
            mapaSubprocesso.setCodigo(2L);

            when(subprocessoService.buscarSubprocesso(1L)).thenReturn(subprocesso);
            when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(mapaVigente));
            when(mapaRepo.findBySubprocessoCodigo(1L)).thenReturn(Optional.of(mapaSubprocesso));
            when(atividadeRepo.findByMapaCodigoWithConhecimentos(anyLong())).thenReturn(List.of());
            when(competenciaRepo.findByMapaCodigo(anyLong())).thenReturn(List.of());

            ImpactoMapaDto resultado = impactoMapaService.verificarImpactos(1L, chefe);

            assertNotNull(resultado);
        }
    }
}
