package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.comum.erros.*;
import sgc.comum.model.*;
import sgc.mapa.model.*;
import sgc.mapa.service.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.seguranca.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubprocessoService Extra Coverage Test")
class SubprocessoServiceExtraCoverageTest {

    @InjectMocks
    private SubprocessoService subprocessoService;

    @Mock private SubprocessoRepo subprocessoRepo;
    @Mock private MovimentacaoRepo movimentacaoRepo;
    @Mock private UnidadeService unidadeService;
    @Mock private MapaManutencaoService mapaManutencaoService;
    @Mock private CopiaMapaService copiaMapaService;
    @Mock private ComumRepo repo;
    @Mock private UsuarioFacade usuarioFacade;
    @Mock private SgcPermissionEvaluator permissionEvaluator;
    @Mock private HierarquiaService hierarquiaService;
    @Mock private SubprocessoValidacaoService validacaoService;
    @Mock private AnaliseRepo analiseRepo;
    @Mock private ImpactoMapaService impactoMapaService;
    @Mock private MapaSalvamentoService mapaSalvamentoService;
    @Mock private MapaVisualizacaoService mapaVisualizacaoService;

    @BeforeEach
    void setUp() {
        // As dependências agora são injetadas via construtor, Mockito @InjectMocks cuida disso.
    }


    @Nested
    @DisplayName("obterUnidadeLocalizacao")
    class ObterUnidadeLocalizacao {
        @Test
        @DisplayName("deve retornar a unidade destino da ultima movimentacao se localizacaoAtual for null")
        void deveRetornarDestinoMovimentacao() {
            Unidade u1 = new Unidade(); u1.setCodigo(1L);
            Unidade u2 = new Unidade(); u2.setCodigo(2L);
            Subprocesso sp = new Subprocesso(); sp.setCodigo(100L); sp.setUnidade(u1);
            Movimentacao mov = new Movimentacao(); mov.setUnidadeDestino(u2);

            when(movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(100L)).thenReturn(List.of(mov));

            assertThat(subprocessoService.obterUnidadeLocalizacao(sp)).isEqualTo(u2);
        }

        @Test
        @DisplayName("deve retornar unidade do subprocesso se movimentacao nao tiver destino e localizacaoAtual for null")
        void deveRetornarUnidadeSubprocesso() {
            Unidade u1 = new Unidade(); u1.setCodigo(1L);
            Subprocesso sp = new Subprocesso(); sp.setCodigo(100L); sp.setUnidade(u1);
            Movimentacao mov = new Movimentacao(); mov.setUnidadeDestino(null);

            when(movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(100L)).thenReturn(List.of(mov));

            assertThat(subprocessoService.obterUnidadeLocalizacao(sp)).isEqualTo(u1);
        }

        @Test
        @DisplayName("deve retornar unidade do subprocesso se codigo for null")
        void codNull() {
            Unidade u = new Unidade();
            Subprocesso sp = new Subprocesso(); sp.setUnidade(u); sp.setCodigo(null);
            assertThat(subprocessoService.obterUnidadeLocalizacao(sp)).isEqualTo(u);
        }

        @Test
        @DisplayName("deve retornar unidade do subprocesso se movimentacoes vazio")
        void movVazio() {
            Unidade u = new Unidade();
            Subprocesso sp = new Subprocesso(); sp.setUnidade(u); sp.setCodigo(1L);
            when(movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(1L)).thenReturn(List.of());
            assertThat(subprocessoService.obterUnidadeLocalizacao(sp)).isEqualTo(u);
        }
    }

