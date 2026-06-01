package sgc.mapa.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.comum.erros.*;
import sgc.mapa.dto.*;
import sgc.mapa.model.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;
import sgc.seguranca.*;
import sgc.subprocesso.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static sgc.seguranca.AcaoPermissao.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NullAway.Init")
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
    private UsuarioFacade usuarioFacade;

    @InjectMocks
    private ImpactoMapaService impactoMapaService;

    @Test
    @DisplayName("verificarImpactos - deve lancar ErroAcessoNegado quando nao tiver permissao")
    void verificarImpactosDeveLancarErroAcessoNegado() {
        Subprocesso subprocesso = criarSubprocessoPadrao();
        Usuario usuario = new Usuario();

        when(usuarioFacade.usuarioAutenticado()).thenReturn(usuario);
        doReturn(false).when(permissionEvaluator).verificarPermissao(usuario, subprocesso, VERIFICAR_IMPACTOS);

        assertThatThrownBy(() -> impactoMapaService.verificarImpactos(subprocesso)).isInstanceOf(sgc.comum.erros.ErroAcessoNegado.class);
    }

    private Usuario usuarioAdmin() {
        Usuario u = new Usuario();
        u.setPerfilAtivo(Perfil.ADMIN);
        return u;
    }

    private Subprocesso criarSubprocessoPadrao() {
        Subprocesso sp = new Subprocesso();
        Processo p = new Processo();
        p.setTipo(sgc.processo.model.TipoProcesso.REVISAO);
        sp.setProcesso(p);
        return sp;
    }

    private void mockAcessoLivre() {
        doReturn(true).when(permissionEvaluator).verificarPermissao(any(), any(), any());
    }

    private void mockUsuarioAutenticado(Usuario usuario) {
        when(usuarioFacade.usuarioAutenticado()).thenReturn(usuario);
    }

    private Subprocesso criarSubprocessoParaImpacto(
            SituacaoSubprocesso situacao,
            Long codigoUnidade
    ) {
        Unidade unidade = new Unidade();
        unidade.setCodigo(codigoUnidade);

        Subprocesso subprocesso = criarSubprocessoPadrao();
        subprocesso.setSituacaoForcada(situacao);
        subprocesso.setUnidade(unidade);
        subprocesso.setCodigo(99L);
        Processo processo = new Processo();
        processo.setTipo(sgc.processo.model.TipoProcesso.REVISAO);
        subprocesso.setProcesso(processo);

        return subprocesso;
    }

    @Test
    @DisplayName("Deve retornar sem impacto se não houver mapa vigente")
    void semMapaVigente() {
        mockAcessoLivre();
        Usuario usuario = usuarioAdmin();
        when(usuarioFacade.usuarioAutenticado()).thenReturn(usuario);
        Subprocesso subprocesso = criarSubprocessoPadrao();
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        subprocesso.setUnidade(unidade);

        when(mapaRepo.buscarMapaVigentePorUnidade(1L)).thenReturn(Optional.empty());

        ImpactoMapaResponse result = impactoMapaService.verificarImpactos(subprocesso);

        assertThat(result).isNotNull();
        assertThat(result.inseridas()).isEmpty();
        assertThat(result.removidas()).isEmpty();
        assertThat(result.alteradas()).isEmpty();
    }

    @Test
    @DisplayName("Deve detectar atividade inserida")
    void deveDetectarInserida() {
        mockAcessoLivre();
        Usuario usuario = usuarioAdmin();
        when(usuarioFacade.usuarioAutenticado()).thenReturn(usuario);
        Subprocesso subprocesso = criarSubprocessoPadrao();
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
        subprocesso.setCodigo(10L);
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        subprocesso.setUnidade(unidade);

        Mapa mapaVigente = new Mapa();
        mapaVigente.setCodigo(100L);
        Mapa mapaSub = new Mapa();
        mapaSub.setCodigo(200L);

        when(mapaRepo.buscarMapaVigentePorUnidade(1L)).thenReturn(Optional.of(mapaVigente));
        when(mapaRepo.buscarPorSubprocesso(10L)).thenReturn(Optional.of(mapaSub));

        // Vigente: vazio
        when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(100L)).thenReturn(Collections.emptyList());

        // Atual: 1 atividade
        Atividade nova = new Atividade();
        nova.setCodigo(1L);
        nova.setDescricao("Nova");
        nova.setConhecimentos(Collections.emptySet());
        when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(200L)).thenReturn(Collections.singletonList(nova));

        when(competenciaRepo.findByMapa_Codigo(100L)).thenReturn(Collections.emptyList());

        ImpactoMapaResponse result = impactoMapaService.verificarImpactos(subprocesso);

        assertThat(result.inseridas().size()).isEqualTo(1);
        assertThat(result.inseridas().getFirst().descricao()).isEqualTo("Nova");
    }

    @Test
    @DisplayName("Deve bloquear cálculo de impacto para perfil sem regra")
    void deveBloquearPerfilSemRegra() {
        mockAcessoLivre();
        Usuario usuario = new Usuario();
        usuario.setPerfilAtivo(Perfil.SERVIDOR);
        when(usuarioFacade.usuarioAutenticado()).thenReturn(usuario);

        Subprocesso subprocesso = criarSubprocessoPadrao();
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        subprocesso.setUnidade(unidade);

        assertThatThrownBy(() -> impactoMapaService.verificarImpactos(subprocesso)).isInstanceOf(ErroValidacao.class);
    }

    @Test
    @DisplayName("CHEFE pode verificar impacto em NAO_INICIADO")
    void chefePodeVerificarImpactoEmNaoIniciado() {
        mockAcessoLivre();
        Usuario usuario = new Usuario();
        usuario.setPerfilAtivo(Perfil.CHEFE);
        when(usuarioFacade.usuarioAutenticado()).thenReturn(usuario);
        Subprocesso subprocesso = criarSubprocessoParaImpacto(SituacaoSubprocesso.NAO_INICIADO, 7L);

        when(mapaRepo.buscarMapaVigentePorUnidade(7L)).thenReturn(Optional.empty());

        ImpactoMapaResponse resultado = impactoMapaService.verificarImpactos(subprocesso);

        assertThat(resultado).isNotNull();
        assertThat(resultado.temImpactos()).isFalse();
    }

    @Test
    @DisplayName("GESTOR pode verificar impacto em REVISAO_CADASTRO_DISPONIBILIZADA")
    void gestorPodeVerificarImpactoEmRevisaoDisponibilizada() {
        mockAcessoLivre();
        Usuario usuario = new Usuario();
        usuario.setPerfilAtivo(Perfil.GESTOR);
        when(usuarioFacade.usuarioAutenticado()).thenReturn(usuario);
        Subprocesso subprocesso = criarSubprocessoParaImpacto(
                SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA,
                8L
        );

        when(mapaRepo.buscarMapaVigentePorUnidade(8L)).thenReturn(Optional.empty());

        ImpactoMapaResponse resultado = impactoMapaService.verificarImpactos(subprocesso);

        assertThat(resultado).isNotNull();
        assertThat(resultado.temImpactos()).isFalse();
    }

    @Test
    @DisplayName("CHEFE recebe mensagem formatada ao tentar situação inválida")
    void chefeRecebeMensagemFormatadaQuandoSituacaoInvalida() {
        mockAcessoLivre();
        Usuario usuario = new Usuario();
        usuario.setPerfilAtivo(Perfil.CHEFE);
        when(usuarioFacade.usuarioAutenticado()).thenReturn(usuario);
        Subprocesso subprocesso = criarSubprocessoParaImpacto(
                SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA,
                9L
        );

        assertThatThrownBy(() -> impactoMapaService.verificarImpactos(subprocesso))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("REVISAO_CADASTRO_DISPONIBILIZADA")
                .hasMessageContaining("CHEFE");
    }

    @Test
    @DisplayName("Deve detectar atividade removida e impacto em competência")
    void deveDetectarRemovida() {
        mockAcessoLivre();
        Usuario usuario = usuarioAdmin();
        mockUsuarioAutenticado(usuario);
        Subprocesso subprocesso = criarSubprocessoPadrao();
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
        subprocesso.setCodigo(10L);
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        subprocesso.setUnidade(unidade);

        Mapa mapaVigente = new Mapa();
        mapaVigente.setCodigo(100L);
        Mapa mapaSub = new Mapa();
        mapaSub.setCodigo(200L);

        when(mapaRepo.buscarMapaVigentePorUnidade(1L)).thenReturn(Optional.of(mapaVigente));
        when(mapaRepo.buscarPorSubprocesso(10L)).thenReturn(Optional.of(mapaSub));

        // Vigente: 1 atividade "Antiga"
        Atividade antiga = new Atividade();
        antiga.setCodigo(1L);
        antiga.setDescricao("Antiga");
        antiga.setConhecimentos(Collections.emptySet());
        when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(100L)).thenReturn(Collections.singletonList(antiga));

        // Atual: vazio (foi removida)
        when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(200L)).thenReturn(Collections.emptyList());

        // Competencia ligada à atividade antiga
        Competencia comp = new Competencia();
        comp.setCodigo(50L);
        comp.setDescricao("Comp A");
        comp.setAtividades(Set.of(antiga));
        when(competenciaRepo.findByMapa_Codigo(100L)).thenReturn(Collections.singletonList(comp));

        ImpactoMapaResponse result = impactoMapaService.verificarImpactos(subprocesso);

        assertThat(result.removidas().size()).isEqualTo(1);
        assertThat(result.removidas().getFirst().descricao()).isEqualTo("Antiga");

        assertThat(result.competenciasImpactadas().size()).isEqualTo(1);
        assertThat(result.competenciasImpactadas().getFirst().descricao()).isEqualTo("Comp A");
    }

    @Test
    @DisplayName("Deve detectar atividade alterada (conhecimentos diferentes)")
    void deveDetectarAlterada() {
        mockAcessoLivre();
        Usuario usuario = usuarioAdmin();
        mockUsuarioAutenticado(usuario);
        Subprocesso subprocesso = criarSubprocessoPadrao();
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
        subprocesso.setCodigo(10L);
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        subprocesso.setUnidade(unidade);

        Mapa mapaVigente = new Mapa();
        mapaVigente.setCodigo(100L);
        Mapa mapaSub = new Mapa();
        mapaSub.setCodigo(200L);

        when(mapaRepo.buscarMapaVigentePorUnidade(1L)).thenReturn(Optional.of(mapaVigente));
        when(mapaRepo.buscarPorSubprocesso(10L)).thenReturn(Optional.of(mapaSub));

        // Vigente: Ativ A com Conhecimento C1
        Atividade ativVigente = new Atividade();
        ativVigente.setCodigo(1L);
        ativVigente.setDescricao("Ativ A");
        Conhecimento c1 = new Conhecimento();
        c1.setDescricao("C1");
        ativVigente.setConhecimentos(Set.of(c1));

        when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(100L)).thenReturn(Collections.singletonList(ativVigente));

        // Atual: Ativ A com Conhecimento C2 (alterado)
        Atividade ativAtual = new Atividade();
        ativAtual.setCodigo(2L);
        ativAtual.setDescricao("Ativ A");
        Conhecimento c2 = new Conhecimento();
        c2.setDescricao("C2");
        ativAtual.setConhecimentos(Set.of(c2));

        when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(200L)).thenReturn(Collections.singletonList(ativAtual));

        when(competenciaRepo.findByMapa_Codigo(100L)).thenReturn(Collections.emptyList());

        ImpactoMapaResponse result = impactoMapaService.verificarImpactos(subprocesso);

        assertThat(result.alteradas().size()).isEqualTo(1);
        assertThat(result.alteradas().getFirst().descricao()).isEqualTo("Ativ A");
    }

    @Test
    void deveDetectarConhecimentosDiferentesComAmbosVazios() {
        mockAcessoLivre();
        Mapa mapaVigente = Mapa.builder().codigo(100L).build();
        Mapa mapaAtual = Mapa.builder().codigo(200L).build();
        Subprocesso subprocesso = criarSubprocesso(mapaAtual);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);

        Usuario usuario = usuarioAdmin();
        mockUsuarioAutenticado(usuario);

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

        when(mapaRepo.buscarMapaVigentePorUnidade(1L)).thenReturn(Optional.of(mapaVigente));
        when(mapaRepo.buscarPorSubprocesso(1L)).thenReturn(Optional.of(mapaAtual));
        when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(100L))
                .thenReturn(Collections.singletonList(ativVigente));
        when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(200L))
                .thenReturn(Collections.singletonList(ativAtual));
        when(competenciaRepo.findByMapa_Codigo(100L)).thenReturn(Collections.emptyList());

        ImpactoMapaResponse result = impactoMapaService.verificarImpactos(subprocesso);

        assertThat(result.alteradas().size()).isEqualTo(0);
    }

    @Test
    void deveDetectarAtividadesComConhecimentosDiferentes() {
        mockAcessoLivre();
        Mapa mapaVigente = Mapa.builder().codigo(100L).build();
        Mapa mapaAtual = Mapa.builder().codigo(200L).build();
        Subprocesso subprocesso = criarSubprocesso(mapaAtual);
        subprocesso.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);

        Usuario usuario = usuarioAdmin();
        mockUsuarioAutenticado(usuario);

        Conhecimento c1 = Conhecimento.builder().codigo(1L).descricao("C1").build();
        Conhecimento c2 = Conhecimento.builder().codigo(2L).descricao("C2").build();
        Conhecimento c3 = Conhecimento.builder().codigo(3L).descricao("C3").build();

        Atividade ativVigente = Atividade.builder()
                .codigo(1L)
                .descricao("Ativ teste")
                .conhecimentos(Set.of(c1))
                .build();

        Atividade ativAtual = Atividade.builder()
                .codigo(2L)
                .descricao("Ativ teste")
                .conhecimentos(Set.of(c2, c3))
                .build();

        when(mapaRepo.buscarMapaVigentePorUnidade(1L)).thenReturn(Optional.of(mapaVigente));
        when(mapaRepo.buscarPorSubprocesso(1L)).thenReturn(Optional.of(mapaAtual));
        when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(100L))
                .thenReturn(Collections.singletonList(ativVigente));
        when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(200L))
                .thenReturn(Collections.singletonList(ativAtual));
        when(competenciaRepo.findByMapa_Codigo(100L)).thenReturn(Collections.emptyList());

        ImpactoMapaResponse result = impactoMapaService.verificarImpactos(subprocesso);

        assertThat(result.alteradas().size()).isEqualTo(1);
        assertThat(result.alteradas().getFirst().descricao()).isEqualTo("Ativ teste");
    }

    private Subprocesso criarSubprocesso(Mapa mapa) {
        Unidade unidade = Unidade.builder()
                .codigo(1L)
                .sigla("UNID")
                .nome("Unidade teste")
                .build();
        Processo processo = new Processo();
        processo.setTipo(sgc.processo.model.TipoProcesso.REVISAO);
        return Subprocesso.builder()
                .codigo(1L)
                .unidade(unidade)
                .mapa(mapa)
                .processo(processo)
                .build();
    }

    @Nested
    @DisplayName("Cobertura extra")
    class Coverage {

        @Test
        @DisplayName("verificarImpactos: Falha quando mapa do subprocesso não existe")
        void verificarImpactos_MapaSubprocessoInexistente() {
            mockAcessoLivre();
            Subprocesso sp = criarSubprocessoPadrao();
            sp.setCodigo(1L);
            sp.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
            Unidade u = new Unidade();
            u.setCodigo(100L);
            sp.setUnidade(u);
            Usuario usuario = usuarioAdmin();
            mockUsuarioAutenticado(usuario);

            when(mapaRepo.buscarMapaVigentePorUnidade(100L)).thenReturn(Optional.of(new Mapa()));
            when(mapaRepo.buscarPorSubprocesso(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> impactoMapaService.verificarImpactos(sp)).isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("verificarImpactos: Atividades duplicadas (mesma descrição) usa handler de colisão")
        void verificarImpactos_AtividadesDuplicadas() {
            mockAcessoLivre();
            Subprocesso sp = criarSubprocessoPadrao();
            sp.setCodigo(1L);
            sp.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
            Unidade u = new Unidade();
            u.setCodigo(100L);
            sp.setUnidade(u);

            Mapa mapaVigente = new Mapa();
            mapaVigente.setCodigo(20L);

            Mapa mapaSub = new Mapa();
            mapaSub.setCodigo(21L);

            when(mapaRepo.buscarMapaVigentePorUnidade(100L)).thenReturn(Optional.of(mapaVigente));
            when(mapaRepo.buscarPorSubprocesso(1L)).thenReturn(Optional.of(mapaSub));

            Atividade a1 = new Atividade();
            a1.setCodigo(10L);
            a1.setDescricao("Mesma");

            Atividade a2 = new Atividade();
            a2.setCodigo(11L);
            a2.setDescricao("Mesma");

            when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(20L))
                    .thenReturn(List.of(a1, a2));
            when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(21L))
                    .thenReturn(List.of());

            when(competenciaRepo.findByMapa_Codigo(20L)).thenReturn(Collections.emptyList());

            mockUsuarioAutenticado(usuarioAdmin());
            ImpactoMapaResponse response = impactoMapaService.verificarImpactos(sp);

            assertThat(response.removidas().size()).isEqualTo(2);
        }

        @Test
        @DisplayName("verificarImpactos: Atividade alterada vinculada a Competencia gera impacto")
        void verificarImpactos_AtividadeAlteradaComCompetencia() {
            mockAcessoLivre();
            Subprocesso sp = criarSubprocessoPadrao();
            sp.setCodigo(1L);
            sp.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
            Unidade u = new Unidade();
            u.setCodigo(100L);
            sp.setUnidade(u);

            Mapa mapaVigente = new Mapa();
            mapaVigente.setCodigo(20L);

            Mapa mapaSub = new Mapa();
            mapaSub.setCodigo(21L);

            when(mapaRepo.buscarMapaVigentePorUnidade(100L)).thenReturn(Optional.of(mapaVigente));
            when(mapaRepo.buscarPorSubprocesso(1L)).thenReturn(Optional.of(mapaSub));

            Atividade aVigente = new Atividade();
            aVigente.setCodigo(10L);
            aVigente.setDescricao("Ativ 1");
            aVigente.setConhecimentos(Set.of(new Conhecimento()));

            Atividade aAtual = new Atividade();
            aAtual.setCodigo(10L);
            aAtual.setDescricao("Ativ 1");
            aAtual.setConhecimentos(Set.of());

            when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(20L))
                    .thenReturn(List.of(aVigente));
            when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(21L))
                    .thenReturn(List.of(aAtual));

            Competencia comp = new Competencia();
            comp.setCodigo(50L);
            comp.setDescricao("Comp A");
            comp.setAtividades(Set.of(aVigente));

            when(competenciaRepo.findByMapa_Codigo(20L)).thenReturn(List.of(comp));

            mockUsuarioAutenticado(usuarioAdmin());
            ImpactoMapaResponse response = impactoMapaService.verificarImpactos(sp);

            assertThat(response.alteradas().size()).isEqualTo(1);
            assertThat(response.competenciasImpactadas().size()).isEqualTo(1);
        }

        @Test
        @DisplayName("verificarImpactos: Competência sem atividades não deve quebrar cálculo")
        void verificarImpactos_CompetenciaSemAtividades() {
            mockAcessoLivre();
            Subprocesso sp = criarSubprocessoPadrao();
            sp.setCodigo(1L);
            sp.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
            Unidade u = new Unidade();
            u.setCodigo(100L);
            sp.setUnidade(u);

            Mapa mapaVigente = new Mapa();
            mapaVigente.setCodigo(20L);

            Mapa mapaSub = new Mapa();
            mapaSub.setCodigo(21L);

            when(mapaRepo.buscarMapaVigentePorUnidade(100L)).thenReturn(Optional.of(mapaVigente));
            when(mapaRepo.buscarPorSubprocesso(1L)).thenReturn(Optional.of(mapaSub));
            when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(20L)).thenReturn(Collections.emptyList());
            when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(21L)).thenReturn(Collections.emptyList());

            Competencia compSemAtividades = new Competencia();
            compSemAtividades.setCodigo(91L);
            compSemAtividades.setDescricao("Comp sem atividades");
            compSemAtividades.setAtividades(Set.of());
            when(competenciaRepo.findByMapa_Codigo(20L)).thenReturn(List.of(compSemAtividades));

            mockUsuarioAutenticado(usuarioAdmin());
            ImpactoMapaResponse response = impactoMapaService.verificarImpactos(sp);

            assertThat(response).isNotNull();
            assertThat(response.temImpactos()).isFalse();
            assertThat(response.competenciasImpactadas()).isEmpty();
        }

        @Test
        @DisplayName("verificarImpactos: Deve acumular tipos e detalhes de impacto na mesma competência")
        void verificarImpactos_DeveAcumularImpactosNaMesmaCompetencia() {
            mockAcessoLivre();
            Subprocesso sp = criarSubprocessoPadrao();
            sp.setCodigo(1L);
            sp.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
            Unidade u = new Unidade();
            u.setCodigo(100L);
            sp.setUnidade(u);

            Mapa mapaVigente = new Mapa();
            mapaVigente.setCodigo(20L);

            Mapa mapaSub = new Mapa();
            mapaSub.setCodigo(21L);

            when(mapaRepo.buscarMapaVigentePorUnidade(100L)).thenReturn(Optional.of(mapaVigente));
            when(mapaRepo.buscarPorSubprocesso(1L)).thenReturn(Optional.of(mapaSub));

            Conhecimento conhecimentoAntigo = new Conhecimento();
            conhecimentoAntigo.setDescricao("Legado");

            Atividade removida = new Atividade();
            removida.setCodigo(10L);
            removida.setDescricao("Atividade removida");
            removida.setConhecimentos(Set.of());

            Atividade alteradaVigente = new Atividade();
            alteradaVigente.setCodigo(11L);
            alteradaVigente.setDescricao("Atividade alterada");
            alteradaVigente.setConhecimentos(Set.of(conhecimentoAntigo));

            Atividade alteradaAtual = new Atividade();
            alteradaAtual.setCodigo(12L);
            alteradaAtual.setDescricao("Atividade alterada");
            alteradaAtual.setConhecimentos(Set.of());

            when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(20L))
                    .thenReturn(List.of(removida, alteradaVigente));
            when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(21L))
                    .thenReturn(List.of(alteradaAtual));

            Competencia comp = new Competencia();
            comp.setCodigo(50L);
            comp.setDescricao("Comp Estratégica");
            comp.setAtividades(Set.of(removida, alteradaVigente));
            when(competenciaRepo.findByMapa_Codigo(20L)).thenReturn(List.of(comp));

            mockUsuarioAutenticado(usuarioAdmin());
            ImpactoMapaResponse response = impactoMapaService.verificarImpactos(sp);

            assertThat(response.removidas().size()).isEqualTo(1);
            assertThat(response.alteradas().size()).isEqualTo(1);
            assertThat(response.competenciasImpactadas().size()).isEqualTo(1);

            CompetenciaImpactadaDto competenciaImpactada = response.competenciasImpactadas().getFirst();
            assertThat(competenciaImpactada.codigo()).isEqualTo(50L);
            assertThat(competenciaImpactada.descricao()).isEqualTo("Comp Estratégica");
            assertThat(competenciaImpactada.atividadesAfetadas().size()).isEqualTo(3);
            assertThat(competenciaImpactada.atividadesAfetadas()).contains("Atividade removida: Atividade removida");
            assertThat(competenciaImpactada.atividadesAfetadas().stream()
                    .anyMatch(detalhe -> detalhe.contains("Atividade alterada"))).isTrue();
            assertThat(competenciaImpactada.tiposImpacto()).contains(TipoImpactoCompetencia.ATIVIDADE_REMOVIDA.name());
            assertThat(competenciaImpactada.tiposImpacto()).contains(TipoImpactoCompetencia.ATIVIDADE_ALTERADA.name());
            assertThat(competenciaImpactada.tiposImpacto().size()).isEqualTo(2);
        }

        @Test
        @DisplayName("verificarImpactos: Atividades com conhecimentos idênticos não devem gerar alteração")
        void verificarImpactos_ConhecimentosIdenticos() {
            mockAcessoLivre();
            Subprocesso sp = criarSubprocessoPadrao();
            sp.setCodigo(1L);
            sp.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
            Unidade u = new Unidade();
            u.setCodigo(100L);
            sp.setUnidade(u);

            Mapa mapaVigente = new Mapa();
            mapaVigente.setCodigo(20L);
            Mapa mapaSub = new Mapa();
            mapaSub.setCodigo(21L);

            when(mapaRepo.buscarMapaVigentePorUnidade(100L)).thenReturn(Optional.of(mapaVigente));
            when(mapaRepo.buscarPorSubprocesso(1L)).thenReturn(Optional.of(mapaSub));

            Conhecimento c1 = new Conhecimento();
            c1.setDescricao("Java");
            Atividade a1 = new Atividade();
            a1.setCodigo(10L);
            a1.setDescricao("Ativ");
            a1.setConhecimentos(Set.of(c1));
            Atividade a2 = new Atividade();
            a2.setCodigo(11L);
            a2.setDescricao("Ativ");
            a2.setConhecimentos(Set.of(c1));

            when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(20L)).thenReturn(List.of(a1));
            when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(21L)).thenReturn(List.of(a2));
            when(competenciaRepo.findByMapa_Codigo(20L)).thenReturn(List.of());

            mockUsuarioAutenticado(usuarioAdmin());
            ImpactoMapaResponse response = impactoMapaService.verificarImpactos(sp);

            assertThat(response.temImpactos()).isFalse(); // branch 157 false
            assertThat(response.alteradas()).isEmpty();
        }

        @Test
        @DisplayName("verificarImpactos: Detectar alteração de descrição via heurística de competências")
        void verificarImpactos_HeuristicaDescricao() {
            mockAcessoLivre();
            Subprocesso sp = criarSubprocessoPadrao();
            sp.setCodigo(1L);
            sp.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
            Unidade u = new Unidade();
            u.setCodigo(100L);
            sp.setUnidade(u);

            Mapa mapaVigente = new Mapa();
            mapaVigente.setCodigo(20L);
            Mapa mapaSub = new Mapa();
            mapaSub.setCodigo(21L);

            when(mapaRepo.buscarMapaVigentePorUnidade(100L)).thenReturn(Optional.of(mapaVigente));
            when(mapaRepo.buscarPorSubprocesso(1L)).thenReturn(Optional.of(mapaSub));

            Competencia compVigente = new Competencia();
            compVigente.setCodigo(50L);
            compVigente.setDescricao("Competencia X");

            Conhecimento cRemovido = new Conhecimento();
            cRemovido.setDescricao("Java 8");
            Conhecimento cAdicionado = new Conhecimento();
            cAdicionado.setDescricao("Java 21");
            Conhecimento cMantido = new Conhecimento();
            cMantido.setDescricao("Java Básico");

            Atividade aVigente = new Atividade();
            aVigente.setCodigo(10L);
            aVigente.setDescricao("Descrição Antiga");
            aVigente.setConhecimentos(Set.of(cRemovido, cMantido));
            aVigente.setCompetencias(Set.of(compVigente));
            compVigente.setAtividades(Set.of(aVigente));

            Atividade aAtual = new Atividade();
            aAtual.setCodigo(11L);
            aAtual.setDescricao("Descrição Nova");
            aAtual.setConhecimentos(Set.of(cAdicionado, cMantido));
            aAtual.setCompetencias(Set.of(compVigente));

            when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(20L)).thenReturn(List.of(aVigente));
            when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(21L)).thenReturn(List.of(aAtual));
            when(competenciaRepo.findByMapa_Codigo(20L)).thenReturn(List.of(compVigente));

            mockUsuarioAutenticado(usuarioAdmin());
            ImpactoMapaResponse response = impactoMapaService.verificarImpactos(sp);

            assertThat(response.alteradas().size()).isEqualTo(1);
            AtividadeImpactadaDto alt = response.alteradas().getFirst();
            assertThat(alt.descricaoAnterior()).isEqualTo("Descrição Antiga");
            assertThat(alt.descricao()).isEqualTo("Descrição Nova");
            assertThat(alt.tipoImpacto()).isEqualTo(TipoImpactoAtividade.ALTERADA.name());

            // Verifica se a competência foi impactada pela alteração de nome
            assertThat(response.competenciasImpactadas().size()).isEqualTo(1);
            assertThat(response.competenciasImpactadas().getFirst().atividadesAfetadas().stream()
                    .anyMatch(d -> d.contains("Descrição alterada para Descrição Nova"))).isTrue();
        }

        @Test
        @DisplayName("verificarImpactos: Registro de detalhes de conhecimentos adicionados e removidos")
        void verificarImpactos_DetalhesConhecimentos() {
            mockAcessoLivre();
            Subprocesso sp = criarSubprocessoPadrao();
            sp.setCodigo(1L);
            sp.setSituacaoForcada(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
            Unidade u = new Unidade();
            u.setCodigo(100L);
            sp.setUnidade(u);

            Mapa mapaVigente = new Mapa();
            mapaVigente.setCodigo(20L);
            Mapa mapaSub = new Mapa();
            mapaSub.setCodigo(21L);

            when(mapaRepo.buscarMapaVigentePorUnidade(100L)).thenReturn(Optional.of(mapaVigente));
            when(mapaRepo.buscarPorSubprocesso(1L)).thenReturn(Optional.of(mapaSub));

            Conhecimento cRemovido = new Conhecimento();
            cRemovido.setDescricao("Java 8");
            Conhecimento cAdicionado = new Conhecimento();
            cAdicionado.setDescricao("Java 21");
            Conhecimento cMantido = new Conhecimento();
            cMantido.setDescricao("Java Básico");

            Atividade aVigente = new Atividade();
            aVigente.setCodigo(10L);
            aVigente.setDescricao("Desenvolvimento");
            aVigente.setConhecimentos(Set.of(cRemovido, cMantido));

            Atividade aAtual = new Atividade();
            aAtual.setCodigo(10L); // Mesma atividade (ID fixo no DTO ou descrição igual)
            aAtual.setDescricao("Desenvolvimento");
            aAtual.setConhecimentos(Set.of(cAdicionado, cMantido));

            Competencia comp = new Competencia();
            comp.setCodigo(50L);
            comp.setDescricao("Comp Dev");
            comp.setAtividades(Set.of(aVigente));

            when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(20L)).thenReturn(List.of(aVigente));
            when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(21L)).thenReturn(List.of(aAtual));
            when(competenciaRepo.findByMapa_Codigo(20L)).thenReturn(List.of(comp));

            mockUsuarioAutenticado(usuarioAdmin());
            ImpactoMapaResponse response = impactoMapaService.verificarImpactos(sp);

            assertThat(response.alteradas().size()).isEqualTo(1);
            AtividadeImpactadaDto alt = response.alteradas().getFirst();
            assertThat(alt.conhecimentosAdicionados()).contains("Java 21");
            assertThat(alt.conhecimentosRemovidos()).contains("Java 8");

            assertThat(response.competenciasImpactadas().size()).isEqualTo(1);
            List<String> detalhes = response.competenciasImpactadas().getFirst().atividadesAfetadas();
            assertThat(detalhes.stream().anyMatch(d -> d.contains("Conhecimento Java 21 adicionado"))).isTrue();
            assertThat(detalhes.stream().anyMatch(d -> d.contains("Conhecimento Java 8 removido"))).isTrue();
        }

    }

    @Test
    @DisplayName("verificarImpactos deve lançar ErroAcessoNegado quando sem permissão")
    void verificarImpactosSemPermissao() {
        Usuario user = new Usuario();
        Subprocesso sp = criarSubprocessoPadrao();
        when(usuarioFacade.usuarioAutenticado()).thenReturn(user);
        when(permissionEvaluator.verificarPermissao(any(), any(), any())).thenReturn(false);

        assertThatThrownBy(() -> impactoMapaService.verificarImpactos(sp)).isInstanceOf(ErroAcessoNegado.class);
    }

    @Test
    @DisplayName("verificarImpactos deve retornar sem impacto quando não há mapa vigente")
    void verificarImpactosSemMapaVigente() {
        Usuario user = new Usuario();
        user.setPerfilAtivo(Perfil.ADMIN);

        Unidade u = new Unidade();
        u.setCodigo(1L);

        Subprocesso sp = criarSubprocessoPadrao();
        sp.setUnidade(u);
        sp.setSituacaoForcada(SituacaoSubprocesso.NAO_INICIADO);

        when(usuarioFacade.usuarioAutenticado()).thenReturn(user);
        when(permissionEvaluator.verificarPermissao(any(), any(), any())).thenReturn(true);
        when(mapaRepo.buscarMapaVigentePorUnidade(1L)).thenReturn(Optional.empty());

        ImpactoMapaResponse res = impactoMapaService.verificarImpactos(sp);
        assertThat(res.temImpactos()).isFalse();
    }

    @Test
    @DisplayName("verificarImpactos deve lançar ErroEntidadeNaoEncontrada quando mapa do subprocesso não existe")
    void verificarImpactosSemMapaSubprocesso() {
        Usuario user = new Usuario();
        user.setPerfilAtivo(Perfil.ADMIN);

        Unidade u = new Unidade();
        u.setCodigo(1L);

        Subprocesso sp = criarSubprocessoPadrao();
        sp.setCodigo(100L);
        sp.setUnidade(u);
        sp.setSituacaoForcada(SituacaoSubprocesso.NAO_INICIADO);

        when(usuarioFacade.usuarioAutenticado()).thenReturn(user);
        when(permissionEvaluator.verificarPermissao(any(), any(), any())).thenReturn(true);
        when(mapaRepo.buscarMapaVigentePorUnidade(1L)).thenReturn(Optional.of(new Mapa()));
        when(mapaRepo.buscarPorSubprocesso(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> impactoMapaService.verificarImpactos(sp)).isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }

    @Test
    @DisplayName("calcularImpactos deve retornar sem impacto para processo de mapeamento")
    void calcularImpactosMapeamentoSemImpacto() {
        Subprocesso subprocesso = new Subprocesso();
        Processo processo = new Processo();
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        subprocesso.setProcesso(processo);

        ImpactoMapaResponse resposta = impactoMapaService.calcularImpactos(subprocesso);

        assertThat(resposta.temImpactos()).isFalse();
        assertThat(resposta.inseridas()).isEmpty();
        assertThat(resposta.removidas()).isEmpty();
        assertThat(resposta.alteradas()).isEmpty();
    }

    @Test
    @DisplayName("podeVisualizarImpactos deve retornar falso para processo de mapeamento")
    void podeVisualizarImpactosMapeamentoRetornaFalso() {
        Subprocesso subprocesso = new Subprocesso();
        Processo processo = new Processo();
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        subprocesso.setProcesso(processo);

        assertThat(impactoMapaService.podeVisualizarImpactos(subprocesso)).isFalse();
        verifyNoInteractions(usuarioFacade, permissionEvaluator);
    }

    @Test
    @DisplayName("verificarImpactos deve lançar erro de validação para processo de mapeamento")
    void verificarImpactosMapeamentoLancaErroValidacao() {
        Subprocesso subprocesso = new Subprocesso();
        Processo processo = new Processo();
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        subprocesso.setProcesso(processo);
        when(usuarioFacade.usuarioAutenticado()).thenReturn(usuarioAdmin());

        assertThatThrownBy(() -> impactoMapaService.verificarImpactos(subprocesso))
                .isInstanceOf(ErroValidacao.class)
                .hasMessageContaining("apenas para processos de revisão");
    }

    @Test
    @DisplayName("podeVisualizarImpactos deve retornar falso quando não há permissão")
    void podeVisualizarImpactosSemPermissao() {
        Usuario usuario = new Usuario();
        usuario.setPerfilAtivo(Perfil.ADMIN);
        Subprocesso subprocesso = criarSubprocessoParaImpacto(SituacaoSubprocesso.NAO_INICIADO, 1L);
        when(usuarioFacade.usuarioAutenticado()).thenReturn(usuario);
        when(permissionEvaluator.verificarPermissao(usuario, subprocesso, VERIFICAR_IMPACTOS)).thenReturn(false);

        assertThat(impactoMapaService.podeVisualizarImpactos(subprocesso)).isFalse();
    }

    @Test
    @DisplayName("verificarImpactos deve calcular adicionados e removidos na heurística de renomeação")
    void verificarImpactosHeuristicaComConhecimentosDiferentes() {
        mockAcessoLivre();
        Subprocesso subprocesso = criarSubprocessoParaImpacto(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO, 11L);
        subprocesso.setCodigo(77L);
        mockUsuarioAutenticado(usuarioAdmin());

        Mapa mapaVigente = new Mapa();
        mapaVigente.setCodigo(501L);
        Mapa mapaAtual = new Mapa();
        mapaAtual.setCodigo(601L);
        when(mapaRepo.buscarMapaVigentePorUnidade(11L)).thenReturn(Optional.of(mapaVigente));
        when(mapaRepo.buscarPorSubprocesso(77L)).thenReturn(Optional.of(mapaAtual));

        Competencia competencia = new Competencia();
        competencia.setCodigo(900L);
        competencia.setDescricao("Comp");

        Conhecimento legado = new Conhecimento();
        legado.setDescricao("Legado");
        Atividade atividadeVigente = new Atividade();
        atividadeVigente.setCodigo(1000L);
        atividadeVigente.setDescricao("Nome antigo");
        atividadeVigente.setConhecimentos(Set.of(legado));
        atividadeVigente.setCompetencias(Set.of(competencia));
        competencia.setAtividades(Set.of(atividadeVigente));

        Conhecimento novo = new Conhecimento();
        novo.setDescricao("Novo");
        Atividade atividadeAtual = new Atividade();
        atividadeAtual.setCodigo(1001L);
        atividadeAtual.setDescricao("Nome novo");
        atividadeAtual.setConhecimentos(Set.of(novo));
        atividadeAtual.setCompetencias(Set.of(competencia));

        when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(501L)).thenReturn(List.of(atividadeVigente));
        when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(601L)).thenReturn(List.of(atividadeAtual));
        when(competenciaRepo.findByMapa_Codigo(501L)).thenReturn(List.of(competencia));

        ImpactoMapaResponse resposta = impactoMapaService.verificarImpactos(subprocesso);

        assertThat(resposta.alteradas()).hasSize(1);
        assertThat(resposta.alteradas().getFirst().conhecimentosAdicionados()).contains("Novo");
        assertThat(resposta.alteradas().getFirst().conhecimentosRemovidos()).contains("Legado");
    }

    @Nested
    @DisplayName("Testes de Situação")
    class CheckSituacaoTest {
        @Test
        @DisplayName("CHEFE - situações válidas")
        void chefeSituacoesValidas() {
            testCheckSituacao(Perfil.CHEFE, SituacaoSubprocesso.NAO_INICIADO, true);
            testCheckSituacao(Perfil.CHEFE, SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO, true);
            testCheckSituacao(Perfil.CHEFE, SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO, false);
        }

        @Test
        @DisplayName("GESTOR - situação válida")
        void gestorSituacoesValidas() {
            testCheckSituacao(Perfil.GESTOR, SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA, true);
            testCheckSituacao(Perfil.GESTOR, SituacaoSubprocesso.NAO_INICIADO, false);
        }

        @Test
        @DisplayName("ADMIN - situações válidas")
        void adminSituacoesValidas() {
            testCheckSituacao(Perfil.ADMIN, SituacaoSubprocesso.NAO_INICIADO, true);
            testCheckSituacao(Perfil.ADMIN, SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA, true);
            testCheckSituacao(Perfil.ADMIN, SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO, true);
            testCheckSituacao(Perfil.ADMIN, SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO, false);
        }

        @Test
        @DisplayName("SERVIDOR - sempre bloqueado")
        void servidorBloqueado() {
            testCheckSituacao(Perfil.SERVIDOR, SituacaoSubprocesso.NAO_INICIADO, false);
        }

        private void testCheckSituacao(Perfil perfil, SituacaoSubprocesso situacao, boolean expected) {
            Usuario user = new Usuario();
            user.setPerfilAtivo(perfil);
            Subprocesso sp = criarSubprocessoPadrao();
            sp.setSituacaoForcada(situacao);

            when(permissionEvaluator.verificarPermissao(user, sp, AcaoPermissao.VERIFICAR_IMPACTOS)).thenReturn(true);
            when(usuarioFacade.usuarioAutenticado()).thenReturn(user);

            assertThat(impactoMapaService.podeVisualizarImpactos(sp)).isEqualTo(expected);
        }
    }

    @Test
    @DisplayName("verificarImpactos deve classificar corretamente conhecimentos parcialmente sobrepostos")
    void verificarImpactosComConhecimentosParcialmenteSobrepostos() {
        mockAcessoLivre();
        Subprocesso sp = criarSubprocessoParaImpacto(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO, 200L);
        sp.setCodigo(2L);

        Mapa mapaVigente = new Mapa();
        mapaVigente.setCodigo(30L);
        Mapa mapaAtual = new Mapa();
        mapaAtual.setCodigo(31L);

        when(mapaRepo.buscarMapaVigentePorUnidade(200L)).thenReturn(Optional.of(mapaVigente));
        when(mapaRepo.buscarPorSubprocesso(2L)).thenReturn(Optional.of(mapaAtual));

        Conhecimento cJava = new Conhecimento();
        cJava.setDescricao("Java");
        Conhecimento cPython = new Conhecimento();
        cPython.setDescricao("Python");
        Conhecimento cKotlin = new Conhecimento();
        cKotlin.setDescricao("Kotlin");

        Atividade aVigente = new Atividade();
        aVigente.setCodigo(10L);
        aVigente.setDescricao("Programação");
        aVigente.setConhecimentos(Set.of(cJava, cPython));

        Atividade aAtual = new Atividade();
        aAtual.setCodigo(11L);
        aAtual.setDescricao("Programação");
        aAtual.setConhecimentos(Set.of(cJava, cKotlin));

        when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(30L)).thenReturn(List.of(aVigente));
        when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(31L)).thenReturn(List.of(aAtual));
        when(competenciaRepo.findByMapa_Codigo(30L)).thenReturn(List.of());

        mockUsuarioAutenticado(usuarioAdmin());
        ImpactoMapaResponse response = impactoMapaService.verificarImpactos(sp);

        assertThat(response.alteradas()).hasSize(1);
        assertThat(response.alteradas().getFirst().conhecimentosAdicionados()).containsExactlyInAnyOrder("Kotlin");
        assertThat(response.alteradas().getFirst().conhecimentosRemovidos()).containsExactlyInAnyOrder("Python");
    }

    @Test
    @DisplayName("verificarImpactos deve ignorar atividade sem competencias na heuristica de renomeacao")
    void verificarImpactosDeveIgnorarAtividadeSemCompetenciasNaHeuristica() {
        mockAcessoLivre();
        Subprocesso sp = criarSubprocessoParaImpacto(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO, 201L);
        sp.setCodigo(3L);

        Mapa mapaVigente = new Mapa();
        mapaVigente.setCodigo(40L);
        Mapa mapaAtual = new Mapa();
        mapaAtual.setCodigo(41L);

        when(mapaRepo.buscarMapaVigentePorUnidade(201L)).thenReturn(Optional.of(mapaVigente));
        when(mapaRepo.buscarPorSubprocesso(3L)).thenReturn(Optional.of(mapaAtual));

        Atividade aVigente = new Atividade();
        aVigente.setCodigo(20L);
        aVigente.setDescricao("Ativity Old");
        aVigente.setConhecimentos(Set.of());
        aVigente.setCompetencias(Set.of());

        Atividade aAtual = new Atividade();
        aAtual.setCodigo(21L);
        aAtual.setDescricao("Ativity New");
        aAtual.setConhecimentos(Set.of());
        aAtual.setCompetencias(Set.of());

        when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(40L)).thenReturn(List.of(aVigente));
        when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(41L)).thenReturn(List.of(aAtual));
        when(competenciaRepo.findByMapa_Codigo(40L)).thenReturn(List.of());

        mockUsuarioAutenticado(usuarioAdmin());
        ImpactoMapaResponse response = impactoMapaService.verificarImpactos(sp);

        assertThat(response.alteradas()).isEmpty();
    }

    @Test
    @DisplayName("verificarImpactos deve ignorar atividade vigente sem competencias na heuristica")
    void verificarImpactosDeveIgnorarVigentesSemCompetenciasNaHeuristica() {
        mockAcessoLivre();
        Subprocesso sp = criarSubprocessoParaImpacto(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO, 202L);
        sp.setCodigo(4L);

        Mapa mapaVigente = new Mapa();
        mapaVigente.setCodigo(50L);
        Mapa mapaAtual = new Mapa();
        mapaAtual.setCodigo(51L);

        when(mapaRepo.buscarMapaVigentePorUnidade(202L)).thenReturn(Optional.of(mapaVigente));
        when(mapaRepo.buscarPorSubprocesso(4L)).thenReturn(Optional.of(mapaAtual));

        Competencia compAtual = new Competencia();
        compAtual.setCodigo(70L);
        compAtual.setDescricao("Comp Atual");
        compAtual.setAtividades(Set.of());

        Atividade aVigente = new Atividade();
        aVigente.setCodigo(30L);
        aVigente.setDescricao("Desc Antiga");
        aVigente.setConhecimentos(Set.of());
        aVigente.setCompetencias(Set.of());

        Atividade aAtual = new Atividade();
        aAtual.setCodigo(31L);
        aAtual.setDescricao("Desc Nova");
        aAtual.setConhecimentos(Set.of());
        aAtual.setCompetencias(Set.of(compAtual));

        when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(50L)).thenReturn(List.of(aVigente));
        when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(51L)).thenReturn(List.of(aAtual));
        when(competenciaRepo.findByMapa_Codigo(50L)).thenReturn(List.of());

        mockUsuarioAutenticado(usuarioAdmin());
        ImpactoMapaResponse response = impactoMapaService.verificarImpactos(sp);

        assertThat(response.alteradas()).isEmpty();
    }

    @Test
    @DisplayName("verificarImpactos deve ignorar quando competencias vigentes e atuais diferem na heuristica")
    void verificarImpactosDeveIgnorarQuandoCompetenciasDiferentesNaHeuristica() {
        mockAcessoLivre();
        Subprocesso sp = criarSubprocessoParaImpacto(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO, 202L);
        sp.setCodigo(4L);

        Mapa mapaVigente = new Mapa();
        mapaVigente.setCodigo(50L);
        Mapa mapaAtual = new Mapa();
        mapaAtual.setCodigo(51L);

        when(mapaRepo.buscarMapaVigentePorUnidade(202L)).thenReturn(Optional.of(mapaVigente));
        when(mapaRepo.buscarPorSubprocesso(4L)).thenReturn(Optional.of(mapaAtual));

        Competencia compAtual = new Competencia();
        compAtual.setCodigo(70L);
        compAtual.setDescricao("Comp Atual");
        compAtual.setAtividades(Set.of());

        Atividade aVigente = new Atividade();
        aVigente.setCodigo(30L);
        aVigente.setDescricao("Desc Antiga");
        aVigente.setConhecimentos(Set.of());
        aVigente.setCompetencias(Set.of());

        Atividade aAtual = new Atividade();
        aAtual.setCodigo(31L);
        aAtual.setDescricao("Desc Nova");
        aAtual.setConhecimentos(Set.of());
        aAtual.setCompetencias(Set.of(compAtual));

        Competencia compVigente = new Competencia();
        compVigente.setCodigo(80L);
        compVigente.setDescricao("Comp Vigente");
        compVigente.setAtividades(Set.of(aVigente));

        when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(50L)).thenReturn(List.of(aVigente));
        when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(51L)).thenReturn(List.of(aAtual));
        when(competenciaRepo.findByMapa_Codigo(50L)).thenReturn(List.of(compVigente));

        mockUsuarioAutenticado(usuarioAdmin());
        ImpactoMapaResponse response = impactoMapaService.verificarImpactos(sp);

        assertThat(response.alteradas()).isEmpty();
    }
}
