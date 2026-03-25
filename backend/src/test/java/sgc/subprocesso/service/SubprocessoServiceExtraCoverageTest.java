package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.comum.erros.*;
import sgc.comum.model.*;
import sgc.mapa.dto.*;
import sgc.mapa.model.*;
import sgc.mapa.service.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.seguranca.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoService Extra Coverage Test")
class SubprocessoServiceExtraCoverageTest {

    @Mock
    private SubprocessoRepo subprocessoRepo;
    @Mock
    private ComumRepo repo;
    @Mock
    private UnidadeService unidadeService;
    @Mock
    private MapaSalvamentoService mapaSalvamentoService;
    @Mock
    private MapaManutencaoService mapaManutencaoService;
    @Mock
    private SubprocessoValidacaoService validacaoService;
    @Mock
    private UsuarioFacade usuarioFacade;
    @Mock
    private MovimentacaoRepo movimentacaoRepo;
    @Mock
    private SgcPermissionEvaluator permissionEvaluator;
    @Mock
    private HierarquiaService hierarquiaService;
    @Mock
    private CopiaMapaService copiaMapaService;

    @InjectMocks
    private SubprocessoService subprocessoService;

    private Subprocesso criarSubprocessoComMapa(Long codigo) {
        return criarSubprocessoComMapa(codigo, TipoProcesso.MAPEAMENTO);
    }

    private Subprocesso criarSubprocessoComMapa(Long codigo, TipoProcesso tipo) {
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
            Unidade u1 = new Unidade(); u1.setCodigo(1L);
            Unidade u2 = new Unidade(); u2.setCodigo(2L);
            Subprocesso sp = criarSubprocessoComMapa(100L); sp.setUnidade(u1);
            Movimentacao mov = new Movimentacao(); mov.setUnidadeDestino(u2);

            when(movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(100L)).thenReturn(List.of(mov));

            assertThat(subprocessoService.obterUnidadeLocalizacao(sp)).isEqualTo(u2);
        }

        @Test
        @DisplayName("deve retornar unidade do subprocesso se movimentacao nao tiver destino e localizacaoAtual for null")
        void deveRetornarUnidadeSubprocesso() {
            Unidade u1 = new Unidade(); u1.setCodigo(1L);
            Subprocesso sp = criarSubprocessoComMapa(100L); sp.setUnidade(u1);
            Movimentacao mov = new Movimentacao(); mov.setUnidadeDestino(null);

            when(movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(100L)).thenReturn(List.of(mov));

            assertThat(subprocessoService.obterUnidadeLocalizacao(sp)).isEqualTo(u1);
        }

        @Test
        @DisplayName("deve retornar unidade do subprocesso se codigo for null")
        void codNull() {
            Unidade u = new Unidade();
            Subprocesso sp = criarSubprocessoComMapa(null); sp.setUnidade(u);
            assertThat(subprocessoService.obterUnidadeLocalizacao(sp)).isEqualTo(u);
        }

