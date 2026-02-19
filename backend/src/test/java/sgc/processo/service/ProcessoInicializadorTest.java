package sgc.processo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.comum.repo.ComumRepo;
import sgc.organizacao.model.*;
import sgc.processo.erros.ErroProcessoEmSituacaoInvalida;
import sgc.processo.erros.ErroUnidadesNaoDefinidas;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.service.SubprocessoService;
import sgc.testutils.UnidadeTestBuilder;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("ProcessoInicializador Test")
class ProcessoInicializadorTest {

    @Mock
    private ProcessoRepo processoRepo;
    @Mock
    private UnidadeRepo unidadeRepo;
    @Mock
    private ComumRepo repo;
    @Mock
    private UnidadeMapaRepo unidadeMapaRepo;
    @Mock
    private ProcessoNotificacaoService notificacaoService;
    @Mock
    private SubprocessoService subprocessoService;
    @Mock
    private ProcessoValidador processoValidador;

    @InjectMocks
    private ProcessoInicializador inicializador;

    private Unidade criarUnidade(long codigo, String sigla) {
        return UnidadeTestBuilder.umaDe()
                .comCodigo(String.valueOf(codigo))
                .comSigla(sigla)
                .build();
    }

    @Test
    @DisplayName("Iniciar processo falha se situação inválida")
    void iniciarProcessoFalhaSituacaoInvalida() {
        Processo p = new Processo();
        p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        when(repo.buscar(Processo.class, 1L)).thenReturn(p);

        List<Long> unidades = List.of();
        Usuario usuario = new Usuario();
        assertThatThrownBy(() -> inicializador.iniciar(1L, unidades, usuario))
                .isInstanceOf(ErroProcessoEmSituacaoInvalida.class);
    }

    @Test
    @DisplayName("Iniciar REVISAO falha se lista unidades vazia")
    void iniciarRevisaoFalhaListaUnidadesVazia() {
        Processo p = new Processo();
        p.setSituacao(SituacaoProcesso.CRIADO);
        p.setTipo(TipoProcesso.REVISAO);
        when(repo.buscar(Processo.class, 1L)).thenReturn(p);

        List<Long> unidades = Collections.emptyList();
        Usuario usuario = new Usuario();
        assertThatThrownBy(() -> inicializador.iniciar(1L, unidades, usuario))
                .isInstanceOf(ErroUnidadesNaoDefinidas.class);
    }

    @Test
    @DisplayName("Iniciar MAPEAMENTO falha se sem participantes")
    void iniciarMapeamentoFalhaSemParticipantes() {
        Processo p = new Processo();
        p.setSituacao(SituacaoProcesso.CRIADO);
        p.setTipo(TipoProcesso.MAPEAMENTO);
        p.setParticipantes(new ArrayList<>());
        when(repo.buscar(Processo.class, 1L)).thenReturn(p);

        Usuario usuario = new Usuario();
        assertThatThrownBy(() -> inicializador.iniciar(1L, null, usuario))
                .isInstanceOf(ErroUnidadesNaoDefinidas.class);
    }

    @Test
    @DisplayName("Iniciar sucesso (Mapeamento)")
    void iniciarSucessoMapeamento() {
        Processo p = new Processo();
        p.setCodigo(1L);
        p.setSituacao(SituacaoProcesso.CRIADO);
        p.setTipo(TipoProcesso.MAPEAMENTO);
        Unidade u = criarUnidade(1L, "U1");
        p.adicionarParticipantes(Set.of(u));

        when(repo.buscar(Processo.class, 1L)).thenReturn(p);
        when(processoRepo.findUnidadeCodigosBySituacaoAndUnidadeCodigosIn(any(), any())).thenReturn(List.of());

        Unidade admin = criarUnidade(999L, "ADMIN");
        when(repo.buscarPorSigla(Unidade.class, "ADMIN")).thenReturn(admin);

        Usuario usuario = new Usuario();
        List<String> erros = inicializador.iniciar(1L, null, usuario);

        assertThat(erros).isEmpty();
        verify(subprocessoService).criarParaMapeamento(eq(p), any(), eq(admin), eq(usuario));
        verify(notificacaoService).notificarInicioProcesso(eq(1L), anyList());
        assertThat(p.getSituacao()).isEqualTo(SituacaoProcesso.EM_ANDAMENTO);
    }

