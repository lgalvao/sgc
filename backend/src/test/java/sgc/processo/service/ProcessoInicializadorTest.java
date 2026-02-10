package sgc.processo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeMapa;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UnidadeMapaRepo;
import sgc.organizacao.model.UnidadeRepo;
import sgc.processo.erros.ErroProcessoEmSituacaoInvalida;
import sgc.processo.erros.ErroUnidadesNaoDefinidas;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.service.SubprocessoFacade;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    private UnidadeMapaRepo unidadeMapaRepo;
    @Mock
    private ApplicationEventPublisher publicadorEventos;
    @Mock
    private SubprocessoFacade subprocessoFacade;
    @Mock
    private ProcessoValidador processoValidador;

    @InjectMocks
    private ProcessoInicializador inicializador;

    @Test
    @DisplayName("Iniciar processo falha se situação inválida")
    void iniciarProcessoFalhaSituacaoInvalida() {
        Processo p = new Processo();
        p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        when(processoRepo.findById(1L)).thenReturn(Optional.of(p));

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
        when(processoRepo.findById(1L)).thenReturn(Optional.of(p));

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
        p.setParticipantes(new java.util.ArrayList<>());
        when(processoRepo.findById(1L)).thenReturn(Optional.of(p));

        Usuario usuario = new Usuario();
        assertThatThrownBy(() -> inicializador.iniciar(1L, null, usuario))
                .isInstanceOf(ErroUnidadesNaoDefinidas.class);
    }

    @Test
    @DisplayName("Iniciar sucesso (Mapeamento)")
    void iniciarSucessoMapeamento() {
        Processo p = new Processo();
        p.setSituacao(SituacaoProcesso.CRIADO);
        p.setTipo(TipoProcesso.MAPEAMENTO);
        Unidade u = new Unidade();
        u.setCodigo(1L);
        p.adicionarParticipantes(Set.of(u));

        when(processoRepo.findById(1L)).thenReturn(Optional.of(p));
        when(processoRepo.findUnidadeCodigosBySituacaoAndUnidadeCodigosIn(any(), any())).thenReturn(List.of());

        Unidade sedoc = new Unidade();
        sedoc.setSigla("SEDOC");
        when(unidadeRepo.findBySigla("SEDOC")).thenReturn(Optional.of(sedoc));

        Usuario usuario = new Usuario();
        List<String> erros = inicializador.iniciar(1L, null, usuario);

        assertThat(erros).isEmpty();
        verify(subprocessoFacade).criarParaMapeamento(eq(p), any(), eq(sedoc), eq(usuario));
        verify(publicadorEventos).publishEvent(any(Object.class));
        assertThat(p.getSituacao()).isEqualTo(SituacaoProcesso.EM_ANDAMENTO);
    }

    @Test
    @DisplayName("Iniciar falha se unidade sem mapa em Revisão")
    void iniciarFalhaUnidadeSemMapaRevisao() {
        Processo p = new Processo();
        p.setSituacao(SituacaoProcesso.CRIADO);
        p.setTipo(TipoProcesso.REVISAO);

        when(processoRepo.findById(1L)).thenReturn(Optional.of(p));
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
        Unidade u = new Unidade();
        u.setCodigo(1L);
        p.adicionarParticipantes(Set.of(u));

        when(processoRepo.findById(1L)).thenReturn(Optional.of(p));
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
        p.setSituacao(SituacaoProcesso.CRIADO);
        p.setTipo(TipoProcesso.REVISAO);

        when(processoRepo.findById(1L)).thenReturn(Optional.of(p));
        when(processoValidador.getMensagemErroUnidadesSemMapa(any())).thenReturn(Optional.empty());

        UnidadeMapa um = new UnidadeMapa();
        um.setUnidadeCodigo(1L);
        when(unidadeMapaRepo.findAllById(anyList())).thenReturn(List.of(um));

        Unidade u = new Unidade();
        u.setCodigo(1L);
        when(unidadeRepo.findAllById(anyList())).thenReturn(List.of(u));

        Unidade sedoc = new Unidade();
        sedoc.setSigla("SEDOC");
        when(unidadeRepo.findBySigla("SEDOC")).thenReturn(Optional.of(sedoc));

        Usuario usuario = new Usuario();
        List<String> erros = inicializador.iniciar(1L, List.of(1L), usuario);

        assertThat(erros).isEmpty();
        verify(subprocessoFacade).criarParaRevisao(eq(p), any(), any(), eq(sedoc), eq(usuario));
    }

    @Test
    @DisplayName("Iniciar sucesso (Diagnóstico)")
    void iniciarSucessoDiagnostico() {
        Processo p = new Processo();
        p.setSituacao(SituacaoProcesso.CRIADO);
        p.setTipo(TipoProcesso.DIAGNOSTICO);
        Unidade u = new Unidade();
        u.setCodigo(1L);
        p.adicionarParticipantes(Set.of(u));

        when(processoRepo.findById(1L)).thenReturn(Optional.of(p));
        when(processoValidador.getMensagemErroUnidadesSemMapa(any())).thenReturn(Optional.empty());

        UnidadeMapa um = new UnidadeMapa();
        um.setUnidadeCodigo(1L);
        when(unidadeMapaRepo.findAllById(anyList())).thenReturn(List.of(um));
        when(unidadeRepo.findAllById(anyList())).thenReturn(List.of(u));

        Unidade sedoc = new Unidade();
        sedoc.setSigla("SEDOC");
        when(unidadeRepo.findBySigla("SEDOC")).thenReturn(Optional.of(sedoc));

        Usuario usuario = new Usuario();
        List<String> erros = inicializador.iniciar(1L, null, usuario);

        assertThat(erros).isEmpty();
        verify(subprocessoFacade).criarParaDiagnostico(eq(p), any(), any(), eq(sedoc), eq(usuario));
    }

    @Test
    @DisplayName("Iniciar falha se unidade participante não for encontrada")
    void iniciarFalhaUnidadeNaoEncontrada() {
        Processo p = new Processo();
        p.setSituacao(SituacaoProcesso.CRIADO);
        p.setTipo(TipoProcesso.REVISAO);

        when(processoRepo.findById(1L)).thenReturn(Optional.of(p));
        when(processoValidador.getMensagemErroUnidadesSemMapa(any())).thenReturn(Optional.empty());
        UnidadeMapa um = new UnidadeMapa();
        um.setUnidadeCodigo(99L);
        when(unidadeMapaRepo.findAllById(any())).thenReturn(List.of(um));
        when(unidadeRepo.findAllById(any())).thenReturn(List.of()); // Retorna vazio

        Unidade sedoc = new Unidade();
        sedoc.setSigla("SEDOC");
        when(unidadeRepo.findBySigla("SEDOC")).thenReturn(Optional.of(sedoc));

        List<Long> unidades = List.of(99L);
        Usuario usuario = new Usuario();
        assertThatThrownBy(() -> inicializador.iniciar(1L, unidades, usuario))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class);
    }
}
