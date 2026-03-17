package sgc.subprocesso.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sgc.mapa.service.CopiaMapaService;
import sgc.mapa.service.MapaManutencaoService;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.service.UnidadeService;
import sgc.processo.model.Processo;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.AtualizarSubprocessoRequest;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
    }

    @Nested
    @DisplayName("processarAlteracoes (via atualizarEntidade)")
    class ProcessarAlteracoes {
        @Test
        @DisplayName("deve atualizar datas limite e fim de etapa sem codMapa")
        void deveAtualizarDatasSemCodMapa() {
            LocalDateTime limite1 = LocalDateTime.now().plusDays(1);
            LocalDateTime fim1 = LocalDateTime.now().plusDays(2);
            LocalDateTime limite2 = LocalDateTime.now().plusDays(3);
            LocalDateTime fim2 = LocalDateTime.now().plusDays(4);

            AtualizarSubprocessoRequest req = AtualizarSubprocessoRequest.builder()
                    .dataLimiteEtapa1(limite1)
                    .dataFimEtapa1(fim1)
                    .dataLimiteEtapa2(limite2)
                    .dataFimEtapa2(fim2)
                    .build();

            Subprocesso sp = new Subprocesso();
            sp.setCodigo(1L);

            when(subprocessoRepo.buscarPorCodigoComMapaEAtividades(1L)).thenReturn(Optional.of(sp));
            when(subprocessoRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Subprocesso atualizado = subprocessoService.atualizarEntidade(1L, req);

            assertThat(atualizado.getDataLimiteEtapa1()).isEqualTo(limite1);
            assertThat(atualizado.getDataFimEtapa1()).isEqualTo(fim1);
            assertThat(atualizado.getDataLimiteEtapa2()).isEqualTo(limite2);
            assertThat(atualizado.getDataFimEtapa2()).isEqualTo(fim2);
        }
    }
}
