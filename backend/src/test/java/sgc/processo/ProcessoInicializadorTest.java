package sgc.processo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.fixture.ProcessoFixture;
import sgc.fixture.UnidadeFixture;
import sgc.processo.api.eventos.EventoProcessoIniciado;
import sgc.processo.internal.erros.ErroProcessoEmSituacaoInvalida;
import sgc.processo.internal.erros.ErroUnidadesNaoDefinidas;
import sgc.processo.internal.model.Processo;
import sgc.processo.internal.model.ProcessoRepo;
import sgc.processo.internal.model.SituacaoProcesso;
import sgc.processo.internal.model.TipoProcesso;
import sgc.processo.internal.service.ProcessoInicializador;
import sgc.subprocesso.internal.service.SubprocessoFactory;
import sgc.unidade.internal.model.Unidade;
import sgc.unidade.internal.model.UnidadeMapaRepo;
import sgc.unidade.internal.model.UnidadeRepo;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para ProcessoInicializador.
 * 
 * Esta classe é responsável por inicializar processos de qualquer tipo
 * (MAPEAMENTO, REVISAO, DIAGNOSTICO), criar subprocessos e publicar eventos.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessoInicializador")
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
    private ProcessoInicializador processoInicializador;

    @Nested
    @DisplayName("Validação de Situação")
    class ValidacaoSituacao {
        
        @Test
        @DisplayName("Deve lançar exceção quando processo não encontrado")
        void deveLancarExcecaoQuandoProcessoNaoEncontrado() {
            // Arrange
            when(processoRepo.findById(99L)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThatThrownBy(() -> processoInicializador.iniciar(99L, null))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                .hasMessageContaining("Processo")
                .hasNoCause();
        }
        
        @Test
        @DisplayName("Deve lançar exceção quando processo não está em situação CRIADO")
        void deveLancarExcecaoQuandoProcessoNaoSituacaoCriado() {
            // Arrange
            Processo processo = ProcessoFixture.processoEmAndamento();
            processo.setCodigo(1L);
            when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
            
            // Act & Assert
            assertThatThrownBy(() -> processoInicializador.iniciar(1L, null))
                .isInstanceOf(ErroProcessoEmSituacaoInvalida.class)
                .hasMessageContaining("CRIADO");
        }
    }

    @Nested
    @DisplayName("Inicialização de Mapeamento")
    class InicializacaoMapeamento {
        
        @Test
        @DisplayName("Deve iniciar mapeamento com sucesso quando unidades participantes válidas")
        void deveIniciarMapeamentoQuandoUnidadesParticipantesValidas() {
            // Arrange
            Unidade unidade = UnidadeFixture.unidadeComId(10L);
            Set<Unidade> participantes = new HashSet<>();
            participantes.add(unidade);
            
            Processo processo = ProcessoFixture.processoPadrao();
            processo.setCodigo(1L);
            processo.setTipo(TipoProcesso.MAPEAMENTO);
            processo.setParticipantes(participantes);
            
            when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
            when(processoRepo.findUnidadeCodigosBySituacaoAndUnidadeCodigosIn(any(), anyList()))
                .thenReturn(List.of());  // Nenhuma unidade bloqueada
            when(processoRepo.save(any())).thenReturn(processo);
            
            // Act
            List<String> erros = processoInicializador.iniciar(1L, null);
            
            // Assert
            assertThat(erros).isEmpty();
            assertThat(processo.getSituacao()).isEqualTo(SituacaoProcesso.EM_ANDAMENTO);
            verify(subprocessoFactory).criarParaMapeamento(eq(processo), eq(unidade));
            verify(publicadorEventos).publishEvent(any(EventoProcessoIniciado.class));
        }
        
        @Test
        @DisplayName("Deve lançar exceção quando mapeamento sem unidades participantes")
        void deveLancarExcecaoQuandoMapeamentoSemUnidadesParticipantes() {
            // Arrange
            Processo processo = ProcessoFixture.processoPadrao();
            processo.setCodigo(1L);
            processo.setTipo(TipoProcesso.MAPEAMENTO);
            processo.setParticipantes(new HashSet<>());  // Sem participantes
            
            when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
            
            // Act & Assert
            assertThatThrownBy(() -> processoInicializador.iniciar(1L, null))
                .isInstanceOf(ErroUnidadesNaoDefinidas.class)
                .hasMessageContaining("unidades participantes");
        }
        
        @Test
        @DisplayName("Deve retornar erro quando unidades já estão em processos ativos")
        void deveRetornarErroQuandoUnidadesJaEmProcessosAtivos() {
            // Arrange
            Unidade unidade = UnidadeFixture.unidadeComId(10L);
            unidade.setSigla("SECRETARIA_1");
            Set<Unidade> participantes = new HashSet<>();
            participantes.add(unidade);
            
            Processo processo = ProcessoFixture.processoPadrao();
            processo.setCodigo(1L);
            processo.setTipo(TipoProcesso.MAPEAMENTO);
            processo.setParticipantes(participantes);
            
            when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
            when(processoRepo.findUnidadeCodigosBySituacaoAndUnidadeCodigosIn(any(), anyList()))
                .thenReturn(List.of(10L));  // Unidade bloqueada
            when(unidadeRepo.findSiglasByCodigos(List.of(10L)))
                .thenReturn(List.of("SECRETARIA_1"));
            
            // Act
            List<String> erros = processoInicializador.iniciar(1L, null);
            
            // Assert
            assertThat(erros).hasSize(1);
            assertThat(erros.getFirst()).contains("SECRETARIA_1");
            assertThat(erros.getFirst()).contains("participam de outro processo ativo");
            verify(subprocessoFactory, never()).criarParaMapeamento(any(), any());
        }
    }

    @Nested
    @DisplayName("Inicialização de Revisão")
    class InicializacaoRevisao {
        
        @Test
        @DisplayName("Deve iniciar revisão com sucesso quando unidades com mapa vigente")
        void deveIniciarRevisaoQuandoUnidadesComMapaVigente() {
            // Arrange
            Unidade unidade = UnidadeFixture.unidadeComId(10L);
            
            Processo processo = ProcessoFixture.processoPadrao();
            processo.setCodigo(1L);
            processo.setTipo(TipoProcesso.REVISAO);
            
            when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
            when(unidadeRepo.findAllById(anyList())).thenReturn(List.of(unidade));
            when(unidadeMapaRepo.existsById(10L)).thenReturn(true);  // Tem mapa
            when(processoRepo.findUnidadeCodigosBySituacaoAndUnidadeCodigosIn(any(), anyList()))
                .thenReturn(List.of());  // Nenhuma bloqueada
            when(unidadeRepo.findById(10L)).thenReturn(Optional.of(unidade));
            when(processoRepo.save(any())).thenReturn(processo);
            
            // Act
            List<String> erros = processoInicializador.iniciar(1L, List.of(10L));
            
            // Assert
            assertThat(erros).isEmpty();
            assertThat(processo.getSituacao()).isEqualTo(SituacaoProcesso.EM_ANDAMENTO);
            verify(subprocessoFactory).criarParaRevisao(eq(processo), eq(unidade));
            verify(publicadorEventos).publishEvent(any(EventoProcessoIniciado.class));
        }
        
        @Test
        @DisplayName("Deve lançar exceção quando revisão sem lista de unidades")
        void deveLancarExcecaoQuandoRevisaoSemListaUnidades() {
            // Arrange
            Processo processo = ProcessoFixture.processoPadrao();
            processo.setCodigo(1L);
            processo.setTipo(TipoProcesso.REVISAO);
            
            when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
            
            // Act & Assert
            assertThatThrownBy(() -> processoInicializador.iniciar(1L, null))
                .isInstanceOf(ErroUnidadesNaoDefinidas.class)
                .hasMessageContaining("lista de unidades é obrigatória");
        }
        
        @Test
        @DisplayName("Deve lançar exceção quando revisão com lista de unidades vazia")
        void deveLancarExcecaoQuandoRevisaoComListaUnidadesVazia() {
            // Arrange
            Processo processo = ProcessoFixture.processoPadrao();
            processo.setCodigo(1L);
            processo.setTipo(TipoProcesso.REVISAO);
            
            when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
            
            // Act & Assert
            assertThatThrownBy(() -> processoInicializador.iniciar(1L, List.of()))
                .isInstanceOf(ErroUnidadesNaoDefinidas.class)
                .hasMessageContaining("lista de unidades é obrigatória");
        }
        
        @Test
        @DisplayName("Deve retornar erro quando unidades sem mapa vigente")
        void deveRetornarErroQuandoUnidadesSemMapaVigente() {
            // Arrange
            Unidade unidade = UnidadeFixture.unidadeComId(10L);
            unidade.setSigla("SEC_SEM_MAPA");
            
            Processo processo = ProcessoFixture.processoPadrao();
            processo.setCodigo(1L);
            processo.setTipo(TipoProcesso.REVISAO);
            
            when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
            when(unidadeRepo.findAllById(anyList())).thenReturn(List.of(unidade));
            when(unidadeMapaRepo.existsById(10L)).thenReturn(false);  // Sem mapa
            when(unidadeRepo.findSiglasByCodigos(List.of(10L)))
                .thenReturn(List.of("SEC_SEM_MAPA"));
            
            // Act
            List<String> erros = processoInicializador.iniciar(1L, List.of(10L));
            
            // Assert
            assertThat(erros).hasSize(1);
            assertThat(erros.getFirst()).contains("SEC_SEM_MAPA");
            assertThat(erros.getFirst()).contains("não possuem mapa vigente");
            verify(subprocessoFactory, never()).criarParaRevisao(any(), any());
        }
        
        @Test
        @DisplayName("Deve lançar exceção quando unidade não encontrada durante criação de subprocesso")
        void deveLancarExcecaoQuandoUnidadeNaoEncontradaNaCriacaoSubprocesso() {
            // Arrange
            Unidade unidade = UnidadeFixture.unidadeComId(10L);
            
            Processo processo = ProcessoFixture.processoPadrao();
            processo.setCodigo(1L);
            processo.setTipo(TipoProcesso.REVISAO);
            
            when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
            when(unidadeRepo.findAllById(anyList())).thenReturn(List.of(unidade));
            when(unidadeMapaRepo.existsById(10L)).thenReturn(true);
            when(processoRepo.findUnidadeCodigosBySituacaoAndUnidadeCodigosIn(any(), anyList()))
                .thenReturn(List.of());
            when(unidadeRepo.findById(10L)).thenReturn(Optional.empty());  // Unidade não encontrada
            
            // Act & Assert
            assertThatThrownBy(() -> processoInicializador.iniciar(1L, List.of(10L)))
                .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                .hasMessageContaining("Unidade");
        }
    }

    @Nested
    @DisplayName("Inicialização de Diagnóstico")
    class InicializacaoDiagnostico {
        
        @Test
        @DisplayName("Deve iniciar diagnóstico com sucesso quando unidades com mapa vigente")
        void deveIniciarDiagnosticoQuandoUnidadesComMapaVigente() {
            // Arrange
            Unidade unidade = UnidadeFixture.unidadeComId(10L);
            Set<Unidade> participantes = new HashSet<>();
            participantes.add(unidade);
            
            Processo processo = ProcessoFixture.processoPadrao();
            processo.setCodigo(1L);
            processo.setTipo(TipoProcesso.DIAGNOSTICO);
            processo.setParticipantes(participantes);
            
            when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
            when(unidadeRepo.findAllById(anyList())).thenReturn(List.of(unidade));
            when(unidadeMapaRepo.existsById(10L)).thenReturn(true);  // Tem mapa
            when(processoRepo.findUnidadeCodigosBySituacaoAndUnidadeCodigosIn(any(), anyList()))
                .thenReturn(List.of());
            when(processoRepo.save(any())).thenReturn(processo);
            
            // Act
            List<String> erros = processoInicializador.iniciar(1L, null);
            
            // Assert
            assertThat(erros).isEmpty();
            assertThat(processo.getSituacao()).isEqualTo(SituacaoProcesso.EM_ANDAMENTO);
            verify(subprocessoFactory).criarParaDiagnostico(eq(processo), eq(unidade));
            verify(publicadorEventos).publishEvent(any(EventoProcessoIniciado.class));
        }
        
        @Test
        @DisplayName("Deve retornar erro quando diagnóstico com unidades sem mapa vigente")
        void deveRetornarErroQuandoDiagnosticoComUnidadesSemMapaVigente() {
            // Arrange
            Unidade unidade = UnidadeFixture.unidadeComId(10L);
            unidade.setSigla("SEC_DIAG");
            Set<Unidade> participantes = new HashSet<>();
            participantes.add(unidade);
            
            Processo processo = ProcessoFixture.processoPadrao();
            processo.setCodigo(1L);
            processo.setTipo(TipoProcesso.DIAGNOSTICO);
            processo.setParticipantes(participantes);
            
            when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
            when(unidadeRepo.findAllById(anyList())).thenReturn(List.of(unidade));
            when(unidadeMapaRepo.existsById(10L)).thenReturn(false);  // Sem mapa
            when(unidadeRepo.findSiglasByCodigos(List.of(10L)))
                .thenReturn(List.of("SEC_DIAG"));
            
            // Act
            List<String> erros = processoInicializador.iniciar(1L, null);
            
            // Assert
            assertThat(erros).hasSize(1);
            assertThat(erros.getFirst()).contains("SEC_DIAG");
            assertThat(erros.getFirst()).contains("não possuem mapa vigente");
            verify(subprocessoFactory, never()).criarParaDiagnostico(any(), any());
        }
    }

    @Nested
    @DisplayName("Publicação de Eventos")
    class PublicacaoEventos {
        
        @Test
        @DisplayName("Deve publicar evento com dados corretos ao iniciar processo")
        void devePublicarEventoComDadosCorretosAoIniciarProcesso() {
            // Arrange
            Unidade unidade = UnidadeFixture.unidadeComId(10L);
            Set<Unidade> participantes = new HashSet<>();
            participantes.add(unidade);
            
            Processo processo = ProcessoFixture.processoPadrao();
            processo.setCodigo(100L);
            processo.setTipo(TipoProcesso.MAPEAMENTO);
            processo.setParticipantes(participantes);
            
            when(processoRepo.findById(100L)).thenReturn(Optional.of(processo));
            when(processoRepo.findUnidadeCodigosBySituacaoAndUnidadeCodigosIn(any(), anyList()))
                .thenReturn(List.of());
            when(processoRepo.save(any())).thenReturn(processo);
            
            // Act
            processoInicializador.iniciar(100L, null);
            
            // Assert
            verify(publicadorEventos).publishEvent(any(EventoProcessoIniciado.class));
        }
    }
}
