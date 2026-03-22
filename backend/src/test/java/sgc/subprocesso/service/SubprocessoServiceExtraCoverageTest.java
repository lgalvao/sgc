package sgc.subprocesso.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.mockito.quality.*;
import sgc.comum.model.*;
import sgc.comum.erros.*;
import sgc.mapa.service.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;
import sgc.seguranca.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SubprocessoService Extra Coverage Test")
class SubprocessoServiceExtraCoverageTest {

    @InjectMocks
    private SubprocessoService subprocessoService;

    @Mock
    private SubprocessoRepo subprocessoRepo;

    @Mock
    private MovimentacaoRepo movimentacaoRepo;

    @Mock
    private UnidadeService unidadeService;

    @Mock
    private MapaManutencaoService mapaManutencaoService;

    @Mock
    private CopiaMapaService copiaMapaService;

    @Mock
    private ComumRepo repo;

    @Mock
    private UsuarioFacade usuarioFacade;

    @Mock
    private SgcPermissionEvaluator permissionEvaluator;

    @BeforeEach
    void setUp() {
        subprocessoService.setSubprocessoRepo(subprocessoRepo);
        subprocessoService.setMovimentacaoRepo(movimentacaoRepo);
        subprocessoService.setMapaManutencaoService(mapaManutencaoService);
    }

    @Nested
    @DisplayName("atualizarParaEmAndamento")
    class AtualizarParaEmAndamento {

        @Test
        @DisplayName("deve lançar exceção quando não encontrar subprocesso pelo mapa")
        void deveLancarExcecaoNaoEncontrado() {
            when(subprocessoRepo.findByMapa_Codigo(1L)).thenReturn(Optional.empty());

            assertThrows(NoSuchElementException.class, () -> subprocessoService.atualizarParaEmAndamento(1L));
        }

        @Test
        @DisplayName("deve atualizar revisão para andamento se nao iniciado")
        void atualizarRevisao() {
            Processo p = new Processo();
            p.setTipo(TipoProcesso.REVISAO);

            Subprocesso sp = new Subprocesso();
            sp.setProcesso(p);
            sp.setSituacaoForcada(SituacaoSubprocesso.NAO_INICIADO);

            when(subprocessoRepo.findByMapa_Codigo(1L)).thenReturn(Optional.of(sp));
            when(mapaManutencaoService.atividadesMapaCodigoSemRels(1L)).thenReturn(List.of());

            subprocessoService.atualizarParaEmAndamento(1L);

            verify(subprocessoRepo).save(sp);
            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
        }

        @Test
        @DisplayName("deve atualizar para andamento se tiver atividades e não for revisão")
        void atualizarMapeamentoComAtividades() {
            Processo p = new Processo();
            p.setTipo(TipoProcesso.MAPEAMENTO);

            Subprocesso sp = new Subprocesso();
            sp.setProcesso(p);
            sp.setSituacaoForcada(SituacaoSubprocesso.NAO_INICIADO);

            when(subprocessoRepo.findByMapa_Codigo(1L)).thenReturn(Optional.of(sp));
            when(mapaManutencaoService.atividadesMapaCodigoSemRels(1L)).thenReturn(List.of(new sgc.mapa.model.Atividade()));

            subprocessoService.atualizarParaEmAndamento(1L);

            verify(subprocessoRepo).save(sp);
            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
        }

        @Test
        @DisplayName("deve atualizar para nao iniciado se nao tiver atividades e nao for revisao")
        void atualizarMapeamentoSemAtividades() {
            Processo p = new Processo();
            p.setTipo(TipoProcesso.MAPEAMENTO);

            Subprocesso sp = new Subprocesso();
            sp.setProcesso(p);
            sp.setSituacaoForcada(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);

            when(subprocessoRepo.findByMapa_Codigo(1L)).thenReturn(Optional.of(sp));
            when(mapaManutencaoService.atividadesMapaCodigoSemRels(1L)).thenReturn(List.of());

            subprocessoService.atualizarParaEmAndamento(1L);

            verify(subprocessoRepo).save(sp);
            assertThat(sp.getSituacao()).isEqualTo(SituacaoSubprocesso.NAO_INICIADO);
        }
    }