    @Nested
    @DisplayName("listarPorProcessoEUnidadeCodigosESituacoes")
    class ListarPorProcessoEUnidadeCodigosESituacoes {
        @Test
        @DisplayName("deve filtrar por situacoes")
        void deveFiltrar() {
            Subprocesso sp1 = new Subprocesso(); sp1.setSituacao(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            Subprocesso sp2 = new Subprocesso(); sp2.setSituacao(MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

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
            Subprocesso sp = new Subprocesso();
            when(repo.buscar(Subprocesso.class, 1L)).thenReturn(sp);
            Usuario user = new Usuario();
            when(usuarioFacade.usuarioAutenticado()).thenReturn(user);
            when(permissionEvaluator.verificarPermissao(user, sp, AcaoPermissao.EDITAR_CADASTRO)).thenReturn(false);

            List<Long> ativs = List.of();
            assertThrows(ErroAcessoNegado.class, () -> subprocessoService.importarAtividades(1L, 2L, ativs));
        }

        @Test
        @DisplayName("deve lancar erro se nao tiver permissao na origem")
        void semPermissaoOrigem() {
            Subprocesso spDest = new Subprocesso(); spDest.setSituacao(NAO_INICIADO);
            Subprocesso spOrig = new Subprocesso();
            
            when(repo.buscar(Subprocesso.class, 1L)).thenReturn(spDest);
            when(repo.buscar(Subprocesso.class, 2L)).thenReturn(spOrig);
            Usuario user = new Usuario();
            when(usuarioFacade.usuarioAutenticado()).thenReturn(user);
            when(permissionEvaluator.verificarPermissao(user, spDest, AcaoPermissao.EDITAR_CADASTRO)).thenReturn(true);
            when(permissionEvaluator.verificarPermissao(user, spOrig, AcaoPermissao.CONSULTAR_PARA_IMPORTACAO)).thenReturn(false);

            List<Long> ativs = List.of();
            assertThrows(ErroAcessoNegado.class, () -> subprocessoService.importarAtividades(1L, 2L, ativs));
        }

        @Test
        @DisplayName("deve cobrir default case do switch (DIAGNOSTICO) na linha 789")
        void deveCobrirDefaultSwitch() {
            Processo p = new Processo(); p.setTipo(TipoProcesso.DIAGNOSTICO);
            Subprocesso spDest = new Subprocesso(); spDest.setCodigo(1L); spDest.setProcesso(p); spDest.setSituacaoForcada(NAO_INICIADO);
            Mapa mapaDest = new Mapa(); mapaDest.setCodigo(10L); spDest.setMapa(mapaDest);
            Subprocesso spOrig = new Subprocesso(); spOrig.setCodigo(2L);
            Unidade uOrig = new Unidade(); uOrig.setSigla("ORIG"); spOrig.setUnidade(uOrig);
            Mapa mapaOrig = new Mapa(); mapaOrig.setCodigo(20L); spOrig.setMapa(mapaOrig);
            
            when(repo.buscar(Subprocesso.class, 1L)).thenReturn(spDest);
            when(repo.buscar(Subprocesso.class, 2L)).thenReturn(spOrig);
            Usuario user = new Usuario();
            when(usuarioFacade.usuarioAutenticado()).thenReturn(user);
            when(permissionEvaluator.verificarPermissao(user, spDest, AcaoPermissao.EDITAR_CADASTRO)).thenReturn(true);
            when(permissionEvaluator.verificarPermissao(user, spOrig, AcaoPermissao.CONSULTAR_PARA_IMPORTACAO)).thenReturn(true);
            
            subprocessoService.importarAtividades(1L, 2L, null);
            verify(subprocessoRepo).save(spDest);
            assertThat(spDest.getSituacao()).isEqualTo(NAO_INICIADO);
        }
    }

    @Nested
    @DisplayName("listarAtividadesParaImportacao")
    class ListarAtividadesParaImportacao {
        @Test
        @DisplayName("deve lancar erro se processo nao finalizado")
        void processoNaoFinalizado() {
            Subprocesso sp = new Subprocesso();
            Processo p = new Processo(); p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
            sp.setProcesso(p);
            when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(1L)).thenReturn(Optional.of(sp));
            assertThrows(ErroValidacao.class, () -> subprocessoService.listarAtividadesParaImportacao(1L));
        }

        @Test
        @DisplayName("deve retornar lista de atividades na linha 828")
        void deveRetornarLista() {
            Processo p = new Processo(); p.setSituacao(SituacaoProcesso.FINALIZADO);
            Subprocesso sp = new Subprocesso(); sp.setCodigo(1L); sp.setProcesso(p);
            Mapa mapa = new Mapa(); mapa.setCodigo(10L); sp.setMapa(mapa);
            
            when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(1L)).thenReturn(Optional.of(sp));
            when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(10L)).thenReturn(List.of());
            
