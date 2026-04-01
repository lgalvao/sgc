package sgc.subprocesso.service;

import org.jspecify.annotations.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.comum.erros.*;
import sgc.mapa.model.*;
import sgc.mapa.service.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoConsultaService Extra Coverage Test")
@SuppressWarnings("NullAway.Init")
class SubprocessoConsultaServiceExtraCoverageTest {

    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private UnidadeService unidadeService;
    @Mock
    private MapaManutencaoService mapaManutencaoService;
    @Mock
    private UsuarioFacade usuarioFacade;
    @Mock
    private MovimentacaoRepo movimentacaoRepo;
    @Mock
    private HierarquiaService hierarquiaService;
    @Mock
    private AnaliseRepo analiseRepo;
    @Mock
    private SubprocessoValidacaoService validacaoService;
    @Mock
    private ImpactoMapaService impactoMapaService;
    @Mock
    private MapaVisualizacaoService mapaVisualizacaoService;

    @InjectMocks
    private SubprocessoConsultaService consultaService;

    private Subprocesso criarSubprocessoComMapa(@Nullable Long codigo) {
        return criarSubprocessoComMapa(codigo, TipoProcesso.MAPEAMENTO);
    }

    private Subprocesso criarSubprocessoComMapa(@Nullable Long codigo, TipoProcesso tipo) {
        Subprocesso sp = new Subprocesso();
        sp.setCodigo(codigo);
        sp.setMapa(new Mapa());
        sp.setSituacaoForcada(NAO_INICIADO);
        sp.setProcesso(Processo.builder().tipo(tipo).situacao(SituacaoProcesso.EM_ANDAMENTO).build());
        return sp;
    }

    private Usuario criarUsuarioMock() {
        Usuario user = new Usuario();
        user.setTituloEleitoral("12345678");
        user.setNome("Usuario Teste");
        return user;
    }

    @Nested
    @DisplayName("obterUnidadeLocalizacao")
    class ObterUnidadeLocalizacao {
        @Test
        @DisplayName("deve retornar a unidade destino da ultima movimentacao se localizacaoAtual for null")
        void deveRetornarDestinoMovimentacao() {
            Unidade u1 = new Unidade();
            u1.setCodigo(1L);
            Unidade u2 = new Unidade();
            u2.setCodigo(2L);
            Subprocesso sp = criarSubprocessoComMapa(100L);
            sp.setUnidade(u1);
            Movimentacao mov = new Movimentacao();
            mov.setUnidadeDestino(u2);

            when(movimentacaoRepo.listarPorSubprocessoOrdenadasPorDataHoraDesc(100L)).thenReturn(List.of(mov));

            assertThat(consultaService.obterUnidadeLocalizacao(sp)).isEqualTo(u2);
        }

        @Test
        @DisplayName("deve retornar unidade do subprocesso se movimentacao nao tiver destino e localizacaoAtual for null")
        void deveRetornarUnidadeSubprocesso() {
            Unidade u1 = new Unidade();
            u1.setCodigo(1L);
            Subprocesso sp = criarSubprocessoComMapa(100L);
            sp.setUnidade(u1);
            Movimentacao mov = new Movimentacao();
            mov.setUnidadeDestino(null);

            when(movimentacaoRepo.listarPorSubprocessoOrdenadasPorDataHoraDesc(100L)).thenReturn(List.of(mov));

            assertThat(consultaService.obterUnidadeLocalizacao(sp)).isEqualTo(u1);
        }

        @Test
        @DisplayName("deve retornar unidade do subprocesso se codigo for null")
        void codNull() {
            Unidade u = new Unidade();
            Subprocesso sp = criarSubprocessoComMapa(null);
            sp.setUnidade(u);
            assertThat(consultaService.obterUnidadeLocalizacao(sp)).isEqualTo(u);
        }