    @Test
    @DisplayName("Iniciar falha se unidade sem mapa em Revisão")
    void iniciarFalhaUnidadeSemMapaRevisao() {
        Processo p = new Processo();
        p.setSituacao(SituacaoProcesso.CRIADO);
        p.setTipo(TipoProcesso.REVISAO);

        when(repo.buscar(Processo.class, 1L)).thenReturn(p);
        when(processoValidador.getMensagemErroUnidadesSemMapa(any())).thenReturn(Optional.of("As seguintes unidades não possuem mapa vigente: U1"));
        when(processoRepo.findUnidadeCodigosBySituacaoAndUnidadeCodigosIn(any(), any())).thenReturn(List.of());

        Usuario usuario = new Usuario();
        List<String> erros = inicializador.iniciar(1L, List.of(1L), usuario);

        assertThat(erros).isNotEmpty();
        assertThat(erros.getFirst()).contains("não possuem mapa vigente");
    }

    @Test
    @DisplayName("Iniciar falha se unidade em processo ativo")
    void iniciarFalhaUnidadeEmProcessoAtivo() {
        Processo p = new Processo();
        p.setSituacao(SituacaoProcesso.CRIADO);
        p.setTipo(TipoProcesso.MAPEAMENTO);
        Unidade u = criarUnidade(1L, "U1");
        p.adicionarParticipantes(Set.of(u));

        when(repo.buscar(Processo.class, 1L)).thenReturn(p);
        when(processoRepo.findUnidadeCodigosBySituacaoAndUnidadeCodigosIn(any(), any())).thenReturn(List.of(1L));
        when(unidadeRepo.findSiglasByCodigos(any())).thenReturn(List.of("U1"));

        Usuario usuario = new Usuario();
        List<String> erros = inicializador.iniciar(1L, null, usuario);

        assertThat(erros).isNotEmpty();
        assertThat(erros.getFirst()).contains("já participam de outro processo");
    }

    @Test
    @DisplayName("Iniciar sucesso (Revisão)")
    void iniciarSucessoRevisao() {
        Processo p = new Processo();
        p.setCodigo(1L);
        p.setSituacao(SituacaoProcesso.CRIADO);
        p.setTipo(TipoProcesso.REVISAO);

        when(repo.buscar(Processo.class, 1L)).thenReturn(p);
        when(processoValidador.getMensagemErroUnidadesSemMapa(any())).thenReturn(Optional.empty());

        UnidadeMapa um = new UnidadeMapa();
        um.setUnidadeCodigo(1L);
        when(unidadeMapaRepo.findAllById(anyList())).thenReturn(List.of(um));

        Unidade u = criarUnidade(1L, "U1");
        when(repo.buscar(Unidade.class, 1L)).thenReturn(u);

        Unidade admin = criarUnidade(999L, "ADMIN");
        when(repo.buscarPorSigla(Unidade.class, "ADMIN")).thenReturn(admin);

        Usuario usuario = new Usuario();
        List<String> erros = inicializador.iniciar(1L, List.of(1L), usuario);

        assertThat(erros).isEmpty();
        verify(unidadeMapaRepo).findAllById(anyList());
        verify(subprocessoService).criarParaRevisao(p, u, um, admin, usuario);
        verify(notificacaoService).notificarInicioProcesso(eq(1L), anyList());
    }

    @Test
    @DisplayName("Iniciar sucesso (Diagnóstico)")
    void iniciarSucessoDiagnostico() {
        Processo p = new Processo();
        p.setCodigo(1L);
        p.setSituacao(SituacaoProcesso.CRIADO);
        p.setTipo(TipoProcesso.DIAGNOSTICO);
        Unidade u = criarUnidade(1L, "U1");
        p.adicionarParticipantes(Set.of(u));

        when(repo.buscar(Processo.class, 1L)).thenReturn(p);
        when(processoValidador.getMensagemErroUnidadesSemMapa(any())).thenReturn(Optional.empty());

        UnidadeMapa um = new UnidadeMapa();
        um.setUnidadeCodigo(1L);
        when(unidadeMapaRepo.findAllById(anyList())).thenReturn(List.of(um));
        when(unidadeRepo.findAllById(anyList())).thenReturn(List.of(u));

        Unidade admin = criarUnidade(999L, "ADMIN");
        when(repo.buscarPorSigla(Unidade.class, "ADMIN")).thenReturn(admin);

        Usuario usuario = new Usuario();
        List<String> erros = inicializador.iniciar(1L, null, usuario);

        assertThat(erros).isEmpty();
        verify(subprocessoService).criarParaDiagnostico(eq(p), any(), any(), eq(admin), eq(usuario));
        verify(notificacaoService).notificarInicioProcesso(eq(1L), anyList());
    }
}