            subprocessoService.listarAtividadesParaImportacao(1L);
            verify(mapaManutencaoService).atividadesMapaCodigoComConhecimentos(10L);
        }
    }

    @Test
    @DisplayName("deve cobrir verificarAcessoCadastroHabilitado para perfil SERVIDOR na linha 692")
    void deveCobrirLinha692() {
        Usuario user = new Usuario(); user.setPerfilAtivo(Perfil.SERVIDOR); user.setUnidadeAtivaCodigo(10L);
        when(usuarioFacade.usuarioAutenticado()).thenReturn(user);
        Unidade uUser = new Unidade(); uUser.setCodigo(10L);
        when(unidadeService.buscarPorCodigo(10L)).thenReturn(uUser);
        
        Processo p = new Processo(); p.setTipo(TipoProcesso.MAPEAMENTO);
        Subprocesso sp = new Subprocesso(); sp.setCodigo(1L); sp.setProcesso(p);
        Unidade uSp = new Unidade(); uSp.setCodigo(10L); sp.setUnidade(uSp);
        sp.setSituacaoForcada(MAPEAMENTO_CADASTRO_DISPONIBILIZADO);
        Mapa mapa = new Mapa(); mapa.setCodigo(100L); sp.setMapa(mapa);
        
        when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(1L)).thenReturn(Optional.of(sp));
        when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(100L)).thenReturn(List.of());
        when(mapaManutencaoService.mapaCompletoSubprocesso(1L)).thenReturn(mapa);
        
        ContextoEdicaoResponse resp = subprocessoService.obterContextoEdicao(1L);
        assertThat(resp.detalhes().permissoes().habilitarAcessoCadastro()).isTrue();
    }

    @Test
    @DisplayName("deve cobrir verificarAcessoMapaHabilitado default case na linha 709")
    void deveCobrirLinha709() {
        Usuario user = new Usuario(); user.setPerfilAtivo(null); user.setUnidadeAtivaCodigo(10L);
        when(usuarioFacade.usuarioAutenticado()).thenReturn(user);
        Unidade uUser = new Unidade(); uUser.setCodigo(10L);
        when(unidadeService.buscarPorCodigo(10L)).thenReturn(uUser);
        
        Processo p = new Processo(); p.setTipo(TipoProcesso.MAPEAMENTO);
        Subprocesso sp = new Subprocesso(); sp.setCodigo(1L); sp.setProcesso(p);
        sp.setSituacaoForcada(MAPEAMENTO_MAPA_CRIADO);
        Mapa mapa = new Mapa(); mapa.setCodigo(100L); sp.setMapa(mapa);
        Unidade uSp = new Unidade(); uSp.setCodigo(10L); sp.setUnidade(uSp);
        
        when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(1L)).thenReturn(Optional.of(sp));
        when(mapaManutencaoService.atividadesMapaCodigoComConhecimentos(100L)).thenReturn(List.of());
        when(mapaManutencaoService.mapaCompletoSubprocesso(1L)).thenReturn(mapa);
        
        ContextoEdicaoResponse resp = subprocessoService.obterContextoEdicao(1L);
        assertThat(resp.detalhes().permissoes().habilitarAcessoMapa()).isFalse();
    }
}