        @Test
        @DisplayName("deve retornar unidade do subprocesso se movimentacoes vazio")
        void movVazio() {
            Unidade u = new Unidade();
            Subprocesso sp = criarSubprocessoComMapa(1L);
            sp.setUnidade(u);
            when(movimentacaoRepo.listarPorSubprocessoOrdenadasPorDataHoraDesc(1L)).thenReturn(List.of());
            assertThat(consultaService.obterUnidadeLocalizacao(sp)).isEqualTo(u);
        }
    }

    @Nested
    @DisplayName("Permissões e Detalhes")
    class PermissoesEDetalhes {
        @Test
        @DisplayName("deve obter detalhes e permissoes para CHEFE em processo finalizado")
        void obterDetalhes_Chefe_ProcessoFinalizado() {
            Subprocesso sp = criarSubprocessoComMapa(1L);
            sp.setSituacaoForcada(MAPEAMENTO_MAPA_HOMOLOGADO);

            Unidade u = new Unidade();
            u.setTipo(TipoUnidade.OPERACIONAL);
            u.setCodigo(10L);
            u.setSigla("U1");
            u.setTituloTitular("titular");
            sp.setUnidade(u);

            Processo p = sp.getProcesso();
            p.setSituacao(SituacaoProcesso.FINALIZADO);

            Usuario user = criarUsuarioMock();
            user.setPerfilAtivo(Perfil.CHEFE);
            user.setUnidadeAtivaCodigo(10L);

            when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(1L)).thenReturn(Optional.of(sp));
            when(usuarioFacade.buscarPorLogin("titular")).thenReturn(user);
            when(movimentacaoRepo.listarPorSubprocessoOrdenadasPorDataHoraDesc(1L)).thenReturn(List.of());
            when(unidadeService.buscarPorCodigo(10L)).thenReturn(u);

            SubprocessoDetalheResponse res = consultaService.obterDetalhes(1L, user);

            assertThat(res.permissoes().habilitarAcessoCadastro()).isTrue();
            assertThat(res.permissoes().habilitarAcessoMapa()).isTrue();
        }

        @Test
        @DisplayName("obterDetalhes com movimentacao completa")
        void obterDetalhes_MovimentacaoCompleta() {
            Subprocesso sp = criarSubprocessoComMapa(1L);
            sp.setSituacaoForcada(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            Unidade u = new Unidade();
            u.setTipo(TipoUnidade.OPERACIONAL);
            u.setSigla("U1");
            u.setCodigo(10L);
            u.setTituloTitular("titular");
            sp.setUnidade(u);

            Usuario user = criarUsuarioMock();
            user.setPerfilAtivo(Perfil.ADMIN);
            user.setUnidadeAtivaCodigo(10L);

            Unidade dest = new Unidade();
            dest.setCodigo(20L);
            dest.setSigla("U2");
            dest.setNome("Dest");
            Movimentacao mov = new Movimentacao();
            mov.setUnidadeOrigem(u);
            mov.setUnidadeDestino(dest);
            mov.setUsuario(user);

            when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(1L)).thenReturn(Optional.of(sp));
            when(movimentacaoRepo.listarPorSubprocessoOrdenadasPorDataHoraDesc(1L)).thenReturn(List.of(mov));
            when(unidadeService.buscarPorCodigo(10L)).thenReturn(u);
            when(usuarioFacade.buscarPorLogin("titular")).thenReturn(user);

            SubprocessoDetalheResponse res = consultaService.obterDetalhes(1L, user);
            assertThat(res.localizacaoAtual()).isEqualTo("U2");
        }

