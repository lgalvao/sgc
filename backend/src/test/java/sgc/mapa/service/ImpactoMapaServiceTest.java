package sgc.mapa.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import sgc.atividade.internal.model.AtividadeRepo;
import sgc.atividade.internal.model.ConhecimentoRepo;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.mapa.api.ImpactoMapaDto;
import sgc.mapa.internal.model.CompetenciaRepo;
import sgc.mapa.internal.model.MapaRepo;
import sgc.mapa.internal.service.ImpactoMapaService;
import sgc.sgrh.internal.model.Perfil;
import sgc.sgrh.internal.model.Usuario;
import sgc.subprocesso.internal.model.Subprocesso;
import sgc.subprocesso.internal.model.SubprocessoRepo;
import sgc.unidade.internal.model.Unidade;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static sgc.subprocesso.internal.model.SituacaoSubprocesso.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Testes para ImpactoMapaService")
class ImpactoMapaServiceTest {
    @Autowired
    private ImpactoMapaService impactoMapaService;

    @MockitoBean
    private SubprocessoRepo subprocessoRepo;

    @MockitoBean
    private MapaRepo mapaRepo;

    @MockitoBean
    private AtividadeRepo atividadeRepo;

    @MockitoBean
    private ConhecimentoRepo conhecimentoRepo;

    @MockitoBean
    private CompetenciaRepo competenciaRepo;

    private Usuario chefe;
    private Usuario gestor;
    private Usuario admin;
    private Subprocesso subprocesso;

    @BeforeEach
    void setUp() {
        chefe = new Usuario();
        addAtribuicao(chefe, Perfil.CHEFE);

        gestor = new Usuario();
        addAtribuicao(gestor, Perfil.GESTOR);

        admin = new Usuario();
        addAtribuicao(admin, Perfil.ADMIN);

        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);

