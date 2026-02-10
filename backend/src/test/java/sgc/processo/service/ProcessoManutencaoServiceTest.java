package sgc.processo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.organizacao.UnidadeFacade;
import sgc.organizacao.model.Unidade;
import sgc.processo.dto.AtualizarProcessoRequest;
import sgc.processo.dto.CriarProcessoRequest;
import sgc.processo.erros.ErroProcesso;
import sgc.processo.erros.ErroProcessoEmSituacaoInvalida;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.testutils.UnidadeTestBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("ProcessoManutencaoService")
class ProcessoManutencaoServiceTest {

    @Mock
    private ProcessoRepo processoRepo;

    @Mock
    private UnidadeFacade unidadeService;

    @Mock
    private ProcessoValidador processoValidador;

    @Mock
    private ProcessoConsultaService processoConsultaService;

    @InjectMocks
    private ProcessoManutencaoService service;

    @Nested
    @DisplayName("Criação de Processo")
    class CriacaoTests {

        @Test
        @DisplayName("Deve criar processo do tipo REVISAO com sucesso")
        void deveCriarProcessoRevisaoComSucesso() {
            // Given
            LocalDateTime dataLimite = LocalDateTime.now().plusDays(30);
            CriarProcessoRequest request = CriarProcessoRequest.builder()
                    .descricao("Processo de Revisão")
                    .tipo(TipoProcesso.REVISAO)
                    .dataLimiteEtapa1(dataLimite)
                    .unidades(List.of(1L, 2L))
                    .build();

            Unidade unidade1 = UnidadeTestBuilder.umaDe().comCodigo("1").build();
            Unidade unidade2 = UnidadeTestBuilder.umaDe().comCodigo("2").build();

            when(unidadeService.buscarEntidadePorId(1L)).thenReturn(unidade1);
            when(unidadeService.buscarEntidadePorId(2L)).thenReturn(unidade2);
            when(processoValidador.getMensagemErroUnidadesSemMapa(anyList())).thenReturn(Optional.empty());
            
            Processo processoSalvo = new Processo();
            processoSalvo.setCodigo(100L);
            processoSalvo.setDescricao("Processo de Revisão");
            processoSalvo.setTipo(TipoProcesso.REVISAO);
            when(processoRepo.saveAndFlush(any(Processo.class))).thenReturn(processoSalvo);

            // When
            Processo resultado = service.criar(request);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getCodigo()).isEqualTo(100L);
            verify(processoValidador).getMensagemErroUnidadesSemMapa(List.of(1L, 2L));
            verify(processoRepo).saveAndFlush(any(Processo.class));

            ArgumentCaptor<Processo> captor = ArgumentCaptor.forClass(Processo.class);
            verify(processoRepo).saveAndFlush(captor.capture());
            Processo processoCapturado = captor.getValue();
            assertThat(processoCapturado.getDescricao()).isEqualTo("Processo de Revisão");
            assertThat(processoCapturado.getTipo()).isEqualTo(TipoProcesso.REVISAO);
            assertThat(processoCapturado.getSituacao()).isEqualTo(SituacaoProcesso.CRIADO);
            assertThat(processoCapturado.getParticipantes()).hasSize(2);
        }

        @Test
        @DisplayName("Deve criar processo do tipo DIAGNOSTICO com sucesso")
        void deveCriarProcessoDiagnosticoComSucesso() {
            // Given
            LocalDateTime dataLimite = LocalDateTime.now().plusDays(30);
            CriarProcessoRequest request = CriarProcessoRequest.builder()
                    .descricao("Processo de Diagnóstico")
                    .tipo(TipoProcesso.DIAGNOSTICO)
                    .dataLimiteEtapa1(dataLimite)
                    .unidades(List.of(1L))
                    .build();

            Unidade unidade1 = UnidadeTestBuilder.umaDe().comCodigo("1").build();

            when(unidadeService.buscarEntidadePorId(1L)).thenReturn(unidade1);
            when(processoValidador.getMensagemErroUnidadesSemMapa(anyList())).thenReturn(Optional.empty());
            
            Processo processoSalvo = new Processo();
            processoSalvo.setCodigo(101L);
            processoSalvo.setDescricao("Processo de Diagnóstico");
            processoSalvo.setTipo(TipoProcesso.DIAGNOSTICO);
            when(processoRepo.saveAndFlush(any(Processo.class))).thenReturn(processoSalvo);

            // When
            Processo resultado = service.criar(request);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getCodigo()).isEqualTo(101L);
            verify(processoValidador).getMensagemErroUnidadesSemMapa(List.of(1L));
            verify(processoRepo).saveAndFlush(any(Processo.class));