        @Test
        @DisplayName("deve retornar unidade do subprocesso se movimentacoes vazio")
        void movVazio() {
            Unidade u = new Unidade();
            Subprocesso sp = criarSubprocessoComMapa(1L); sp.setUnidade(u);
            when(movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(1L)).thenReturn(List.of());
            assertThat(subprocessoService.obterUnidadeLocalizacao(sp)).isEqualTo(u);
        }
    }

    @Nested
    @DisplayName("Manutenção de Mapa")
    class ManutencaoMapa {
        @Test
        @DisplayName("deve salvar mapa de subprocesso mudando situacao se estava vazio e tem novas competencias")
        void salvarMapaSubprocesso_EraVazioTemNovasCompetencias() {
            Subprocesso sp = criarSubprocessoComMapa(1L);
            sp.setSituacaoForcada(MAPEAMENTO_CADASTRO_HOMOLOGADO);
            sp.getMapa().setCodigo(100L);

            when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(1L)).thenReturn(Optional.of(sp));
            when(mapaManutencaoService.competenciasCodMapa(100L)).thenReturn(List.of()); // eraVazio = true
            when(mapaSalvamentoService.salvarMapaCompleto(eq(100L), any())).thenReturn(sp.getMapa());

            SalvarMapaRequest request = new SalvarMapaRequest("Desc", List.of(new SalvarMapaRequest.CompetenciaRequest(null, "Comp", List.of()))); // temNovas = true
            subprocessoService.salvarMapaSubprocesso(1L, request);

            verify(mapaManutencaoService).reconciliarSituacaoSubprocesso(sp);
            assertThat(sp.getSituacao()).isEqualTo(MAPEAMENTO_MAPA_CRIADO); // Mudou situacao
        }

        @Test
        @DisplayName("deve adicionar competencia e atualizar situacao se era vazio em REVISAO")
        void adicionarCompetencia_EraVazioEmRevisao() {
            Subprocesso sp = criarSubprocessoComMapa(1L, TipoProcesso.REVISAO);
            sp.setSituacaoForcada(REVISAO_CADASTRO_HOMOLOGADA);
            sp.getMapa().setCodigo(100L);

            when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(1L)).thenReturn(Optional.of(sp));
            when(mapaManutencaoService.competenciasCodMapa(100L)).thenReturn(List.of()); // eraVazio = true
            when(mapaManutencaoService.mapaCodigo(100L)).thenReturn(sp.getMapa());

            subprocessoService.adicionarCompetencia(1L, new CompetenciaRequest("Desc", List.of()));

            assertThat(sp.getSituacao()).isEqualTo(REVISAO_MAPA_AJUSTADO); // Mudou situacao
        }

        @Test
        @DisplayName("deve remover competencia e atualizar situacao se ficou vazio em MAPEAMENTO")
        void removerCompetencia_FicouVazioEmMapeamento() {
            Subprocesso sp = criarSubprocessoComMapa(1L);
            sp.setSituacaoForcada(MAPEAMENTO_MAPA_CRIADO);
            sp.getMapa().setCodigo(100L);

            when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(1L)).thenReturn(Optional.of(sp));
            when(mapaManutencaoService.competenciasCodMapa(100L)).thenReturn(List.of()); // ficouVazio = true
            when(mapaManutencaoService.mapaCodigo(100L)).thenReturn(sp.getMapa());

            subprocessoService.removerCompetencia(1L, 10L);

            assertThat(sp.getSituacao()).isEqualTo(MAPEAMENTO_CADASTRO_HOMOLOGADO); // Mudou situacao
        }

        @Test
        @DisplayName("deve remover competencia e atualizar situacao se ficou vazio em REVISAO")
        void removerCompetencia_FicouVazioEmRevisao() {
            Subprocesso sp = criarSubprocessoComMapa(1L, TipoProcesso.REVISAO);
            sp.setSituacaoForcada(REVISAO_MAPA_AJUSTADO);
            sp.getMapa().setCodigo(100L);

            when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(1L)).thenReturn(Optional.of(sp));
            when(mapaManutencaoService.competenciasCodMapa(100L)).thenReturn(List.of()); // ficouVazio = true
            when(mapaManutencaoService.mapaCodigo(100L)).thenReturn(sp.getMapa());

            subprocessoService.removerCompetencia(1L, 10L);

            assertThat(sp.getSituacao()).isEqualTo(REVISAO_CADASTRO_HOMOLOGADA); // Mudou situacao
        }

        @Test
        @DisplayName("deve atualizar competencias validando nulidade")
        void atualizarCompetenciasNulidade() {
            Subprocesso sp = criarSubprocessoComMapa(1L, TipoProcesso.REVISAO);
            sp.setSituacaoForcada(REVISAO_MAPA_AJUSTADO);

            when(repo.buscar(Subprocesso.class, 1L)).thenReturn(sp);
            
            CompetenciaAjusteDto compDto = new CompetenciaAjusteDto(10L, "Nome", List.of());
            when(mapaManutencaoService.competenciasCodigos(anyList())).thenReturn(List.of()); // mapa vazio, competencia = null

            subprocessoService.salvarAjustesMapa(1L, List.of(compDto));
            
            verify(subprocessoRepo).save(sp);
            assertThat(sp.getSituacao()).isEqualTo(REVISAO_MAPA_AJUSTADO);
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
            when(movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(1L)).thenReturn(List.of());
            when(unidadeService.buscarPorCodigo(10L)).thenReturn(u);

            SubprocessoDetalheResponse res = subprocessoService.obterDetalhes(1L, user);
            
            assertThat(res.permissoes().habilitarAcessoCadastro()).isTrue();
            assertThat(res.permissoes().habilitarAcessoMapa()).isTrue();
        }

        @Test
        @DisplayName("obterDetalhes com movimentacao completa")
        void obterDetalhes_MovimentacaoCompleta() {
            Subprocesso sp = criarSubprocessoComMapa(1L);
            sp.setSituacaoForcada(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            Unidade u = new Unidade(); u.setSigla("U1"); u.setCodigo(10L);
            sp.setUnidade(u);

            Usuario user = criarUsuarioMock(); user.setPerfilAtivo(Perfil.ADMIN); user.setUnidadeAtivaCodigo(10L);

            Unidade dest = new Unidade(); dest.setCodigo(20L); dest.setSigla("U2"); dest.setNome("Dest");
            Movimentacao mov = new Movimentacao();
            mov.setUnidadeOrigem(u);
            mov.setUnidadeDestino(dest);
            mov.setUsuario(user);

            when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(1L)).thenReturn(Optional.of(sp));
            when(movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(1L)).thenReturn(List.of(mov));
            when(unidadeService.buscarPorCodigo(10L)).thenReturn(u);

            SubprocessoDetalheResponse res = subprocessoService.obterDetalhes(1L, user);
            assertThat(res.localizacaoAtual()).isEqualTo("U2");
        }

        @Test
        @DisplayName("obterPermissoesUI para GESTOR")
        void obterPermissoesUI_Gestor() {
            Subprocesso sp = criarSubprocessoComMapa(1L);
            sp.setSituacaoForcada(MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
            Unidade u = new Unidade(); u.setCodigo(10L);
            sp.setUnidade(u);
            
            Usuario user = criarUsuarioMock();
            user.setPerfilAtivo(Perfil.GESTOR);
            user.setUnidadeAtivaCodigo(20L); // Diferente

            when(unidadeService.buscarPorCodigo(20L)).thenReturn(new Unidade());
            when(unidadeService.verificarMapaVigente(10L)).thenReturn(true);
            when(hierarquiaService.ehMesmaOuSubordinada(any(), any())).thenReturn(true);

            PermissoesSubprocessoDto res = subprocessoService.obterPermissoesUI(sp, user);
            assertThat(res.habilitarAcessoCadastro()).isTrue();
        }

        @Test
        @DisplayName("obterPermissoesUI para SERVIDOR mesma unidade")
        void obterPermissoesUI_Servidor() {
            Subprocesso sp = criarSubprocessoComMapa(1L);
            sp.setSituacaoForcada(MAPEAMENTO_MAPA_DISPONIBILIZADO);
            Unidade u = new Unidade(); u.setCodigo(10L);
            sp.setUnidade(u);
            
            Usuario user = criarUsuarioMock();
            user.setPerfilAtivo(Perfil.SERVIDOR);
            user.setUnidadeAtivaCodigo(10L);

            when(unidadeService.buscarPorCodigo(10L)).thenReturn(u);
            when(unidadeService.verificarMapaVigente(10L)).thenReturn(false);

            PermissoesSubprocessoDto res = subprocessoService.obterPermissoesUI(sp, user);
            assertThat(res.habilitarAcessoMapa()).isTrue();
        }

        @Test
        @DisplayName("obterPermissoesUI para ADMIN sem processo e com movimentacao de destino")
        void obterPermissoesUI_Admin() {
            Subprocesso sp = criarSubprocessoComMapa(1L);
            sp.setSituacaoForcada(REVISAO_MAPA_COM_SUGESTOES);
            Unidade u = new Unidade(); u.setCodigo(10L);
            sp.setUnidade(u);
            
            Unidade dest = new Unidade(); dest.setCodigo(30L);
            Movimentacao mov = new Movimentacao(); mov.setUnidadeDestino(dest);
            sp.setLocalizacaoAtual(dest);

            Usuario user = new Usuario();
            user.setPerfilAtivo(Perfil.ADMIN);
            user.setUnidadeAtivaCodigo(20L); 

            when(unidadeService.buscarPorCodigo(20L)).thenReturn(new Unidade());
            when(unidadeService.verificarMapaVigente(10L)).thenReturn(true);

            PermissoesSubprocessoDto res = subprocessoService.obterPermissoesUI(sp, user);
            assertThat(res.habilitarAcessoMapa()).isTrue();
        }

        @Test
        @DisplayName("obterPermissoesUI situacao DIAGNOSTICO")
        void obterPermissoesUI_Diagnostico() {
            Subprocesso sp = criarSubprocessoComMapa(1L);
            sp.setSituacaoForcada(DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO);
            Unidade u = new Unidade(); u.setCodigo(10L);
            sp.setUnidade(u);
            
            Usuario user = new Usuario();
            user.setPerfilAtivo(Perfil.ADMIN);
            user.setUnidadeAtivaCodigo(10L);

            when(unidadeService.buscarPorCodigo(10L)).thenReturn(new Unidade());
            when(unidadeService.verificarMapaVigente(10L)).thenReturn(true);

            PermissoesSubprocessoDto res = subprocessoService.obterPermissoesUI(sp, user);
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
            Unidade u = new Unidade(); u.setCodigo(10L);
            sp.setUnidade(u);

            Usuario user = new Usuario();
            user.setPerfilAtivo(Perfil.ADMIN);
            user.setUnidadeAtivaCodigo(10L);

            when(unidadeService.buscarPorCodigo(10L)).thenReturn(new Unidade());
            when(unidadeService.verificarMapaVigente(10L)).thenReturn(true);

            PermissoesSubprocessoDto res = subprocessoService.obterPermissoesUI(sp, user);
            assertThat(res.habilitarAcessoCadastro()).isTrue();
            assertThat(res.habilitarAcessoMapa()).isTrue();
            assertThat(res.podeReabrirRevisao()).isTrue();
        }

        @Test
        @DisplayName("obterPermissoesUI para GESTOR falha hierarquia")
        void obterPermissoesUI_Gestor_FalhaHierarquia() {
            Subprocesso sp = criarSubprocessoComMapa(1L);
            sp.setSituacaoForcada(REVISAO_MAPA_DISPONIBILIZADO);
            Unidade u = new Unidade(); u.setCodigo(10L);
            sp.setUnidade(u);

            Usuario user = criarUsuarioMock();
            user.setPerfilAtivo(Perfil.GESTOR);
            user.setUnidadeAtivaCodigo(20L); // Unidade diferente

            when(unidadeService.buscarPorCodigo(20L)).thenReturn(new Unidade());
            when(unidadeService.verificarMapaVigente(10L)).thenReturn(true);
            when(hierarquiaService.ehMesmaOuSubordinada(any(), any())).thenReturn(false);

            PermissoesSubprocessoDto res = subprocessoService.obterPermissoesUI(sp, user);
            assertThat(res.habilitarAcessoCadastro()).isFalse();
            assertThat(res.habilitarAcessoMapa()).isFalse();
        }

        @Test
        @DisplayName("obterPermissoesUI isChefe false branch e isAdmin false branch em situacao especifica")
        void obterPermissoesUI_FalseBranches() {
            Subprocesso sp = criarSubprocessoComMapa(1L);
            sp.setSituacaoForcada(NAO_INICIADO);
            Unidade u = new Unidade(); u.setCodigo(10L); sp.setUnidade(u);
            
            Usuario user = criarUsuarioMock();
            user.setPerfilAtivo(Perfil.SERVIDOR); // isChefe=false, isAdmin=false
            user.setUnidadeAtivaCodigo(10L);

            when(unidadeService.buscarPorCodigo(10L)).thenReturn(u);
            when(unidadeService.verificarMapaVigente(10L)).thenReturn(true);

            PermissoesSubprocessoDto res = subprocessoService.obterPermissoesUI(sp, user);
            assertThat(res.podeValidarMapa()).isFalse();
            assertThat(res.podeDisponibilizarMapa()).isFalse();
        }
    }

    @Nested
    @DisplayName("listarPorProcessoEUnidadeCodigosESituacoes")
    class ListarPorProcessoEUnidadeCodigosESituacoes {
        @Test
        @DisplayName("deve filtrar por situacoes")
        void deveFiltrar() {
            Subprocesso sp1 = criarSubprocessoComMapa(null); sp1.setSituacaoForcada(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            Subprocesso sp2 = criarSubprocessoComMapa(null); sp2.setSituacaoForcada(MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

            when(subprocessoRepo.findByProcessoCodigoAndUnidadeCodigoInWithUnidade(1L, List.of(10L, 11L)))
                    .thenReturn(List.of(sp1, sp2));

            List<Subprocesso> res = subprocessoService.listarPorProcessoEUnidadeCodigosESituacoes(1L, List.of(10L, 11L), List.of(MAPEAMENTO_CADASTRO_EM_ANDAMENTO));

            assertThat(res).containsExactly(sp1);
        }
    }

    @Nested
    @DisplayName("importarAtividades")
    class ImportarAtividades {
        @Test
        @DisplayName("deve lancar erro se nao tiver permissao no destino")
        void semPermissaoDestino() {
            Subprocesso sp = criarSubprocessoComMapa(null);
            when(repo.buscar(Subprocesso.class, 1L)).thenReturn(sp);
            Usuario user = criarUsuarioMock();
            when(usuarioFacade.usuarioAutenticado()).thenReturn(user);
            when(permissionEvaluator.verificarPermissao(user, sp, AcaoPermissao.EDITAR_CADASTRO)).thenReturn(false);
            
            assertThrows(ErroAcessoNegado.class, () -> subprocessoService.importarAtividades(1L, 2L, List.of()));
        }

        @Test
        @DisplayName("deve importar atividades com sucesso")
        void sucesso() {
            Subprocesso spDest = criarSubprocessoComMapa(1L);
            spDest.getMapa().setCodigo(100L);
            
            Subprocesso spOrig = criarSubprocessoComMapa(2L);
            spOrig.getProcesso().setSituacao(SituacaoProcesso.FINALIZADO);
            spOrig.getMapa().setCodigo(200L);
            
            Unidade uOrig = new Unidade(); uOrig.setSigla("UORIG");
            spOrig.setUnidade(uOrig);

            when(repo.buscar(Subprocesso.class, 1L)).thenReturn(spDest);
            when(repo.buscar(Subprocesso.class, 2L)).thenReturn(spOrig);
            Usuario user = criarUsuarioMock();
            when(usuarioFacade.usuarioAutenticado()).thenReturn(user);
            when(permissionEvaluator.verificarPermissao(user, spDest, AcaoPermissao.EDITAR_CADASTRO)).thenReturn(true);
            when(permissionEvaluator.verificarPermissao(user, spOrig, AcaoPermissao.CONSULTAR_PARA_IMPORTACAO)).thenReturn(true);
            
            subprocessoService.importarAtividades(1L, 2L, List.of());
            verify(subprocessoRepo).save(spDest);
            verify(copiaMapaService).importarAtividadesDeOutroMapa(eq(200L), eq(100L), any());
            assertThat(spDest.getSituacao()).isEqualTo(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        }

        @Test
        @DisplayName("deve importar atividades com sucesso e mudar situacao para REVISAO")
        void sucessoRevisao() {
            Subprocesso spDest = criarSubprocessoComMapa(1L, TipoProcesso.REVISAO);
            spDest.setSituacaoForcada(NAO_INICIADO);
            spDest.getMapa().setCodigo(100L);
            
            Subprocesso spOrig = criarSubprocessoComMapa(2L);
            spOrig.getProcesso().setSituacao(SituacaoProcesso.FINALIZADO);
            spOrig.getMapa().setCodigo(200L);
            spOrig.setUnidade(new Unidade());

            when(repo.buscar(Subprocesso.class, 1L)).thenReturn(spDest);
            when(repo.buscar(Subprocesso.class, 2L)).thenReturn(spOrig);
            Usuario user = criarUsuarioMock();
            when(usuarioFacade.usuarioAutenticado()).thenReturn(user);
            when(permissionEvaluator.verificarPermissao(eq(user), eq(spDest), any())).thenReturn(true);
            when(permissionEvaluator.verificarPermissao(eq(user), eq(spOrig), any())).thenReturn(true);
            
            subprocessoService.importarAtividades(1L, 2L, List.of());
            assertThat(spDest.getSituacao()).isEqualTo(REVISAO_CADASTRO_EM_ANDAMENTO);
        }

        @Test
        @DisplayName("deve falhar se nao tiver permissao na origem")
        void semPermissaoOrigem() {
            Subprocesso spDest = criarSubprocessoComMapa(1L);
            Subprocesso spOrig = criarSubprocessoComMapa(2L);
            
            when(repo.buscar(Subprocesso.class, 1L)).thenReturn(spDest);
            when(repo.buscar(Subprocesso.class, 2L)).thenReturn(spOrig);
            Usuario user = criarUsuarioMock();
            when(usuarioFacade.usuarioAutenticado()).thenReturn(user);
            when(permissionEvaluator.verificarPermissao(user, spDest, AcaoPermissao.EDITAR_CADASTRO)).thenReturn(true);
            when(permissionEvaluator.verificarPermissao(user, spOrig, AcaoPermissao.CONSULTAR_PARA_IMPORTACAO)).thenReturn(false);
            
            assertThrows(ErroAcessoNegado.class, () -> subprocessoService.importarAtividades(1L, 2L, List.of()));
        }
    }

    @Nested
    @DisplayName("listarAtividadesParaImportacao")
    class ListarAtividadesParaImportacao {
        @Test
        @DisplayName("deve lancar erro se processo nao finalizado")
        void processoNaoFinalizado() {
            Subprocesso sp = criarSubprocessoComMapa(null);
            Processo p = sp.getProcesso(); p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
            when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(1L)).thenReturn(Optional.of(sp));
            assertThrows(ErroValidacao.class, () -> subprocessoService.listarAtividadesParaImportacao(1L));
        }

        @Test
        @DisplayName("deve retornar lista de atividades")
        void deveRetornarLista() {
            Subprocesso sp = criarSubprocessoComMapa(1L);
            sp.getProcesso().setSituacao(SituacaoProcesso.FINALIZADO);
            Mapa mapa = sp.getMapa(); mapa.setCodigo(10L);
            
            when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(1L)).thenReturn(Optional.of(sp));
            when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(10L)).thenReturn(List.of());
            
            subprocessoService.listarAtividadesParaImportacao(1L);
            verify(mapaManutencaoService).atividadesMapaCodigoComConhecimentos(10L);
        }
    }

    @Nested
    @DisplayName("Criação de Subprocesso")
    class CriacaoSubprocesso {
        @Test
        @DisplayName("criarParaMapeamento com todos os tipos de unidades")
        void criarParaMapeamento_TiposUnidades() {
            Processo p = new Processo(); p.setCodigo(1L); p.setDataLimite(LocalDateTime.now());
            Unidade u1 = new Unidade(); u1.setCodigo(1L); u1.setTipo(TipoUnidade.OPERACIONAL);
            Unidade u2 = new Unidade(); u2.setCodigo(2L); u2.setTipo(TipoUnidade.INTEROPERACIONAL);
            Unidade u3 = new Unidade(); u3.setCodigo(3L); u3.setTipo(TipoUnidade.RAIZ);
            Unidade u4 = new Unidade(); u4.setCodigo(4L); u4.setTipo(TipoUnidade.INTERMEDIARIA); // Elegivel=false

            subprocessoService.criarParaMapeamento(p, List.of(u1, u2, u3, u4), null, new Usuario());
            
            verify(subprocessoRepo).saveAll(any());
        }
    }

    @Nested
    @DisplayName("Atualizacao de Subprocesso")
    class AtualizacaoDeSubprocesso {
        @Test
        @DisplayName("deve atualizar subprocesso com todas as datas e mapa")
        void atualizarSubprocesso_Completo() {
            Subprocesso sp = criarSubprocessoComMapa(1L);
            
            LocalDateTime d1 = LocalDateTime.now();
            LocalDateTime f1 = LocalDateTime.now().plusDays(1);
            LocalDateTime d2 = LocalDateTime.now().plusDays(2);
            LocalDateTime f2 = LocalDateTime.now().plusDays(3);
            
            AtualizarSubprocessoRequest request = new AtualizarSubprocessoRequest(10L, 100L, d1, f1, d2, f2);
            
            when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(1L)).thenReturn(Optional.of(sp));
            when(subprocessoRepo.save(any())).thenReturn(sp);
            
            subprocessoService.atualizarEntidade(1L, request);
            
            assertThat(sp.getDataLimiteEtapa1()).isEqualTo(d1);
            assertThat(sp.getDataFimEtapa1()).isEqualTo(f1);
            assertThat(sp.getDataLimiteEtapa2()).isEqualTo(d2);
            assertThat(sp.getDataFimEtapa2()).isEqualTo(f2);
            assertThat(sp.getMapa()).isNotNull();
            assertThat(sp.getMapa().getCodigo()).isEqualTo(100L);
        }

        @Test
        @DisplayName("deve atualizar subprocesso ignorando datas nulas")
        void atualizarSubprocesso_DatasNulas() {
            Subprocesso sp = criarSubprocessoComMapa(1L);
            LocalDateTime d1 = LocalDateTime.now();
            sp.setDataLimiteEtapa1(d1);
            
            AtualizarSubprocessoRequest request = new AtualizarSubprocessoRequest(10L, 100L, null, null, null, null);
            
            when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(1L)).thenReturn(Optional.of(sp));
            when(subprocessoRepo.save(any())).thenReturn(sp);
            
            subprocessoService.atualizarEntidade(1L, request);
            
            assertThat(sp.getDataLimiteEtapa1()).isEqualTo(d1); // Não mudou
            assertThat(sp.getMapa().getCodigo()).isEqualTo(100L);
        }
    }
}