        subprocesso = new Subprocesso();
        subprocesso.setCodigo(1L);
        subprocesso.setUnidade(unidade);
    }

    private void addAtribuicao(Usuario u, Perfil p) {
        java.util.Set<sgc.sgrh.internal.model.UsuarioPerfil> attrs = new java.util.HashSet<>();
        attrs.add(
                        sgc.sgrh.internal.model.UsuarioPerfil.builder()
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
        @DisplayName("CHEFE pode acessar quando situação for REVISAO_CADASTRO_EM_ANDAMENTO")
        void chefePodeAcessar() {
            subprocesso.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
            when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));
            when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.empty());

            assertDoesNotThrow(() -> impactoMapaService.verificarImpactos(1L, chefe));
        }

        @Test
        @DisplayName(
                "CHEFE não pode acessar quando situação for diferente de"
                        + " REVISAO_CADASTRO_EM_ANDAMENTO")
        void chefeNaoPodeAcessar() {
            subprocesso.setSituacao(REVISAO_CADASTRO_DISPONIBILIZADA);
            when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));

            assertThrows(
                    ErroAccessoNegado.class, () -> impactoMapaService.verificarImpactos(1L, chefe));
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
        @DisplayName(
                "GESTOR não pode acessar quando situação for diferente de"
                        + " REVISAO_CADASTRO_DISPONIBILIZADA")
        void gestorNaoPodeAcessar() {
            subprocesso.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
            when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));

            assertThrows(
                    ErroAccessoNegado.class,
                    () -> impactoMapaService.verificarImpactos(1L, gestor));
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
        @DisplayName("ADMIN pode acessar quando situação for REVISAO_MAPA_AJUSTADO")
        void adminPodeAcessarAjustado() {
            subprocesso.setSituacao(REVISAO_MAPA_AJUSTADO);
            when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));
            when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.empty());

            assertDoesNotThrow(() -> impactoMapaService.verificarImpactos(1L, admin));
        }

        @Test
        @DisplayName("ADMIN não pode acessar quando situação for diferente das permitidas")
        void adminNaoPodeAcessar() {
            subprocesso.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
            when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));

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
            subprocesso.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
            when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));
            when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.empty());

            ImpactoMapaDto resultado = impactoMapaService.verificarImpactos(1L, chefe);

            assertFalse(resultado.isTemImpactos());
        }

        @Test
        @DisplayName("Deve detectar impactos quando há diferenças entre mapas")
        void comImpacto() {
            subprocesso.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
            sgc.mapa.internal.model.Mapa mapaVigente = new sgc.mapa.internal.model.Mapa();
            mapaVigente.setCodigo(1L);
            sgc.mapa.internal.model.Mapa mapaSubprocesso = new sgc.mapa.internal.model.Mapa();
            mapaSubprocesso.setCodigo(2L);

            when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));
            when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(mapaVigente));
            when(mapaRepo.findBySubprocessoCodigo(1L)).thenReturn(Optional.of(mapaSubprocesso));
            when(atividadeRepo.findByMapaCodigoWithConhecimentos(anyLong())).thenReturn(List.of());
            when(competenciaRepo.findByMapaCodigo(anyLong())).thenReturn(List.of());

            ImpactoMapaDto resultado = impactoMapaService.verificarImpactos(1L, chefe);

            assertNotNull(resultado);
        }

        @Test
        @DisplayName("Deve detectar atividade inserida quando existe no mapa atual mas não no vigente")
        void deveDetectarAtividadeInserida() {
            subprocesso.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
            sgc.mapa.internal.model.Mapa mapaVigente = new sgc.mapa.internal.model.Mapa();
            mapaVigente.setCodigo(1L);
            sgc.mapa.internal.model.Mapa mapaSubprocesso = new sgc.mapa.internal.model.Mapa();
            mapaSubprocesso.setCodigo(2L);

            // Atividade nova (não existe no vigente)
            sgc.atividade.internal.model.Atividade atividadeNova = new sgc.atividade.internal.model.Atividade();
            atividadeNova.setCodigo(10L);
            atividadeNova.setDescricao("Nova atividade");
            atividadeNova.setConhecimentos(List.of());

            when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));
            when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(mapaVigente));
            when(mapaRepo.findBySubprocessoCodigo(1L)).thenReturn(Optional.of(mapaSubprocesso));
            when(atividadeRepo.findByMapaCodigoWithConhecimentos(2L)).thenReturn(List.of(atividadeNova));
            when(atividadeRepo.findByMapaCodigoWithConhecimentos(1L)).thenReturn(List.of());
            when(competenciaRepo.findByMapaCodigo(anyLong())).thenReturn(List.of());

            ImpactoMapaDto resultado = impactoMapaService.verificarImpactos(1L, chefe);

            assertTrue(resultado.isTemImpactos());
            assertEquals(1, resultado.getTotalAtividadesInseridas());
        }

        @Test
        @DisplayName("Deve detectar atividade removida quando existe no vigente mas não no atual")
        void deveDetectarAtividadeRemovida() {
            subprocesso.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
            sgc.mapa.internal.model.Mapa mapaVigente = new sgc.mapa.internal.model.Mapa();
            mapaVigente.setCodigo(1L);
            sgc.mapa.internal.model.Mapa mapaSubprocesso = new sgc.mapa.internal.model.Mapa();
            mapaSubprocesso.setCodigo(2L);

            // Atividade que será removida (existe no vigente, não no atual)
            sgc.atividade.internal.model.Atividade atividadeVigente = new sgc.atividade.internal.model.Atividade();
            atividadeVigente.setCodigo(5L);
            atividadeVigente.setDescricao("Atividade removida");
            atividadeVigente.setConhecimentos(List.of());
            atividadeVigente.setCompetencias(new java.util.HashSet<>());

            when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));
            when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(mapaVigente));
            when(mapaRepo.findBySubprocessoCodigo(1L)).thenReturn(Optional.of(mapaSubprocesso));
            when(atividadeRepo.findByMapaCodigoWithConhecimentos(2L)).thenReturn(List.of()); // Mapa atual vazio
            when(atividadeRepo.findByMapaCodigoWithConhecimentos(1L)).thenReturn(List.of(atividadeVigente));
            when(competenciaRepo.findByMapaCodigo(anyLong())).thenReturn(List.of());

            ImpactoMapaDto resultado = impactoMapaService.verificarImpactos(1L, chefe);

            assertTrue(resultado.isTemImpactos());
            assertEquals(1, resultado.getTotalAtividadesRemovidas());
        }

        @Test
        @DisplayName("Deve detectar atividade alterada quando conhecimentos são diferentes")
        void deveDetectarAtividadeAlterada() {
            subprocesso.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
            sgc.mapa.internal.model.Mapa mapaVigente = new sgc.mapa.internal.model.Mapa();
            mapaVigente.setCodigo(1L);
            sgc.mapa.internal.model.Mapa mapaSubprocesso = new sgc.mapa.internal.model.Mapa();
            mapaSubprocesso.setCodigo(2L);

            // Atividade atual com conhecimento novo
            sgc.atividade.internal.model.Conhecimento conhecimentoNovo = new sgc.atividade.internal.model.Conhecimento();
            conhecimentoNovo.setDescricao("Conhecimento novo");

            sgc.atividade.internal.model.Atividade atividadeAtual = new sgc.atividade.internal.model.Atividade();
            atividadeAtual.setCodigo(10L);
            atividadeAtual.setDescricao("Atividade existente");
            atividadeAtual.setConhecimentos(List.of(conhecimentoNovo));
            atividadeAtual.setCompetencias(new java.util.HashSet<>());

            // Atividade vigente sem conhecimento (mesma descrição)
            sgc.atividade.internal.model.Atividade atividadeVigente = new sgc.atividade.internal.model.Atividade();
            atividadeVigente.setCodigo(5L);
            atividadeVigente.setDescricao("Atividade existente");
            atividadeVigente.setConhecimentos(List.of());
            atividadeVigente.setCompetencias(new java.util.HashSet<>());

            when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));
            when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(mapaVigente));
            when(mapaRepo.findBySubprocessoCodigo(1L)).thenReturn(Optional.of(mapaSubprocesso));
            when(atividadeRepo.findByMapaCodigoWithConhecimentos(2L)).thenReturn(List.of(atividadeAtual));
            when(atividadeRepo.findByMapaCodigoWithConhecimentos(1L)).thenReturn(List.of(atividadeVigente));
            when(competenciaRepo.findByMapaCodigo(anyLong())).thenReturn(List.of());

            ImpactoMapaDto resultado = impactoMapaService.verificarImpactos(1L, chefe);

            assertTrue(resultado.isTemImpactos());
            assertEquals(1, resultado.getTotalAtividadesAlteradas());
        }

        @Test
        @DisplayName("Deve detectar atividades removidas E alteradas simultaneamente")
        void deveDetectarAtividadesRemovidasEAlteradas() {
            subprocesso.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
            sgc.mapa.internal.model.Mapa mapaVigente = new sgc.mapa.internal.model.Mapa();
            mapaVigente.setCodigo(1L);
            sgc.mapa.internal.model.Mapa mapaSubprocesso = new sgc.mapa.internal.model.Mapa();
            mapaSubprocesso.setCodigo(2L);

            // Conhecimento para causar alteração
            sgc.atividade.internal.model.Conhecimento conhecimentoNovo = new sgc.atividade.internal.model.Conhecimento();
            conhecimentoNovo.setDescricao("Conhecimento adicionado");

            // Atividade atual com alteração
            sgc.atividade.internal.model.Atividade atividadeAtual = new sgc.atividade.internal.model.Atividade();
            atividadeAtual.setCodigo(10L);
            atividadeAtual.setDescricao("Atividade alterada");
            atividadeAtual.setConhecimentos(List.of(conhecimentoNovo));
            atividadeAtual.setCompetencias(new java.util.HashSet<>());

            // Atividade vigente sem alteração
            sgc.atividade.internal.model.Atividade atividadeVigenteMantida = new sgc.atividade.internal.model.Atividade();
            atividadeVigenteMantida.setCodigo(5L);
            atividadeVigenteMantida.setDescricao("Atividade alterada");
            atividadeVigenteMantida.setConhecimentos(List.of());
            atividadeVigenteMantida.setCompetencias(new java.util.HashSet<>());

            // Atividade removida
            sgc.atividade.internal.model.Atividade atividadeVigenteRemovida = new sgc.atividade.internal.model.Atividade();
            atividadeVigenteRemovida.setCodigo(6L);
            atividadeVigenteRemovida.setDescricao("Atividade removida");
            atividadeVigenteRemovida.setConhecimentos(List.of());
            atividadeVigenteRemovida.setCompetencias(new java.util.HashSet<>());

            when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));
            when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(mapaVigente));
            when(mapaRepo.findBySubprocessoCodigo(1L)).thenReturn(Optional.of(mapaSubprocesso));
            when(atividadeRepo.findByMapaCodigoWithConhecimentos(2L)).thenReturn(List.of(atividadeAtual));
            when(atividadeRepo.findByMapaCodigoWithConhecimentos(1L)).thenReturn(List.of(atividadeVigenteMantida, atividadeVigenteRemovida));
            when(competenciaRepo.findByMapaCodigo(anyLong())).thenReturn(List.of());

            ImpactoMapaDto resultado = impactoMapaService.verificarImpactos(1L, chefe);

            assertTrue(resultado.isTemImpactos());
            assertEquals(1, resultado.getTotalAtividadesRemovidas());
            assertEquals(1, resultado.getTotalAtividadesAlteradas());
        }

        @Test
        @DisplayName("Não deve ter impacto quando mapas são idênticos")
        void naoDeveHaverImpactoQuandoMapasIdenticos() {
            subprocesso.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
            sgc.mapa.internal.model.Mapa mapaVigente = new sgc.mapa.internal.model.Mapa();
            mapaVigente.setCodigo(1L);
            sgc.mapa.internal.model.Mapa mapaSubprocesso = new sgc.mapa.internal.model.Mapa();
            mapaSubprocesso.setCodigo(2L);

            // Mesma atividade em ambos os mapas
            sgc.atividade.internal.model.Atividade atividade = new sgc.atividade.internal.model.Atividade();
            atividade.setCodigo(5L);
            atividade.setDescricao("Atividade comum");
            atividade.setConhecimentos(List.of());
            atividade.setCompetencias(new java.util.HashSet<>());

            when(subprocessoRepo.findById(1L)).thenReturn(Optional.of(subprocesso));
            when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(mapaVigente));
            when(mapaRepo.findBySubprocessoCodigo(1L)).thenReturn(Optional.of(mapaSubprocesso));
            when(atividadeRepo.findByMapaCodigoWithConhecimentos(2L)).thenReturn(List.of(atividade));
            when(atividadeRepo.findByMapaCodigoWithConhecimentos(1L)).thenReturn(List.of(atividade));
            when(competenciaRepo.findByMapaCodigo(anyLong())).thenReturn(List.of());

            ImpactoMapaDto resultado = impactoMapaService.verificarImpactos(1L, chefe);

            assertFalse(resultado.isTemImpactos());
        }
    }
}