    @Nested
    @DisplayName("obterUnidadeLocalizacao")
    class ObterUnidadeLocalizacao {
        @Test
        @DisplayName("deve retornar a unidade destino da ultima movimentacao se localizacaoAtual for null")
        void deveRetornarDestinoMovimentacao() {
            Unidade u1 = new Unidade();
            u1.setCodigo(1L);
            u1.setSigla("U1");

            Unidade u2 = new Unidade();
            u2.setCodigo(2L);
            u2.setSigla("U2");

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setUnidade(u1);
            sp.setLocalizacaoAtual(null);

            Movimentacao mov = new Movimentacao();
            mov.setUnidadeDestino(u2);

            when(movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(100L)).thenReturn(List.of(mov));

            Unidade resultado = subprocessoService.obterUnidadeLocalizacao(sp);

            assertThat(resultado).isEqualTo(u2);
        }

        @Test
        @DisplayName("deve retornar unidade do subprocesso se movimentacao nao tiver destino e localizacaoAtual for null")
        void deveRetornarUnidadeSubprocesso() {
            Unidade u1 = new Unidade();
            u1.setCodigo(1L);

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(100L);
            sp.setUnidade(u1);
            sp.setLocalizacaoAtual(null);

            Movimentacao mov = new Movimentacao();
            mov.setUnidadeDestino(null);

            when(movimentacaoRepo.findBySubprocessoCodigoOrderByDataHoraDesc(100L)).thenReturn(List.of(mov));

            Unidade resultado = subprocessoService.obterUnidadeLocalizacao(sp);

            assertThat(resultado).isEqualTo(u1);
        }

        @Test
        @DisplayName("deve retornar unidade do subprocesso se codigo for null")
        void codNull() {
            Unidade u = new Unidade();
            Subprocesso sp = new Subprocesso();
            sp.setUnidade(u);
            sp.setCodigo(null);

            assertThat(subprocessoService.obterUnidadeLocalizacao(sp)).isEqualTo(u);
        }

        @Test
        @DisplayName("deve retornar unidade do subprocesso se movimentacoes vazio")
        void movVazio() {
            Unidade u = new Unidade();
            Subprocesso sp = new Subprocesso();
            sp.setUnidade(u);
            sp.setCodigo(1L);

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
            Subprocesso sp1 = new Subprocesso();
            sp1.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            Subprocesso sp2 = new Subprocesso();
            sp2.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO);

            when(subprocessoRepo.findByProcessoCodigoAndUnidadeCodigoInWithUnidade(1L, List.of(10L, 11L)))
                    .thenReturn(List.of(sp1, sp2));

            List<Subprocesso> res = subprocessoService.listarPorProcessoEUnidadeCodigosESituacoes(1L, List.of(10L, 11L), List.of(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO));

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
            when(usuarioFacade.usuarioAutenticado()).thenReturn(new Usuario());
            when(permissionEvaluator.verificarPermissao(any(), eq(sp), eq(AcaoPermissao.EDITAR_CADASTRO))).thenReturn(false);

            assertThrows(ErroAcessoNegado.class, () -> subprocessoService.importarAtividades(1L, 2L, List.of()));
        }

        @Test
        @DisplayName("deve lancar erro se nao tiver permissao na origem")
        void semPermissaoOrigem() {
            Subprocesso spDest = new Subprocesso();
            spDest.setSituacao(SituacaoSubprocesso.NAO_INICIADO);
            Subprocesso spOrig = new Subprocesso();
            
            when(repo.buscar(Subprocesso.class, 1L)).thenReturn(spDest);
            when(repo.buscar(Subprocesso.class, 2L)).thenReturn(spOrig);
            Usuario u = new Usuario();
            when(usuarioFacade.usuarioAutenticado()).thenReturn(u);
            when(permissionEvaluator.verificarPermissao(u, spDest, AcaoPermissao.EDITAR_CADASTRO)).thenReturn(true);
            when(permissionEvaluator.verificarPermissao(u, spOrig, AcaoPermissao.CONSULTAR_PARA_IMPORTACAO)).thenReturn(false);

            assertThrows(ErroAcessoNegado.class, () -> subprocessoService.importarAtividades(1L, 2L, List.of()));
        }
    }

    @Nested
    @DisplayName("listarAtividadesParaImportacao")
    class ListarAtividadesParaImportacao {
        @Test
        @DisplayName("deve lancar erro se processo nao finalizado")
        void processoNaoFinalizado() {
            Subprocesso sp = new Subprocesso();
            Processo p = new Processo();
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
            sp.setProcesso(p);
            when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(1L)).thenReturn(Optional.of(sp));

            assertThrows(ErroValidacao.class, () -> subprocessoService.listarAtividadesParaImportacao(1L));
        }
    }
}