            ArgumentCaptor<Processo> captor = ArgumentCaptor.forClass(Processo.class);
            verify(processoRepo).saveAndFlush(captor.capture());
            Processo processoCapturado = captor.getValue();
            assertThat(processoCapturado.getTipo()).isEqualTo(TipoProcesso.DIAGNOSTICO);
        }

        @Test
        @DisplayName("Deve criar processo do tipo MAPEAMENTO sem validação de mapa")
        void deveCriarProcessoMapeamentoSemValidacao() {
            // Given
            LocalDateTime dataLimite = LocalDateTime.now().plusDays(30);
            CriarProcessoRequest request = CriarProcessoRequest.builder()
                    .descricao("Processo de Mapeamento")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(dataLimite)
                    .unidades(List.of(1L))
                    .build();

            Unidade unidade1 = UnidadeTestBuilder.umaDe().comCodigo("1").build();

            when(unidadeService.buscarEntidadePorId(1L)).thenReturn(unidade1);
            
            Processo processoSalvo = new Processo();
            processoSalvo.setCodigo(102L);
            processoSalvo.setDescricao("Processo de Mapeamento");
            processoSalvo.setTipo(TipoProcesso.MAPEAMENTO);
            when(processoRepo.saveAndFlush(any(Processo.class))).thenReturn(processoSalvo);

            // When
            Processo resultado = service.criar(request);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getCodigo()).isEqualTo(102L);
            verify(processoValidador, never()).getMensagemErroUnidadesSemMapa(anyList());
            verify(processoRepo).saveAndFlush(any(Processo.class));
        }

        @Test
        @DisplayName("Deve lançar erro ao criar processo REVISAO com unidades sem mapa")
        void deveLancarErroAoCriarRevisaoComUnidadesSemMapa() {
            // Given
            LocalDateTime dataLimite = LocalDateTime.now().plusDays(30);
            CriarProcessoRequest request = CriarProcessoRequest.builder()
                    .descricao("Processo de Revisão")
                    .tipo(TipoProcesso.REVISAO)
                    .dataLimiteEtapa1(dataLimite)
                    .unidades(List.of(1L))
                    .build();

            Unidade unidade1 = UnidadeTestBuilder.umaDe().comCodigo("1").build();

            when(unidadeService.buscarEntidadePorId(1L)).thenReturn(unidade1);
            when(processoValidador.getMensagemErroUnidadesSemMapa(anyList()))
                    .thenReturn(Optional.of("Unidades sem mapa: Unidade 1"));

            // When/Then
            assertThatThrownBy(() -> service.criar(request))
                    .isInstanceOf(ErroProcesso.class)
                    .hasMessage("Unidades sem mapa: Unidade 1");

            verify(processoValidador).getMensagemErroUnidadesSemMapa(List.of(1L));
            verify(processoRepo, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("Deve lançar erro ao criar processo DIAGNOSTICO com unidades sem mapa")
        void deveLancarErroAoCriarDiagnosticoComUnidadesSemMapa() {
            // Given
            LocalDateTime dataLimite = LocalDateTime.now().plusDays(30);
            CriarProcessoRequest request = CriarProcessoRequest.builder()
                    .descricao("Processo de Diagnóstico")
                    .tipo(TipoProcesso.DIAGNOSTICO)
                    .dataLimiteEtapa1(dataLimite)
                    .unidades(List.of(1L, 2L))
                    .build();

            Unidade unidade1 = UnidadeTestBuilder.umaDe().comCodigo("1").build();
            Unidade unidade2 = UnidadeTestBuilder.umaDe().comCodigo("2").build();

            when(unidadeService.buscarEntidadePorId(1L)).thenReturn(unidade1);
            when(unidadeService.buscarEntidadePorId(2L)).thenReturn(unidade2);
            when(processoValidador.getMensagemErroUnidadesSemMapa(anyList()))
                    .thenReturn(Optional.of("Unidades sem mapa: Unidade 1, Unidade 2"));

            // When/Then
            assertThatThrownBy(() -> service.criar(request))
                    .isInstanceOf(ErroProcesso.class)
                    .hasMessage("Unidades sem mapa: Unidade 1, Unidade 2");

            verify(processoValidador).getMensagemErroUnidadesSemMapa(List.of(1L, 2L));
            verify(processoRepo, never()).saveAndFlush(any());
        }
    }

    @Nested
    @DisplayName("Atualização de Processo")
    class AtualizacaoTests {

        @Test
        @DisplayName("Deve atualizar processo na situação CRIADO com sucesso")
        void deveAtualizarProcessoCriadoComSucesso() {
            // Given
            Long codigoProcesso = 100L;
            LocalDateTime dataLimite = LocalDateTime.now().plusDays(30);
            
            AtualizarProcessoRequest request = AtualizarProcessoRequest.builder()
                    .codigo(codigoProcesso)
                    .descricao("Descrição Atualizada")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(dataLimite)
                    .unidades(List.of(1L, 2L))
                    .build();

            Processo processo = new Processo();
            processo.setCodigo(codigoProcesso);
            processo.setSituacao(SituacaoProcesso.CRIADO);
            processo.setDescricao("Descrição Original");
            processo.setTipo(TipoProcesso.REVISAO);

            Unidade unidade1 = UnidadeTestBuilder.umaDe().comCodigo("1").build();
            Unidade unidade2 = UnidadeTestBuilder.umaDe().comCodigo("2").build();

            when(processoConsultaService.buscarPorId(codigoProcesso)).thenReturn(processo);
            when(unidadeService.buscarEntidadePorId(1L)).thenReturn(unidade1);
            when(unidadeService.buscarEntidadePorId(2L)).thenReturn(unidade2);
            
            Processo processoAtualizado = new Processo();
            processoAtualizado.setCodigo(codigoProcesso);
            processoAtualizado.setDescricao("Descrição Atualizada");
            when(processoRepo.saveAndFlush(any(Processo.class))).thenReturn(processoAtualizado);

            // When
            Processo resultado = service.atualizar(codigoProcesso, request);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getCodigo()).isEqualTo(codigoProcesso);
            verify(processoConsultaService).buscarPorId(codigoProcesso);
            verify(processoRepo).saveAndFlush(processo);
            assertThat(processo.getDescricao()).isEqualTo("Descrição Atualizada");
            assertThat(processo.getTipo()).isEqualTo(TipoProcesso.MAPEAMENTO);
            assertThat(processo.getDataLimite()).isEqualTo(dataLimite);
        }

        @Test
        @DisplayName("Deve lançar erro ao atualizar processo na situação EM_ANDAMENTO")
        void deveLancarErroAoAtualizarProcessoEmAndamento() {
            // Given
            Long codigoProcesso = 100L;
            LocalDateTime dataLimite = LocalDateTime.now().plusDays(30);
            
            AtualizarProcessoRequest request = AtualizarProcessoRequest.builder()
                    .codigo(codigoProcesso)
                    .descricao("Descrição Atualizada")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(dataLimite)
                    .unidades(List.of(1L))
                    .build();

            Processo processo = new Processo();
            processo.setCodigo(codigoProcesso);
            processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

            when(processoConsultaService.buscarPorId(codigoProcesso)).thenReturn(processo);

            // When/Then
            assertThatThrownBy(() -> service.atualizar(codigoProcesso, request))
                    .isInstanceOf(ErroProcessoEmSituacaoInvalida.class)
                    .hasMessage("Apenas processos na situação 'CRIADO' podem ser editados.");

            verify(processoConsultaService).buscarPorId(codigoProcesso);
            verify(processoRepo, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("Deve lançar erro ao atualizar processo na situação FINALIZADO")
        void deveLancarErroAoAtualizarProcessoFinalizado() {
            // Given
            Long codigoProcesso = 100L;
            LocalDateTime dataLimite = LocalDateTime.now().plusDays(30);
            
            AtualizarProcessoRequest request = AtualizarProcessoRequest.builder()
                    .codigo(codigoProcesso)
                    .descricao("Descrição Atualizada")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(dataLimite)
                    .unidades(List.of(1L))
                    .build();

            Processo processo = new Processo();
            processo.setCodigo(codigoProcesso);
            processo.setSituacao(SituacaoProcesso.FINALIZADO);

            when(processoConsultaService.buscarPorId(codigoProcesso)).thenReturn(processo);

            // When/Then
            assertThatThrownBy(() -> service.atualizar(codigoProcesso, request))
                    .isInstanceOf(ErroProcessoEmSituacaoInvalida.class)
                    .hasMessage("Apenas processos na situação 'CRIADO' podem ser editados.");

            verify(processoConsultaService).buscarPorId(codigoProcesso);
            verify(processoRepo, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("Deve atualizar processo para REVISAO com validação de mapa")
        void deveAtualizarProcessoParaRevisaoComValidacao() {
            // Given
            Long codigoProcesso = 100L;
            LocalDateTime dataLimite = LocalDateTime.now().plusDays(30);
            
            AtualizarProcessoRequest request = AtualizarProcessoRequest.builder()
                    .codigo(codigoProcesso)
                    .descricao("Processo de Revisão")
                    .tipo(TipoProcesso.REVISAO)
                    .dataLimiteEtapa1(dataLimite)
                    .unidades(List.of(1L))
                    .build();

            Processo processo = new Processo();
            processo.setCodigo(codigoProcesso);
            processo.setSituacao(SituacaoProcesso.CRIADO);

            Unidade unidade1 = UnidadeTestBuilder.umaDe().comCodigo("1").build();

            when(processoConsultaService.buscarPorId(codigoProcesso)).thenReturn(processo);
            when(processoValidador.getMensagemErroUnidadesSemMapa(anyList())).thenReturn(Optional.empty());
            when(unidadeService.buscarEntidadePorId(1L)).thenReturn(unidade1);
            when(processoRepo.saveAndFlush(any(Processo.class))).thenReturn(processo);

            // When
            Processo resultado = service.atualizar(codigoProcesso, request);

            // Then
            assertThat(resultado).isNotNull();
            verify(processoValidador).getMensagemErroUnidadesSemMapa(List.of(1L));
            verify(processoRepo).saveAndFlush(processo);
        }

        @Test
        @DisplayName("Deve atualizar processo para DIAGNOSTICO com validação de mapa")
        void deveAtualizarProcessoParaDiagnosticoComValidacao() {
            // Given
            Long codigoProcesso = 100L;
            LocalDateTime dataLimite = LocalDateTime.now().plusDays(30);
            
            AtualizarProcessoRequest request = AtualizarProcessoRequest.builder()
                    .codigo(codigoProcesso)
                    .descricao("Processo de Diagnóstico")
                    .tipo(TipoProcesso.DIAGNOSTICO)
                    .dataLimiteEtapa1(dataLimite)
                    .unidades(List.of(1L, 2L))
                    .build();

            Processo processo = new Processo();
            processo.setCodigo(codigoProcesso);
            processo.setSituacao(SituacaoProcesso.CRIADO);

            Unidade unidade1 = UnidadeTestBuilder.umaDe().comCodigo("1").build();
            Unidade unidade2 = UnidadeTestBuilder.umaDe().comCodigo("2").build();

            when(processoConsultaService.buscarPorId(codigoProcesso)).thenReturn(processo);
            when(processoValidador.getMensagemErroUnidadesSemMapa(anyList())).thenReturn(Optional.empty());
            when(unidadeService.buscarEntidadePorId(1L)).thenReturn(unidade1);
            when(unidadeService.buscarEntidadePorId(2L)).thenReturn(unidade2);
            when(processoRepo.saveAndFlush(any(Processo.class))).thenReturn(processo);

            // When
            Processo resultado = service.atualizar(codigoProcesso, request);

            // Then
            assertThat(resultado).isNotNull();
            verify(processoValidador).getMensagemErroUnidadesSemMapa(List.of(1L, 2L));
            verify(processoRepo).saveAndFlush(processo);
            assertThat(processo.getTipo()).isEqualTo(TipoProcesso.DIAGNOSTICO);
        }

        @Test
        @DisplayName("Deve lançar erro ao atualizar para REVISAO com unidades sem mapa")
        void deveLancarErroAoAtualizarParaRevisaoComUnidadesSemMapa() {
            // Given
            Long codigoProcesso = 100L;
            LocalDateTime dataLimite = LocalDateTime.now().plusDays(30);
            
            AtualizarProcessoRequest request = AtualizarProcessoRequest.builder()
                    .codigo(codigoProcesso)
                    .descricao("Processo de Revisão")
                    .tipo(TipoProcesso.REVISAO)
                    .dataLimiteEtapa1(dataLimite)
                    .unidades(List.of(1L))
                    .build();

            Processo processo = new Processo();
            processo.setCodigo(codigoProcesso);
            processo.setSituacao(SituacaoProcesso.CRIADO);

            Unidade unidade1 = UnidadeTestBuilder.umaDe().comCodigo("1").build();

            when(processoConsultaService.buscarPorId(codigoProcesso)).thenReturn(processo);
            when(processoValidador.getMensagemErroUnidadesSemMapa(anyList()))
                    .thenReturn(Optional.of("Unidades sem mapa: Unidade 1"));

            // When/Then
            assertThatThrownBy(() -> service.atualizar(codigoProcesso, request))
                    .isInstanceOf(ErroProcesso.class)
                    .hasMessage("Unidades sem mapa: Unidade 1");

            verify(processoValidador).getMensagemErroUnidadesSemMapa(List.of(1L));
            verify(processoRepo, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("Deve lançar erro ao atualizar para DIAGNOSTICO com unidades sem mapa")
        void deveLancarErroAoAtualizarParaDiagnosticoComUnidadesSemMapa() {
            // Given
            Long codigoProcesso = 100L;
            LocalDateTime dataLimite = LocalDateTime.now().plusDays(30);
            
            AtualizarProcessoRequest request = AtualizarProcessoRequest.builder()
                    .codigo(codigoProcesso)
                    .descricao("Processo de Diagnóstico")
                    .tipo(TipoProcesso.DIAGNOSTICO)
                    .dataLimiteEtapa1(dataLimite)
                    .unidades(List.of(1L, 2L))
                    .build();

            Processo processo = new Processo();
            processo.setCodigo(codigoProcesso);
            processo.setSituacao(SituacaoProcesso.CRIADO);

            Unidade unidade1 = UnidadeTestBuilder.umaDe().comCodigo("1").build();
            Unidade unidade2 = UnidadeTestBuilder.umaDe().comCodigo("2").build();

            when(processoConsultaService.buscarPorId(codigoProcesso)).thenReturn(processo);
            when(processoValidador.getMensagemErroUnidadesSemMapa(anyList()))
                    .thenReturn(Optional.of("Unidades sem mapa: Unidade 1, Unidade 2"));

            // When/Then
            assertThatThrownBy(() -> service.atualizar(codigoProcesso, request))
                    .isInstanceOf(ErroProcesso.class)
                    .hasMessage("Unidades sem mapa: Unidade 1, Unidade 2");

            verify(processoValidador).getMensagemErroUnidadesSemMapa(List.of(1L, 2L));
            verify(processoRepo, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("Deve atualizar processo para MAPEAMENTO sem validação de mapa")
        void deveAtualizarProcessoParaMapeamentoSemValidacao() {
            // Given
            Long codigoProcesso = 100L;
            LocalDateTime dataLimite = LocalDateTime.now().plusDays(30);
            
            AtualizarProcessoRequest request = AtualizarProcessoRequest.builder()
                    .codigo(codigoProcesso)
                    .descricao("Processo de Mapeamento")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(dataLimite)
                    .unidades(List.of(1L))
                    .build();

            Processo processo = new Processo();
            processo.setCodigo(codigoProcesso);
            processo.setSituacao(SituacaoProcesso.CRIADO);

            Unidade unidade1 = UnidadeTestBuilder.umaDe().comCodigo("1").build();

            when(processoConsultaService.buscarPorId(codigoProcesso)).thenReturn(processo);
            when(unidadeService.buscarEntidadePorId(1L)).thenReturn(unidade1);
            when(processoRepo.saveAndFlush(any(Processo.class))).thenReturn(processo);

            // When
            Processo resultado = service.atualizar(codigoProcesso, request);

            // Then
            assertThat(resultado).isNotNull();
            verify(processoValidador, never()).getMensagemErroUnidadesSemMapa(anyList());
            verify(processoRepo).saveAndFlush(processo);
        }
    }

    @Nested
    @DisplayName("Remoção de Processo")
    class RemocaoTests {

        @Test
        @DisplayName("Deve apagar processo na situação CRIADO com sucesso")
        void deveApagarProcessoCriadoComSucesso() {
            // Given
            Long codigoProcesso = 100L;

            Processo processo = new Processo();
            processo.setCodigo(codigoProcesso);
            processo.setSituacao(SituacaoProcesso.CRIADO);

            when(processoConsultaService.buscarPorId(codigoProcesso)).thenReturn(processo);

            // When
            service.apagar(codigoProcesso);

            // Then
            verify(processoConsultaService).buscarPorId(codigoProcesso);
            verify(processoRepo).deleteById(codigoProcesso);
        }

        @Test
        @DisplayName("Deve lançar erro ao apagar processo na situação EM_ANDAMENTO")
        void deveLancarErroAoApagarProcessoEmAndamento() {
            // Given
            Long codigoProcesso = 100L;

            Processo processo = new Processo();
            processo.setCodigo(codigoProcesso);
            processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

            when(processoConsultaService.buscarPorId(codigoProcesso)).thenReturn(processo);

            // When/Then
            assertThatThrownBy(() -> service.apagar(codigoProcesso))
                    .isInstanceOf(ErroProcessoEmSituacaoInvalida.class)
                    .hasMessage("Apenas processos na situação 'CRIADO' podem ser removidos.");

            verify(processoConsultaService).buscarPorId(codigoProcesso);
            verify(processoRepo, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("Deve lançar erro ao apagar processo na situação FINALIZADO")
        void deveLancarErroAoApagarProcessoFinalizado() {
            // Given
            Long codigoProcesso = 100L;

            Processo processo = new Processo();
            processo.setCodigo(codigoProcesso);
            processo.setSituacao(SituacaoProcesso.FINALIZADO);

            when(processoConsultaService.buscarPorId(codigoProcesso)).thenReturn(processo);

            // When/Then
            assertThatThrownBy(() -> service.apagar(codigoProcesso))
                    .isInstanceOf(ErroProcessoEmSituacaoInvalida.class)
                    .hasMessage("Apenas processos na situação 'CRIADO' podem ser removidos.");

            verify(processoConsultaService).buscarPorId(codigoProcesso);
            verify(processoRepo, never()).deleteById(anyLong());
        }
    }
}
