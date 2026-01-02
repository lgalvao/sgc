package sgc.processo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import sgc.processo.eventos.EventoProcessoIniciado;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.service.SubprocessoFactory;
import sgc.organizacao.model.Unidade;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessoInicializador")
class ProcessoInicializadorTest {

    @Mock
    private ProcessoRepo processoRepo;
    @Mock
    private sgc.organizacao.model.UnidadeRepo unidadeRepo;
    @Mock
    private sgc.organizacao.model.UnidadeMapaRepo unidadeMapaRepo;
    @Mock
    private SubprocessoFactory subprocessoFactory;
    @Mock
    private ApplicationEventPublisher publicadorEventos;

    @InjectMocks
    private ProcessoInicializador inicializador;

    @Test
    @DisplayName("Deve iniciar processo de mapeamento com sucesso")
    void deveIniciarProcessoMapeamentoComSucesso() {
        // Arrange
        Long codProcesso = 1L;
        Processo processo = new Processo();
        processo.setCodigo(codProcesso);
        processo.setSituacao(SituacaoProcesso.CRIADO);
        processo.setTipo(TipoProcesso.MAPEAMENTO);

        Unidade u1 = new Unidade(); u1.setCodigo(10L);
        processo.setParticipantes(Set.of(u1));

        when(processoRepo.findById(codProcesso)).thenReturn(java.util.Optional.of(processo));

        // Simular que não há unidades em processos ativos
        when(processoRepo.findUnidadeCodigosBySituacaoAndUnidadeCodigosIn(any(), anyList()))
            .thenReturn(List.of());

        // Act
        List<String> erros = inicializador.iniciar(codProcesso, null); // Passa null pois pega do processo

        // Assert
        assertThat(erros).isEmpty();
        verify(processoRepo).save(processo);
        assertThat(processo.getSituacao()).isEqualTo(SituacaoProcesso.EM_ANDAMENTO);
        verify(subprocessoFactory).criarParaMapeamento(eq(processo), any());
        verify(publicadorEventos).publishEvent(any(EventoProcessoIniciado.class));
    }

    @Test
    @DisplayName("Deve falhar ao iniciar processo se não estiver CRIADO")
    void deveFalharAoIniciarSeProcessoNaoCriado() {
        // Arrange
        Long codProcesso = 1L;
        Processo processo = new Processo();
        processo.setCodigo(codProcesso);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO); // Já iniciado

        when(processoRepo.findById(codProcesso)).thenReturn(java.util.Optional.of(processo));

        // Act & Assert
        assertThatThrownBy(() -> inicializador.iniciar(codProcesso, List.of(10L)))
                .isInstanceOf(sgc.processo.erros.ErroProcessoEmSituacaoInvalida.class)
                .hasMessageContaining("Apenas processos na situação 'CRIADO' podem ser iniciados");
    }

    @Test
    @DisplayName("Deve retornar erros se unidades já estiverem em processos ativos")
    void deveRetornarErrosSeUnidadesEmProcessosAtivos() {
        // Arrange
        Long codProcesso = 1L;
        Processo processo = new Processo();
        processo.setCodigo(codProcesso);
        processo.setSituacao(SituacaoProcesso.CRIADO);
        processo.setTipo(TipoProcesso.MAPEAMENTO);

        Unidade u1 = new Unidade(); u1.setCodigo(10L); u1.setSigla("U1");
        Unidade u2 = new Unidade(); u2.setCodigo(20L); u2.setSigla("U2");
        processo.setParticipantes(Set.of(u1, u2));

        when(processoRepo.findById(codProcesso)).thenReturn(java.util.Optional.of(processo));

        // Mocking para findUnidadeCodigosBySituacaoAndUnidadeCodigosIn
        when(processoRepo.findUnidadeCodigosBySituacaoAndUnidadeCodigosIn(eq(SituacaoProcesso.EM_ANDAMENTO), anyList()))
                .thenReturn(List.of(10L));

        // Mocking para findSiglasByCodigos em UnidadeRepo
        when(unidadeRepo.findSiglasByCodigos(List.of(10L))).thenReturn(List.of("U1"));

        // Act
        List<String> erros = inicializador.iniciar(codProcesso, null);

        // Assert
        assertThat(erros).hasSize(1);
        assertThat(erros.get(0)).contains("As seguintes unidades já participam de outro processo ativo: U1");
        verify(processoRepo, never()).save(processo);
    }

    @Test
    @DisplayName("Deve iniciar processo de revisão com sucesso")
    void deveIniciarProcessoRevisaoComSucesso() {
        // Arrange
        Long codProcesso = 1L;
        Processo processo = new Processo();
        processo.setCodigo(codProcesso);
        processo.setSituacao(SituacaoProcesso.CRIADO);
        processo.setTipo(TipoProcesso.REVISAO);

        Unidade u1 = new Unidade(); u1.setCodigo(10L);

        when(processoRepo.findById(codProcesso)).thenReturn(java.util.Optional.of(processo));

        // Mock para unidadeMapaRepo.findAllById
        sgc.organizacao.model.UnidadeMapa um1 = new sgc.organizacao.model.UnidadeMapa();
        um1.setUnidadeCodigo(10L);
        when(unidadeMapaRepo.findAllById(anyList())).thenReturn(List.of(um1));
        
        // Mock para processoRepo.findUnidadeCodigosBySituacaoAndUnidadeCodigosIn
        when(processoRepo.findUnidadeCodigosBySituacaoAndUnidadeCodigosIn(any(), anyList())).thenReturn(List.of());

        // Mock para unidadeRepo.findAllById em vez de findById (para teste da otimização)
        when(unidadeRepo.findAllById(List.of(10L))).thenReturn(List.of(u1));

        // Act
        List<String> erros = inicializador.iniciar(codProcesso, List.of(10L));

        // Assert
        assertThat(erros).isEmpty();
        verify(subprocessoFactory).criarParaRevisao(eq(processo), any(), any());
    }

    @Test
    @DisplayName("Deve retornar erro ao iniciar revisão se unidade sem mapa vigente")
    void deveRetornarErroSeUnidadeSemMapaVigenteNaRevisao() {
        // Arrange
        Long codProcesso = 1L;
        Processo processo = new Processo();
        processo.setCodigo(codProcesso);
        processo.setSituacao(SituacaoProcesso.CRIADO);
        processo.setTipo(TipoProcesso.REVISAO);

        Unidade u1 = new Unidade(); u1.setCodigo(10L); u1.setSigla("U1");

        when(processoRepo.findById(codProcesso)).thenReturn(java.util.Optional.of(processo));

        // Mock para unidadeMapaRepo.findAllById - retorna lista vazia para simular que a unidade 10L não tem mapa
        when(unidadeMapaRepo.findAllById(anyList())).thenReturn(List.of());
        // Mock para unidadeRepo.findSiglasByCodigos
        when(unidadeRepo.findSiglasByCodigos(List.of(10L))).thenReturn(List.of("U1"));

        // Act
        List<String> erros = inicializador.iniciar(codProcesso, List.of(10L));

        // Assert
        assertThat(erros).hasSize(1);
        assertThat(erros.get(0)).contains("As seguintes unidades não possuem mapa vigente");
    }
}
