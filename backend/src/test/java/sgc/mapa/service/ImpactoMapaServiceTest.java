package sgc.mapa.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.comum.erros.*;
import sgc.comum.model.*;
import sgc.mapa.dto.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;
import sgc.seguranca.*;
import sgc.subprocesso.model.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImpactoMapaServiceTest {
    @Mock
    private MapaRepo mapaRepo;
    @Mock
    private CompetenciaRepo competenciaRepo;
    @Mock
    private MapaManutencaoService mapaManutencaoService;
    @Mock
    private SgcPermissionEvaluator permissionEvaluator;
    @Mock
    private ComumRepo repo;

    @InjectMocks
    private ImpactoMapaService impactoMapaService;

    @BeforeEach
    void setUp() {
        // Default permission behavior for tests unless overridden
        lenient().doReturn(true).when(permissionEvaluator).checkPermission(any(), any(), any());
    }

    @Test
    @DisplayName("verificarImpactos - deve lancar ErroAcessoNegado quando nao tiver permissao")
    void verificarImpactosDeveLancarErroAcessoNegado() {
        Subprocesso subprocesso = new Subprocesso();
        Usuario usuario = new Usuario();

        doReturn(false).when(permissionEvaluator).checkPermission(usuario, subprocesso, "VERIFICAR_IMPACTOS");

        assertThrows(sgc.comum.erros.ErroAcessoNegado.class, () ->
            impactoMapaService.verificarImpactos(subprocesso, usuario));
    }

    // Helper para criar usuário ADMIN para testes genéricos
    private Usuario usuarioAdmin() {
        Usuario u = new Usuario();
        u.setPerfilAtivo(Perfil.ADMIN);
        return u;
    }

    @Test
    @DisplayName("Deve retornar sem impacto se não houver mapa vigente")
    void semMapaVigente() {
        Usuario usuario = usuarioAdmin();
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        subprocesso.setUnidade(unidade);

        when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.empty());

        ImpactoMapaResponse result = impactoMapaService.verificarImpactos(subprocesso, usuario);

        assertNotNull(result);
        assertTrue(result.inseridas().isEmpty());
        assertTrue(result.removidas().isEmpty());
        assertTrue(result.alteradas().isEmpty());
    }

    @Test
    @DisplayName("Deve detectar atividade inserida")
    void deveDetectarInserida() {
        Usuario usuario = usuarioAdmin();
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
        subprocesso.setCodigo(10L);
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        subprocesso.setUnidade(unidade);

        Mapa mapaVigente = new Mapa();
        mapaVigente.setCodigo(100L);
        Mapa mapaSub = new Mapa();
        mapaSub.setCodigo(200L);

        when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(mapaVigente));
        when(mapaRepo.findBySubprocessoCodigo(10L)).thenReturn(Optional.of(mapaSub));

        // Vigente: vazio
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(100L)).thenReturn(Collections.emptyList());

        // Atual: 1 atividade
        Atividade nova = new Atividade();
        nova.setCodigo(1L);
        nova.setDescricao("Nova");
        nova.setConhecimentos(Collections.emptySet());
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(200L)).thenReturn(Collections.singletonList(nova));

        when(competenciaRepo.findByMapa_Codigo(100L)).thenReturn(Collections.emptyList());

        ImpactoMapaResponse result = impactoMapaService.verificarImpactos(subprocesso, usuario);

        assertEquals(1, result.inseridas().size());
        assertEquals("Nova", result.inseridas().getFirst().descricao());
    }

    @Test
    @DisplayName("Deve detectar atividade removida e impacto em competência")
    void deveDetectarRemovida() {
        Usuario usuario = usuarioAdmin();
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
        subprocesso.setCodigo(10L);
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        subprocesso.setUnidade(unidade);

        Mapa mapaVigente = new Mapa();
        mapaVigente.setCodigo(100L);
        Mapa mapaSub = new Mapa();
        mapaSub.setCodigo(200L);

        when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(mapaVigente));
        when(mapaRepo.findBySubprocessoCodigo(10L)).thenReturn(Optional.of(mapaSub));

        // Vigente: 1 atividade "Antiga"
        Atividade antiga = new Atividade();
        antiga.setCodigo(1L);
        antiga.setDescricao("Antiga");
        antiga.setConhecimentos(Collections.emptySet());
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(100L)).thenReturn(Collections.singletonList(antiga));

        // Atual: vazio (foi removida)
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(200L)).thenReturn(Collections.emptyList());

        // Competencia ligada à atividade antiga
        Competencia comp = new Competencia();
        comp.setCodigo(50L);
        comp.setDescricao("Comp A");
        comp.setAtividades(Set.of(antiga));
        when(competenciaRepo.findByMapa_Codigo(100L)).thenReturn(Collections.singletonList(comp));

        ImpactoMapaResponse result = impactoMapaService.verificarImpactos(subprocesso, usuario);

        assertEquals(1, result.removidas().size());
        assertEquals("Antiga", result.removidas().getFirst().descricao());

        assertEquals(1, result.competenciasImpactadas().size());
        assertEquals("Comp A", result.competenciasImpactadas().getFirst().descricao());
    }

    @Test
    @DisplayName("Deve detectar atividade alterada (conhecimentos diferentes)")
    void deveDetectarAlterada() {
        Usuario usuario = usuarioAdmin();
        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
        subprocesso.setCodigo(10L);
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        subprocesso.setUnidade(unidade);

        Mapa mapaVigente = new Mapa();
        mapaVigente.setCodigo(100L);
        Mapa mapaSub = new Mapa();
        mapaSub.setCodigo(200L);

        when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(mapaVigente));
        when(mapaRepo.findBySubprocessoCodigo(10L)).thenReturn(Optional.of(mapaSub));

        // Vigente: Ativ A com Conhecimento C1
        Atividade ativVigente = new Atividade();
        ativVigente.setCodigo(1L);
        ativVigente.setDescricao("Ativ A");
        Conhecimento c1 = new Conhecimento();
        c1.setDescricao("C1");
        ativVigente.setConhecimentos(Set.of(c1));

        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(100L)).thenReturn(Collections.singletonList(ativVigente));

        // Atual: Ativ A com Conhecimento C2 (alterado)
        Atividade ativAtual = new Atividade();
        ativAtual.setCodigo(2L);
        ativAtual.setDescricao("Ativ A");
        Conhecimento c2 = new Conhecimento();
        c2.setDescricao("C2");
        ativAtual.setConhecimentos(Set.of(c2));

        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(200L)).thenReturn(Collections.singletonList(ativAtual));

        when(competenciaRepo.findByMapa_Codigo(100L)).thenReturn(Collections.emptyList());

        ImpactoMapaResponse result = impactoMapaService.verificarImpactos(subprocesso, usuario);

        assertEquals(1, result.alteradas().size());
        assertEquals("Ativ A", result.alteradas().getFirst().descricao());
    }

    @Test
    void deveDetectarConhecimentosDiferentesComAmbosVazios() {
        Mapa mapaVigente = Mapa.builder().codigo(100L).build();
        Mapa mapaAtual = Mapa.builder().codigo(200L).build();
        Subprocesso subprocesso = criarSubprocesso(1L, mapaAtual);
        subprocesso.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);

        Usuario usuario = usuarioAdmin();

        Atividade ativVigente = Atividade.builder()
                .codigo(1L)
                .descricao("Ativ A")
                .conhecimentos(Set.of())
                .build();

        Atividade ativAtual = Atividade.builder()
                .codigo(2L)
                .descricao("Ativ A")
                .conhecimentos(Set.of())
                .build();

        when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(mapaVigente));
        when(mapaRepo.findBySubprocessoCodigo(1L)).thenReturn(Optional.of(mapaAtual));
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(100L))
                .thenReturn(Collections.singletonList(ativVigente));
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(200L))
                .thenReturn(Collections.singletonList(ativAtual));
        when(competenciaRepo.findByMapa_Codigo(100L)).thenReturn(Collections.emptyList());

        ImpactoMapaResponse result = impactoMapaService.verificarImpactos(subprocesso, usuario);

        assertEquals(0, result.alteradas().size());
    }

    @Test
    void deveDetectarAtividadesComConhecimentosDiferentes() {
        Mapa mapaVigente = Mapa.builder().codigo(100L).build();
        Mapa mapaAtual = Mapa.builder().codigo(200L).build();
        Subprocesso subprocesso = criarSubprocesso(1L, mapaAtual);
        subprocesso.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);

        Usuario usuario = usuarioAdmin();

        Conhecimento c1 = Conhecimento.builder().codigo(1L).descricao("C1").build();
        Conhecimento c2 = Conhecimento.builder().codigo(2L).descricao("C2").build();
        Conhecimento c3 = Conhecimento.builder().codigo(3L).descricao("C3").build();

        Atividade ativVigente = Atividade.builder()
                .codigo(1L)
                .descricao("Ativ Teste")
                .conhecimentos(Set.of(c1))
                .build();

        Atividade ativAtual = Atividade.builder()
                .codigo(2L)
                .descricao("Ativ Teste")
                .conhecimentos(Set.of(c2, c3))
                .build();

        when(mapaRepo.findMapaVigenteByUnidade(1L)).thenReturn(Optional.of(mapaVigente));
        when(mapaRepo.findBySubprocessoCodigo(1L)).thenReturn(Optional.of(mapaAtual));
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(100L))
                .thenReturn(Collections.singletonList(ativVigente));
        when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(200L))
                .thenReturn(Collections.singletonList(ativAtual));
        when(competenciaRepo.findByMapa_Codigo(100L)).thenReturn(Collections.emptyList());

        ImpactoMapaResponse result = impactoMapaService.verificarImpactos(subprocesso, usuario);

        assertEquals(1, result.alteradas().size());
        assertEquals("Ativ Teste", result.alteradas().getFirst().descricao());
    }

    private Subprocesso criarSubprocesso(Long unidadeCodigo, Mapa mapa) {
        Unidade unidade = Unidade.builder()
                .codigo(unidadeCodigo)
                .sigla("UNID")
                .nome("Unidade Teste")
                .build();
        return Subprocesso.builder()
                .codigo(1L)
                .unidade(unidade)
                .mapa(mapa)
                .build();
    }

    @Nested
    @DisplayName("Cobertura Extra")
    class Coverage {

        @Test
        @DisplayName("verificarImpactos: Falha quando mapa do subprocesso não existe")
        void verificarImpactos_MapaSubprocessoInexistente() {
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(1L);
            sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
            Unidade u = new Unidade();
            u.setCodigo(100L);
            sp.setUnidade(u);
            Usuario usuario = usuarioAdmin();

            when(mapaRepo.findMapaVigenteByUnidade(100L)).thenReturn(Optional.of(new Mapa()));
            when(mapaRepo.findBySubprocessoCodigo(1L)).thenReturn(Optional.empty());

            assertThrows(ErroEntidadeNaoEncontrada.class, () -> impactoMapaService.verificarImpactos(sp, usuario));
        }

        @Test
        @DisplayName("verificarImpactos: Atividades duplicadas (mesma descrição) usa handler de colisão")
        void verificarImpactos_AtividadesDuplicadas() {
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(1L);
            sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
            Unidade u = new Unidade();
            u.setCodigo(100L);
            sp.setUnidade(u);

            Mapa mapaVigente = new Mapa();
            mapaVigente.setCodigo(20L);

            Mapa mapaSub = new Mapa();
            mapaSub.setCodigo(21L);

            when(mapaRepo.findMapaVigenteByUnidade(100L)).thenReturn(Optional.of(mapaVigente));
            when(mapaRepo.findBySubprocessoCodigo(1L)).thenReturn(Optional.of(mapaSub));

            Atividade a1 = new Atividade();
            a1.setCodigo(10L);
            a1.setDescricao("Mesma");

            Atividade a2 = new Atividade();
            a2.setCodigo(11L);
            a2.setDescricao("Mesma");

            when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(20L))
                    .thenReturn(List.of(a1, a2));
            when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(21L))
                    .thenReturn(List.of());

            when(competenciaRepo.findByMapa_Codigo(20L)).thenReturn(Collections.emptyList());

            ImpactoMapaResponse response = impactoMapaService.verificarImpactos(sp, usuarioAdmin());

            assertEquals(2, response.removidas().size());
        }

        @Test
        @DisplayName("verificarImpactos: Atividade Alterada vinculada a Competencia gera impacto")
        void verificarImpactos_AtividadeAlteradaComCompetencia() {
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(1L);
            sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
            Unidade u = new Unidade();
            u.setCodigo(100L);
            sp.setUnidade(u);

            Mapa mapaVigente = new Mapa();
            mapaVigente.setCodigo(20L);

            Mapa mapaSub = new Mapa();
            mapaSub.setCodigo(21L);

            when(mapaRepo.findMapaVigenteByUnidade(100L)).thenReturn(Optional.of(mapaVigente));
            when(mapaRepo.findBySubprocessoCodigo(1L)).thenReturn(Optional.of(mapaSub));

            Atividade aVigente = new Atividade();
            aVigente.setCodigo(10L);
            aVigente.setDescricao("Ativ 1");
            aVigente.setConhecimentos(Set.of(new Conhecimento()));

            Atividade aAtual = new Atividade();
            aAtual.setCodigo(10L);
            aAtual.setDescricao("Ativ 1");
            aAtual.setConhecimentos(Set.of());

            when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(20L))
                    .thenReturn(List.of(aVigente));
            when(mapaManutencaoService.buscarAtividadesPorMapaCodigoComConhecimentos(21L))
                    .thenReturn(List.of(aAtual));

            Competencia comp = new Competencia();
            comp.setCodigo(50L);
            comp.setDescricao("Comp A");
            comp.setAtividades(Set.of(aVigente));

            when(competenciaRepo.findByMapa_Codigo(20L)).thenReturn(List.of(comp));

            ImpactoMapaResponse response = impactoMapaService.verificarImpactos(sp, usuarioAdmin());

            assertEquals(1, response.alteradas().size());
            assertEquals(1, response.competenciasImpactadas().size());
        }
    }
}
