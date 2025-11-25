package sgc.mapa.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.util.Set;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.mapa.dto.ImpactoMapaDto;
import sgc.sgrh.model.Perfil;
import sgc.sgrh.model.Usuario;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.Unidade;
import sgc.mapa.model.MapaRepo;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para ImpactoMapaService")
class ImpactoMapaServiceTest {

    @InjectMocks
    private ImpactoMapaService impactoMapaService;

    @Mock
    private SubprocessoRepo subprocessoRepo;

    @Mock
    private MapaRepo mapaRepo;

    @Mock
    private ImpactoAtividadeService impactoAtividadeService;

    @Mock
    private ImpactoCompetenciaService impactoCompetenciaService;

    private Usuario chefe;
    private Usuario gestor;
    private Usuario admin;
    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        chefe = new Usuario();
        chefe.setPerfis(Set.of(Perfil.CHEFE));

        gestor = new Usuario();
        gestor.setPerfis(Set.of(Perfil.GESTOR));

        admin = new Usuario();
        admin.setPerfis(Set.of(Perfil.ADMIN));

        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);

        subprocesso = new Subprocesso();
        subprocesso.setCodigo(1L);
        subprocesso.setUnidade(unidade);
    }

    @Nested
    @DisplayName("Testes de verificação de acesso")
    class AcessoTestes {

        @Test
        @DisplayName("CHEFE pode acessar quando situação for REVISAO_CADASTRO_EM_ANDAMENTO")
        void chefePodeAcessar() {
            subprocesso.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
            when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));
            when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.empty());

            assertDoesNotThrow(() -> impactoMapaService.verificarImpactos(1L, chefe));
        }

        @Test
        @DisplayName("CHEFE não pode acessar quando situação for diferente de REVISAO_CADASTRO_EM_ANDAMENTO")
        void chefeNaoPodeAcessar() {
            subprocesso.setSituacao(REVISAO_CADASTRO_DISPONIBILIZADA);
            when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));

            assertThrows(ErroAccessoNegado.class, () -> impactoMapaService.verificarImpactos(1L, chefe));
        }

        @Test
        @DisplayName("GESTOR pode acessar quando situação for REVISAO_CADASTRO_DISPONIBILIZADA")
        void gestorPodeAcessar() {
            subprocesso.setSituacao(REVISAO_CADASTRO_DISPONIBILIZADA);
            when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));
            when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.empty());

            assertDoesNotThrow(() -> impactoMapaService.verificarImpactos(1L, gestor));
        }

        @Test
        @DisplayName("GESTOR não pode acessar quando situação for diferente de REVISAO_CADASTRO_DISPONIBILIZADA")
        void gestorNaoPodeAcessar() {
            subprocesso.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
            when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));

            assertThrows(ErroAccessoNegado.class, () -> impactoMapaService.verificarImpactos(1L, gestor));
        }

        @Test
        @DisplayName("ADMIN pode acessar quando situação for REVISAO_CADASTRO_DISPONIBILIZADA")
        void adminPodeAcessarDisponibilizada() {
            subprocesso.setSituacao(REVISAO_CADASTRO_DISPONIBILIZADA);
            when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));
            when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.empty());

            assertDoesNotThrow(() -> impactoMapaService.verificarImpactos(1L, admin));
        }

        @Test
        @DisplayName("ADMIN pode acessar quando situação for REVISAO_CADASTRO_HOMOLOGADA")
        void adminPodeAcessarHomologada() {
            subprocesso.setSituacao(REVISAO_CADASTRO_HOMOLOGADA);
            when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));
            when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.empty());

            assertDoesNotThrow(() -> impactoMapaService.verificarImpactos(1L, admin));
        }

        @Test
        @DisplayName("ADMIN pode acessar quando situação for MAPA_AJUSTADO")
        void adminPodeAcessarAjustado() {
            subprocesso.setSituacao(MAPA_AJUSTADO);
            when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));
            when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.empty());

            assertDoesNotThrow(() -> impactoMapaService.verificarImpactos(1L, admin));
        }

        @Test
        @DisplayName("ADMIN não pode acessar quando situação for diferente das permitidas")
        void adminNaoPodeAcessar() {
            subprocesso.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
            when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));

            assertThrows(ErroAccessoNegado.class, () -> impactoMapaService.verificarImpactos(1L, admin));
        }
    }

    @Nested
    @DisplayName("Testes de detecção de impactos")
    class ImpactoTestes {

        @Test
        @DisplayName("Deve retornar sem impacto se não houver mapa vigente")
        void semImpactoSeNaoHouverMapaVigente() {
            subprocesso.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
            when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));
            when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.empty());

            ImpactoMapaDto resultado = impactoMapaService.verificarImpactos(1L, chefe);

            assertFalse(resultado.isTemImpactos());
        }

        @Test
        @DisplayName("Deve chamar os services de impacto e retornar o DTO com os resultados")
        void comImpacto() {
            subprocesso.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
            sgc.mapa.model.Mapa mapaVigente = new sgc.mapa.model.Mapa();
            sgc.mapa.model.Mapa mapaSubprocesso = new sgc.mapa.model.Mapa();

            when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));
            when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(mapaVigente));
            when(mapaRepo.findBySubprocessoCodigo(1L)).thenReturn(Optional.of(mapaSubprocesso));

            impactoMapaService.verificarImpactos(1L, chefe);

            // TODO: verificar se os services de impacto foram chamados com os parâmetros corretos
        }
    }
}
