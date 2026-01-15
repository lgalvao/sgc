package sgc.processo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeMapa;
import sgc.organizacao.model.UnidadeMapaRepo;
import sgc.organizacao.model.UnidadeRepo;
import sgc.processo.erros.ErroProcessoEmSituacaoInvalida;
import sgc.processo.erros.ErroUnidadesNaoDefinidas;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.service.factory.SubprocessoFactory;

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
    private SubprocessoFactory subprocessoFactory;

    @InjectMocks
    private ProcessoInicializador inicializador;

    @Test
    @DisplayName("Iniciar processo falha se situação inválida")
    void iniciarProcessoFalhaSituacaoInvalida() {
        Processo p = new Processo();
        p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        when(processoRepo.findById(1L)).thenReturn(Optional.of(p));

        assertThatThrownBy(() -> inicializador.iniciar(1L, List.of()))
                .isInstanceOf(ErroProcessoEmSituacaoInvalida.class);
    }

    @Test
    @DisplayName("Iniciar REVISAO falha se lista unidades vazia")
    void iniciarRevisaoFalhaListaUnidadesVazia() {
        Processo p = new Processo();
        p.setSituacao(SituacaoProcesso.CRIADO);
        p.setTipo(TipoProcesso.REVISAO);
        when(processoRepo.findById(1L)).thenReturn(Optional.of(p));

        assertThatThrownBy(() -> inicializador.iniciar(1L, Collections.emptyList()))
                .isInstanceOf(ErroUnidadesNaoDefinidas.class);
    }

    @Test
    @DisplayName("Iniciar MAPEAMENTO falha se sem participantes")
    void iniciarMapeamentoFalhaSemParticipantes() {
        Processo p = new Processo();
        p.setSituacao(SituacaoProcesso.CRIADO);
        p.setTipo(TipoProcesso.MAPEAMENTO);
        p.setParticipantes(Collections.emptySet());
        when(processoRepo.findById(1L)).thenReturn(Optional.of(p));

        assertThatThrownBy(() -> inicializador.iniciar(1L, null))
                .isInstanceOf(ErroUnidadesNaoDefinidas.class);
    }

    @Test
    @DisplayName("Iniciar sucesso (Mapeamento)")
    void iniciarSucessoMapeamento() {
        Processo p = new Processo();
        p.setSituacao(SituacaoProcesso.CRIADO);
        p.setTipo(TipoProcesso.MAPEAMENTO);
        Unidade u = new Unidade(); u.setCodigo(1L);
        p.setParticipantes(Set.of(u));

        when(processoRepo.findById(1L)).thenReturn(Optional.of(p));
        when(processoRepo.findUnidadeCodigosBySituacaoAndUnidadeCodigosIn(any(), any())).thenReturn(List.of());

        List<String> erros = inicializador.iniciar(1L, null);

        assertThat(erros).isEmpty();
        verify(subprocessoFactory).criarParaMapeamento(eq(p), any());
        // Verify publishEvent is called with ANY object, as we don't care about the specific timestamp or instance here for this test
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
        when(unidadeMapaRepo.findAllById(any())).thenReturn(List.of()); // Nenhuma unidade tem mapa
        when(unidadeRepo.findSiglasByCodigos(any())).thenReturn(List.of("U1"));

        List<String> erros = inicializador.iniciar(1L, List.of(1L));

        assertThat(erros).isNotEmpty();
        assertThat(erros.get(0)).contains("não possuem mapa vigente");
    }

    @Test
    @DisplayName("Iniciar falha se unidade em processo ativo")
    void iniciarFalhaUnidadeEmProcessoAtivo() {
        Processo p = new Processo();
        p.setSituacao(SituacaoProcesso.CRIADO);
        p.setTipo(TipoProcesso.MAPEAMENTO);
        Unidade u = new Unidade(); u.setCodigo(1L);
        p.setParticipantes(Set.of(u));

        when(processoRepo.findById(1L)).thenReturn(Optional.of(p));
        when(processoRepo.findUnidadeCodigosBySituacaoAndUnidadeCodigosIn(any(), any())).thenReturn(List.of(1L));
        when(unidadeRepo.findSiglasByCodigos(any())).thenReturn(List.of("U1"));

        List<String> erros = inicializador.iniciar(1L, null);

        assertThat(erros).isNotEmpty();
        assertThat(erros.get(0)).contains("já participam de outro processo");
    }

    @Test
    @DisplayName("Iniciar sucesso (Revisão)")
    void iniciarSucessoRevisao() {
        Processo p = new Processo();
        p.setSituacao(SituacaoProcesso.CRIADO);
        p.setTipo(TipoProcesso.REVISAO);

        when(processoRepo.findById(1L)).thenReturn(Optional.of(p));
        
        UnidadeMapa um = new UnidadeMapa();
        um.setUnidadeCodigo(1L);
        when(unidadeMapaRepo.findAllById(anyList())).thenReturn(List.of(um));

        Unidade u = new Unidade(); u.setCodigo(1L);
        when(unidadeRepo.findAllById(anyList())).thenReturn(List.of(u));

        List<String> erros = inicializador.iniciar(1L, List.of(1L));

        assertThat(erros).isEmpty();
        verify(subprocessoFactory).criarParaRevisao(eq(p), any(), any());
    }

    @Test
    @DisplayName("Iniciar sucesso (Diagnóstico)")
    void iniciarSucessoDiagnostico() {
        Processo p = new Processo();
        p.setSituacao(SituacaoProcesso.CRIADO);
        p.setTipo(TipoProcesso.DIAGNOSTICO);
        Unidade u = new Unidade(); u.setCodigo(1L);
        p.setParticipantes(Set.of(u));

        when(processoRepo.findById(1L)).thenReturn(Optional.of(p));

        UnidadeMapa um = new UnidadeMapa();
        um.setUnidadeCodigo(1L);
        when(unidadeMapaRepo.findAllById(anyList())).thenReturn(List.of(um));

        List<String> erros = inicializador.iniciar(1L, null);

        assertThat(erros).isEmpty();
        verify(subprocessoFactory).criarParaDiagnostico(eq(p), any(), any());
    }

    @Test
    @DisplayName("Iniciar falha se unidade participante não for encontrada")
    void iniciarFalhaUnidadeNaoEncontrada() {
        Processo p = new Processo();
        p.setSituacao(SituacaoProcesso.CRIADO);
        p.setTipo(TipoProcesso.REVISAO);

        when(processoRepo.findById(1L)).thenReturn(Optional.of(p));
        UnidadeMapa um = new UnidadeMapa();
        um.setUnidadeCodigo(99L);
        when(unidadeMapaRepo.findAllById(any())).thenReturn(List.of(um));
        when(unidadeRepo.findAllById(any())).thenReturn(List.of()); // Retorna vazio

        assertThatThrownBy(() -> inicializador.iniciar(1L, List.of(99L)))
                .isInstanceOf(sgc.comum.erros.ErroEntidadeDeveriaExistir.class);
    }
}