        @Test
        @DisplayName("obterDetalhes com destino null na movimentacao")
        void obterDetalhes_MovimentacaoDestinoNull() {
            Subprocesso sp = criarSubprocessoComMapa(1L);
            sp.setSituacaoForcada(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            Unidade u = new Unidade();
            u.setTipo(TipoUnidade.OPERACIONAL);
            u.setSigla("U1");
            u.setCodigo(10L);
            u.setTituloTitular("titular");
            sp.setUnidade(u);

            Usuario user = criarUsuarioMock();
            user.setPerfilAtivo(Perfil.ADMIN);
            user.setUnidadeAtivaCodigo(10L);

            Movimentacao mov = new Movimentacao();
            mov.setUnidadeOrigem(u);
            mov.setUnidadeDestino(null);
            mov.setUsuario(user);

            when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(1L)).thenReturn(Optional.of(sp));
            when(movimentacaoRepo.listarPorSubprocessoOrdenadasPorDataHoraDesc(1L)).thenReturn(List.of(mov));
            when(unidadeService.buscarPorCodigo(10L)).thenReturn(u);
            when(usuarioFacade.buscarPorLogin("titular")).thenReturn(user);

            SubprocessoDetalheResponse res = consultaService.obterDetalhes(1L, user);
            assertThat(res.localizacaoAtual()).isEqualTo("U1");
        }

