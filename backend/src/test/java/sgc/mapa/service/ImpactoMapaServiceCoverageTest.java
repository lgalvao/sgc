package sgc.mapa.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import sgc.comum.erros.*;
import sgc.mapa.dto.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.seguranca.SgcPermissionEvaluator;
import sgc.subprocesso.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImpactoMapaService - Cobertura de Testes")
class ImpactoMapaServiceCoverageTest {
    @InjectMocks
    private ImpactoMapaService target;

    @Mock private MapaRepo mapaRepo;
    @Mock private SgcPermissionEvaluator permissionEvaluator;

    @Test
    @DisplayName("verificarImpactos deve lançar ErroAcessoNegado quando sem permissão")
    void verificarImpactosSemPermissao() {
        Usuario user = new Usuario();
        Subprocesso sp = new Subprocesso();
        when(permissionEvaluator.verificarPermissao(any(), any(), any())).thenReturn(false);

        assertThatThrownBy(() -> target.verificarImpactos(sp, user))
                .isInstanceOf(ErroAcessoNegado.class);
    }

    @Test
    @DisplayName("verificarImpactos deve retornar sem impacto quando não há mapa vigente")
    void verificarImpactosSemMapaVigente() {
        Usuario user = new Usuario();
        user.setPerfilAtivo(Perfil.ADMIN);
        
        Unidade u = new Unidade();
        u.setCodigo(1L);
        
        Subprocesso sp = new Subprocesso();
        sp.setUnidade(u);
        sp.setSituacao(NAO_INICIADO);
        
        when(permissionEvaluator.verificarPermissao(any(), any(), any())).thenReturn(true);
        when(mapaRepo.buscarMapaVigentePorUnidade(1L)).thenReturn(Optional.empty());

        ImpactoMapaResponse res = target.verificarImpactos(sp, user);
        assertThat(res.temImpactos()).isFalse();
    }

    @Test
    @DisplayName("verificarImpactos deve lançar ErroEntidadeNaoEncontrada quando mapa do subprocesso não existe")
    void verificarImpactosSemMapaSubprocesso() {
        Usuario user = new Usuario();
        user.setPerfilAtivo(Perfil.ADMIN);
        
        Unidade u = new Unidade();
        u.setCodigo(1L);
        
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(100L);
        sp.setUnidade(u);
        sp.setSituacao(NAO_INICIADO);
        
        when(permissionEvaluator.verificarPermissao(any(), any(), any())).thenReturn(true);
        when(mapaRepo.buscarMapaVigentePorUnidade(1L)).thenReturn(Optional.of(new Mapa()));
        when(mapaRepo.buscarPorSubprocesso(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> target.verificarImpactos(sp, user))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Nested
    @DisplayName("Testes de Situação (checkSituacao)")
    class CheckSituacaoTest {
        @Test
        @DisplayName("CHEFE - situações válidas")
        void chefeSituacoesValidas() {
            testCheckSituacao(Perfil.CHEFE, NAO_INICIADO, true);
            testCheckSituacao(Perfil.CHEFE, REVISAO_CADASTRO_EM_ANDAMENTO, true);
            testCheckSituacao(Perfil.CHEFE, MAPEAMENTO_CADASTRO_EM_ANDAMENTO, false);
        }

        @Test
        @DisplayName("GESTOR - situação válida")
        void gestorSituacoesValidas() {
            testCheckSituacao(Perfil.GESTOR, REVISAO_CADASTRO_DISPONIBILIZADA, true);
            testCheckSituacao(Perfil.GESTOR, NAO_INICIADO, false);
        }

        @Test
        @DisplayName("ADMIN - situações válidas")
        void adminSituacoesValidas() {
            testCheckSituacao(Perfil.ADMIN, NAO_INICIADO, true);
            testCheckSituacao(Perfil.ADMIN, REVISAO_CADASTRO_HOMOLOGADA, true);
            testCheckSituacao(Perfil.ADMIN, REVISAO_MAPA_AJUSTADO, true);
            testCheckSituacao(Perfil.ADMIN, MAPEAMENTO_MAPA_DISPONIBILIZADO, false);
        }

        @Test
        @DisplayName("SERVIDOR - sempre bloqueado")
        void servidorBloqueado() {
            testCheckSituacao(Perfil.SERVIDOR, NAO_INICIADO, false);
        }

        private void testCheckSituacao(Perfil perfil, SituacaoSubprocesso situacao, boolean expected) {
            Usuario user = new Usuario();
            user.setPerfilAtivo(perfil);
            Subprocesso sp = new Subprocesso();
            sp.setSituacao(situacao);
            
            if (expected) {
                assertThatCode(() -> ReflectionTestUtils.invokeMethod(target, "checkSituacao", user, sp))
                        .doesNotThrowAnyException();
            } else {
                assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(target, "checkSituacao", user, sp))
                        .isInstanceOf(ErroValidacao.class);
            }
        }
    }

    @Test
    @DisplayName("detectarAlteradas - deve considerar atividades com conhecimentos diferentes")
    void detectarAlteradasDiferentes() {
        Atividade v = new Atividade();
        v.setCodigo(1L);
        v.setDescricao("A1");
        Conhecimento c1 = new Conhecimento();
        c1.setDescricao("C1");
        v.setConhecimentos(Set.of(c1));

        Atividade a = new Atividade();
        a.setCodigo(2L);
        a.setDescricao("A1");
        Conhecimento c2 = new Conhecimento();
        c2.setDescricao("C2");
        a.setConhecimentos(Set.of(c2));

        Map<String, Atividade> vigentesMap = Map.of("A1", v);
        List<AtividadeImpactadaDto> res = ReflectionTestUtils.invokeMethod(target, "detectarAlteradas", List.of(a), vigentesMap, Map.of());
        
        assertThat(res).hasSize(1);
        assertThat(res.getFirst().tipoImpacto()).isEqualTo(TipoImpactoAtividade.ALTERADA);
    }

    @Test
    @DisplayName("conhecimentosDiferentes - casos de borda")
    void conhecimentosDiferentesBorda() {
        boolean res1 = ReflectionTestUtils.invokeMethod(target, "conhecimentosDiferentes", List.of(), List.of());
        assertThat(res1).isFalse();

        Conhecimento c1 = new Conhecimento(); c1.setDescricao("C1");
        Conhecimento c2 = new Conhecimento(); c2.setDescricao("C2");

        boolean res2 = ReflectionTestUtils.invokeMethod(target, "conhecimentosDiferentes", List.of(c1), List.of(c1, c2));
        assertThat(res2).isTrue();

        boolean res3 = ReflectionTestUtils.invokeMethod(target, "conhecimentosDiferentes", List.of(c1), List.of(c2));
        assertThat(res3).isTrue();
    }

    @Test
    @DisplayName("construirMapaAtividadeCompetencias - ignora competências sem atividades")
    void construirMapaSemAtividades() {
        Competencia comp = new Competencia();
        comp.setAtividades(Set.of());
        Map<Long, List<Competencia>> res = ReflectionTestUtils.invokeMethod(target, "construirMapaAtividadeCompetencias", List.of(comp));
        assertThat(res).isEmpty();
    }
}