        @Test
        @DisplayName("obterPermissoesUI para GESTOR")
        void obterPermissoesUI_Gestor() {
            Subprocesso sp = criarSubprocessoComMapa(1L);
            sp.setSituacaoForcada(MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            Unidade u = new Unidade();
            u.setCodigo(10L);
            sp.setUnidade(u);

            Usuario user = criarUsuarioMock();
            user.setPerfilAtivo(Perfil.GESTOR);
            user.setUnidadeAtivaCodigo(20L); // Diferente

            when(unidadeService.buscarPorCodigo(20L)).thenReturn(new Unidade());
            when(unidadeService.temMapaVigente(10L)).thenReturn(true);
            when(hierarquiaService.ehMesmaOuSubordinada(any(), any())).thenReturn(true);

            PermissoesSubprocessoDto res = consultaService.obterPermissoesUI(sp, user);
            assertThat(res.habilitarAcessoCadastro()).isTrue();
        }

        @Test
        @DisplayName("obterPermissoesUI para SERVIDOR mesma unidade")
        void obterPermissoesUI_Servidor() {
            Subprocesso sp = criarSubprocessoComMapa(1L);
            sp.setSituacaoForcada(MAPEAMENTO_MAPA_DISPONIBILIZADO);
            Unidade u = new Unidade();
            u.setCodigo(10L);
            sp.setUnidade(u);

            Usuario user = criarUsuarioMock();
            user.setPerfilAtivo(Perfil.SERVIDOR);
            user.setUnidadeAtivaCodigo(10L);

            when(unidadeService.buscarPorCodigo(10L)).thenReturn(u);
            when(unidadeService.temMapaVigente(10L)).thenReturn(false);

            PermissoesSubprocessoDto res = consultaService.obterPermissoesUI(sp, user);
            assertThat(res.habilitarAcessoMapa()).isTrue();
        }

        @Test
        @DisplayName("obterPermissoesUI para ADMIN sem processo e com movimentacao de destino")
        void obterPermissoesUI_Admin() {
            Subprocesso sp = criarSubprocessoComMapa(1L);
            sp.setSituacaoForcada(REVISAO_MAPA_COM_SUGESTOES);
            Unidade u = new Unidade();
            u.setCodigo(10L);
            sp.setUnidade(u);

            Unidade dest = new Unidade();
            dest.setCodigo(30L);
            Movimentacao mov = new Movimentacao();
            mov.setUnidadeDestino(dest);
            sp.setLocalizacaoAtual(dest);

            Usuario user = new Usuario();
            user.setPerfilAtivo(Perfil.ADMIN);
            user.setUnidadeAtivaCodigo(20L);

            when(unidadeService.buscarPorCodigo(20L)).thenReturn(new Unidade());
            when(unidadeService.temMapaVigente(10L)).thenReturn(true);

            PermissoesSubprocessoDto res = consultaService.obterPermissoesUI(sp, user);
            assertThat(res.habilitarAcessoMapa()).isTrue();
        }

        @Test
        @DisplayName("obterPermissoesUI situacao DIAGNOSTICO")
        void obterPermissoesUI_Diagnostico() {
            Subprocesso sp = criarSubprocessoComMapa(1L);
            sp.setSituacaoForcada(DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO);
            Unidade u = new Unidade();
            u.setCodigo(10L);
            sp.setUnidade(u);

            Usuario user = new Usuario();
            user.setPerfilAtivo(Perfil.ADMIN);
            user.setUnidadeAtivaCodigo(10L);

            when(unidadeService.buscarPorCodigo(10L)).thenReturn(new Unidade());
            when(unidadeService.temMapaVigente(10L)).thenReturn(true);

            PermissoesSubprocessoDto res = consultaService.obterPermissoesUI(sp, user);
            assertThat(res.habilitarAcessoCadastro()).isFalse();
            assertThat(res.habilitarAcessoMapa()).isFalse();
            assertThat(res.podeReabrirCadastro()).isFalse();
            assertThat(res.podeReabrirRevisao()).isFalse();
        }

        @Test
        @DisplayName("obterPermissoesUI para ADMIN em REVISAO")
        void obterPermissoesUI_Admin_Revisao() {
            Subprocesso sp = criarSubprocessoComMapa(1L);
            sp.setSituacaoForcada(REVISAO_MAPA_HOMOLOGADO);
            Unidade u = new Unidade();
            u.setCodigo(10L);
            sp.setUnidade(u);

            Usuario user = new Usuario();
            user.setPerfilAtivo(Perfil.ADMIN);
            user.setUnidadeAtivaCodigo(10L);

            when(unidadeService.buscarPorCodigo(10L)).thenReturn(new Unidade());
            when(unidadeService.temMapaVigente(10L)).thenReturn(true);

            PermissoesSubprocessoDto res = consultaService.obterPermissoesUI(sp, user);
            assertThat(res.habilitarAcessoCadastro()).isTrue();
            assertThat(res.habilitarAcessoMapa()).isTrue();
            assertThat(res.podeReabrirRevisao()).isTrue();
        }

        @Test
        @DisplayName("obterPermissoesUI para GESTOR falha hierarquia")
        void obterPermissoesUI_Gestor_FalhaHierarquia() {
            Subprocesso sp = criarSubprocessoComMapa(1L);
            sp.setSituacaoForcada(REVISAO_MAPA_DISPONIBILIZADO);
            Unidade u = new Unidade();
            u.setCodigo(10L);
            sp.setUnidade(u);

            Usuario user = criarUsuarioMock();
            user.setPerfilAtivo(Perfil.GESTOR);
            user.setUnidadeAtivaCodigo(20L); // Unidade diferente

            when(unidadeService.buscarPorCodigo(20L)).thenReturn(new Unidade());
            when(unidadeService.temMapaVigente(10L)).thenReturn(true);
            when(hierarquiaService.ehMesmaOuSubordinada(any(), any())).thenReturn(false);

            PermissoesSubprocessoDto res = consultaService.obterPermissoesUI(sp, user);
            assertThat(res.habilitarAcessoCadastro()).isFalse();
            assertThat(res.habilitarAcessoMapa()).isFalse();
        }

        @Test
        @DisplayName("obterPermissoesUI isChefe false branch e isAdmin false branch em situacao especifica")
        void obterPermissoesUI_FalseBranches() {
            Subprocesso sp = criarSubprocessoComMapa(1L);
            sp.setSituacaoForcada(NAO_INICIADO);
            Unidade u = new Unidade();
            u.setCodigo(10L);
            sp.setUnidade(u);

            Usuario user = criarUsuarioMock();
            user.setPerfilAtivo(Perfil.SERVIDOR); // isChefe=false, isAdmin=false
            user.setUnidadeAtivaCodigo(10L);

            when(unidadeService.buscarPorCodigo(10L)).thenReturn(u);
            when(unidadeService.temMapaVigente(10L)).thenReturn(true);

            PermissoesSubprocessoDto res = consultaService.obterPermissoesUI(sp, user);
            assertThat(res.podeValidarMapa()).isFalse();
            assertThat(res.podeDisponibilizarMapa()).isFalse();
        }

        @Test
        @DisplayName("obterPermissoesUI processo null")
        void obterPermissoesUI_ProcessoNull() {
            Subprocesso sp = new Subprocesso();
            sp.setProcesso(null);
            sp.setSituacaoForcada(NAO_INICIADO);
            sp.setUnidade(new Unidade());
            sp.setCodigo(1L);

            Usuario user = new Usuario();
            user.setUnidadeAtivaCodigo(10L);
            user.setPerfilAtivo(Perfil.ADMIN);

            when(unidadeService.buscarPorCodigo(10L)).thenReturn(new Unidade());

            PermissoesSubprocessoDto res = consultaService.obterPermissoesUI(sp, user);
            assertThat(res).isNotNull();
        }

        @Test
        @DisplayName("obterPermissoesUI Gestor em unidade subordinada")
        void obterPermissoesUI_GestorSubordinada() {
            Subprocesso sp = criarSubprocessoComMapa(1L);
            sp.setSituacaoForcada(MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            Unidade uAlvo = new Unidade(); uAlvo.setCodigo(10L);
            sp.setUnidade(uAlvo);

            Usuario user = new Usuario();
            user.setUnidadeAtivaCodigo(20L);
            user.setPerfilAtivo(Perfil.GESTOR);
            Unidade uGestor = new Unidade(); uGestor.setCodigo(20L);

            when(unidadeService.buscarPorCodigo(20L)).thenReturn(uGestor);
            when(hierarquiaService.ehMesmaOuSubordinada(uAlvo, uGestor)).thenReturn(true);

            PermissoesSubprocessoDto res = consultaService.obterPermissoesUI(sp, user);
            assertTrue(res.habilitarAcessoCadastro());
        }

        @Test
        @DisplayName("obterPermissoesUI Servidor em unidade diferente")
        void obterPermissoesUI_ServidorDiferente() {
            Subprocesso sp = criarSubprocessoComMapa(1L);
            sp.setSituacaoForcada(MAPEAMENTO_MAPA_DISPONIBILIZADO);
            Unidade uAlvo = new Unidade(); uAlvo.setCodigo(10L);
            sp.setUnidade(uAlvo);

            Usuario user = new Usuario();
            user.setUnidadeAtivaCodigo(20L);
            user.setPerfilAtivo(Perfil.SERVIDOR);
            Unidade uUser = new Unidade(); uUser.setCodigo(20L);

            when(unidadeService.buscarPorCodigo(20L)).thenReturn(uUser);

            PermissoesSubprocessoDto res = consultaService.obterPermissoesUI(sp, user);
            assertFalse(res.habilitarAcessoMapa());
        }

        @Test
        @DisplayName("obterPermissoesUI situações de revisão para visualização")
        void obterPermissoesUI_SituacoesRevisaoVisualizacao() {
            Subprocesso sp = criarSubprocessoComMapa(1L, TipoProcesso.REVISAO);
            sp.setSituacaoForcada(REVISAO_CADASTRO_DISPONIBILIZADA);
            Unidade u = new Unidade(); u.setCodigo(10L); sp.setUnidade(u);

            Usuario user = new Usuario(); user.setPerfilAtivo(Perfil.ADMIN); user.setUnidadeAtivaCodigo(10L);
            when(unidadeService.buscarPorCodigo(10L)).thenReturn(u);

            PermissoesSubprocessoDto res = consultaService.obterPermissoesUI(sp, user);
            assertTrue(res.habilitarAcessoCadastro()); // branch 685 (REVISAO)
            
            sp.setSituacaoForcada(REVISAO_MAPA_DISPONIBILIZADO);
            res = consultaService.obterPermissoesUI(sp, user);
            assertTrue(res.habilitarAcessoMapa()); // branch 695 (REVISAO)
        }

        @Test
        @DisplayName("obterPermissoesUI podeDisponibilizarMapa varied situations")
        void obterPermissoesUI_PodeDisponibilizarMapa() {
            Subprocesso sp = criarSubprocessoComMapa(1L);
            Unidade u = new Unidade(); u.setCodigo(10L); sp.setUnidade(u);
            Usuario user = new Usuario(); user.setPerfilAtivo(Perfil.ADMIN); user.setUnidadeAtivaCodigo(10L);
            when(unidadeService.buscarPorCodigo(10L)).thenReturn(u);

            // test a few from the set in line 630
            for (SituacaoSubprocesso s : List.of(MAPEAMENTO_CADASTRO_HOMOLOGADO, MAPEAMENTO_MAPA_COM_SUGESTOES, REVISAO_MAPA_AJUSTADO)) {
                sp.setSituacaoForcada(s);
                assertTrue(consultaService.obterPermissoesUI(sp, user).podeDisponibilizarMapa());
            }
        }

        @Test
        @DisplayName("obterPermissoesUI podeValidarMapa para CHEFE em situação de análise")
        void obterPermissoesUI_ChefeAnalise() {
            Subprocesso sp = criarSubprocessoComMapa(1L);
            sp.setSituacaoForcada(MAPEAMENTO_MAPA_DISPONIBILIZADO);
            Unidade u = new Unidade(); u.setCodigo(10L); sp.setUnidade(u);

            Usuario user = new Usuario();
            user.setPerfilAtivo(Perfil.CHEFE);
            user.setUnidadeAtivaCodigo(10L);

            when(unidadeService.buscarPorCodigo(10L)).thenReturn(u);

            PermissoesSubprocessoDto res = consultaService.obterPermissoesUI(sp, user);
            assertThat(res.podeValidarMapa()).isTrue();
            assertThat(res.podeApresentarSugestoes()).isTrue();
        }

        @Test
        @DisplayName("obterMapaObrigatorio - deve lançar IllegalStateException se mapa for null")
        void obterMapaObrigatorioNull() {
            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setMapa(null);

            assertThatThrownBy(() -> consultaService.obterMapaObrigatorio(sp))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("sem mapa associado");
        }
    }

    @Nested
    @DisplayName("listarPorProcessoEUnidadeCodigosESituacoes")
    class ListarPorProcessoEUnidadeCodigosESituacoes {
        @Test
        @DisplayName("deve filtrar por situacoes")
        void deveFiltrar() {
            Subprocesso sp1 = criarSubprocessoComMapa(null);
            sp1.setSituacaoForcada(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            Subprocesso sp2 = criarSubprocessoComMapa(null);
            sp2.setSituacaoForcada(MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

            when(subprocessoRepo.listarPorProcessoEUnidadesComUnidade(1L, List.of(10L, 11L)))
                    .thenReturn(List.of(sp1, sp2));

            List<Subprocesso> res = consultaService.listarPorProcessoEUnidadeCodigosESituacoes(1L, List.of(10L, 11L), List.of(MAPEAMENTO_CADASTRO_EM_ANDAMENTO));

            assertThat(res).containsExactly(sp1);
        }
    }

    @Nested
    @DisplayName("listarAtividadesParaImportacao")
    class ListarAtividadesParaImportacao {
        @Test
        @DisplayName("deve lancar erro se processo nao finalizado")
        void processoNaoFinalizado() {
            Subprocesso sp = criarSubprocessoComMapa(null);
            Processo p = sp.getProcesso();
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
            when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(1L)).thenReturn(Optional.of(sp));
            assertThrows(ErroValidacao.class, () -> consultaService.listarAtividadesParaImportacao(1L));
        }

        @Test
        @DisplayName("deve lancar erro se processo for null")
        void processoNull() {
            Subprocesso sp = new Subprocesso();
            sp.setProcesso(null);
            when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(1L)).thenReturn(Optional.of(sp));
            assertThrows(ErroValidacao.class, () -> consultaService.listarAtividadesParaImportacao(1L));
        }

        @Test
        @DisplayName("deve retornar lista de atividades")
        void deveRetornarLista() {
            Subprocesso sp = criarSubprocessoComMapa(1L);
            sp.getProcesso().setSituacao(SituacaoProcesso.FINALIZADO);
            Mapa mapa = sp.getMapa();
            mapa.setCodigo(10L);

            when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(1L)).thenReturn(Optional.of(sp));
            when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(10L)).thenReturn(List.of());

            consultaService.listarAtividadesParaImportacao(1L);
            verify(mapaManutencaoService).atividadesMapaCodigoComConhecimentos(10L);
        }
    }

    @Nested
    @DisplayName("Análises e Histórico")
    class AnalisesEHistorico {
        @Test
        @DisplayName("listarAnalisesPorSubprocesso sem filtro")
        void listarAnalisesSemFiltro() {
            Analise a1 = new Analise();
            Analise a2 = new Analise();
            when(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(1L)).thenReturn(List.of(a1, a2));

            assertThat(consultaService.listarAnalisesPorSubprocesso(1L)).containsExactly(a1, a2);
        }

        @Test
        @DisplayName("listarAnalisesPorSubprocesso com filtro de tipo")
        void listarAnalisesComFiltro() {
            Analise a1 = new Analise(); a1.setTipo(TipoAnalise.CADASTRO);
            Analise a2 = new Analise(); a2.setTipo(TipoAnalise.VALIDACAO);
            when(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(1L)).thenReturn(List.of(a1, a2));

            assertThat(consultaService.listarAnalisesPorSubprocesso(1L, TipoAnalise.CADASTRO)).containsExactly(a1);
        }

        @Test
        @DisplayName("listarHistoricoCadastro e Validacao")
        void listarHistoricos() {
            Analise a1 = new Analise(); a1.setTipo(TipoAnalise.CADASTRO); a1.setUnidadeCodigo(10L);
            Analise a2 = new Analise(); a2.setTipo(TipoAnalise.VALIDACAO); a2.setUnidadeCodigo(10L);
            when(analiseRepo.findBySubprocessoCodigoOrderByDataHoraDesc(1L)).thenReturn(List.of(a1, a2));
            when(unidadeService.buscarPorCodigo(10L)).thenReturn(new Unidade());

            assertThat(consultaService.listarHistoricoCadastro(1L)).hasSize(1);
            assertThat(consultaService.listarHistoricoValidacao(1L)).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Localização Atual")
    class LocalizacaoAtual {
        @Test
        @DisplayName("obterLocalizacaoAtual - varios branches")
        void obterLocalizacaoAtual_Branches() {
            Subprocesso sp = new Subprocesso();
            Unidade u = new Unidade(); sp.setUnidade(u);
            
            // Branch: sp.getLocalizacaoAtual() != null
            sp.setLocalizacaoAtual(u);
            assertThat(consultaService.obterLocalizacaoAtual(sp)).isEqualTo(u);

            // Branch: sp.getCodigo() == null
            sp.setLocalizacaoAtual(null);
            sp.setCodigo(null);
            assertThat(consultaService.obterLocalizacaoAtual(sp)).isEqualTo(u);

            // Branch: m.getUnidadeDestino() != null
            sp.setCodigo(1L);
            Unidade dest = new Unidade();
            Movimentacao m = new Movimentacao(); m.setUnidadeDestino(dest);
            when(movimentacaoRepo.buscarUltimaPorSubprocesso(1L)).thenReturn(Optional.of(m));
            assertThat(consultaService.obterLocalizacaoAtual(sp)).isEqualTo(dest);
            
            // Branch: m.getUnidadeDestino() == null
            sp.setLocalizacaoAtual(null);
            m.setUnidadeDestino(null);
            assertThat(consultaService.obterLocalizacaoAtual(sp)).isEqualTo(u);
            
            // Branch: findFirstBy... returns Optional.empty()
            sp.setLocalizacaoAtual(null);
            when(movimentacaoRepo.buscarUltimaPorSubprocesso(1L)).thenReturn(Optional.empty());
            assertThat(consultaService.obterLocalizacaoAtual(sp)).isEqualTo(u);
        }
    }
}
