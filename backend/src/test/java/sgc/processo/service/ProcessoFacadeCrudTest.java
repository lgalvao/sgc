package sgc.processo.service;

import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.fixture.ProcessoFixture;
import sgc.organizacao.UnidadeFacade;
import sgc.processo.dto.AtualizarProcessoRequest;
import sgc.processo.dto.CriarProcessoRequest;
import sgc.processo.dto.ProcessoDto;
import sgc.processo.erros.ErroProcesso;
import sgc.processo.erros.ErroProcessoEmSituacaoInvalida;
import sgc.processo.mapper.ProcessoMapper;
import sgc.processo.model.Processo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.alerta.AlertaFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.subprocesso.mapper.SubprocessoMapper;
import sgc.subprocesso.service.SubprocessoFacade;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("ProcessoFacade - CRUD Operations")
class ProcessoFacadeCrudTest {
    @Mock
    private ProcessoManutencaoService processoManutencaoService;
    @Mock
    private ProcessoConsultaService processoConsultaService;
    @Mock
    private UnidadeFacade unidadeService;
    @Mock
    private ProcessoMapper processoMapper;
    @Mock
    private ProcessoValidador processoValidador;
    @Mock
    private SubprocessoFacade subprocessoFacade;
    @Mock
    private ProcessoDetalheBuilder processoDetalheBuilder;
    @Mock
    private SubprocessoMapper subprocessoMapper;
    @Mock
    private UsuarioFacade usuarioService;
    @Mock
    private ProcessoInicializador processoInicializador;
    @Mock
    private AlertaFacade alertaService;
    @Mock
    private ProcessoAcessoService processoAcessoService;
    @Mock
    private ProcessoFinalizador processoFinalizador;

    @InjectMocks
    private ProcessoFacade processoFacade;

    @Nested
    @DisplayName("Criação de Processo")
    class Criacao {
        @Test
        @DisplayName("Deve criar processo quando dados válidos")
        void deveCriarProcessoQuandoDadosValidos() {
            // Arrange
            CriarProcessoRequest req = new CriarProcessoRequest(
                    "Teste", TipoProcesso.MAPEAMENTO, LocalDateTime.now(), List.of(1L));
            
            when(processoManutencaoService.criar(req)).thenAnswer(
                    i -> {
                        // Simula retorno do serviço
                        Processo p = new Processo();
                        p.setCodigo(100L);
                        p.setDescricao(req.descricao());
                        p.setTipo(req.tipo());
                        p.setSituacao(SituacaoProcesso.CRIADO);
                        return p;
                    });
            when(processoMapper.toDto(any())).thenReturn(ProcessoDto.builder().build());

            // Act
            ProcessoDto resultado = processoFacade.criar(req);

            // Assert
            assertThat(resultado).isNotNull();
            verify(processoManutencaoService).criar(req);
        }

        @Test
        @DisplayName("Deve lançar exceção quando unidade não encontrada (propagada do serviço)")
        void deveLancarExcecaoQuandoUnidadeNaoEncontrada() {
            // Arrange
            CriarProcessoRequest req = new CriarProcessoRequest(
                    "Teste", TipoProcesso.MAPEAMENTO, LocalDateTime.now(), List.of(99L));
            
            when(processoManutencaoService.criar(req))
                    .thenThrow(new ErroEntidadeNaoEncontrada("Unidade", 99L));

            // Act & Assert
            assertThatThrownBy(() -> processoFacade.criar(req))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                    .hasMessageContaining("Unidade")
                    .hasNoCause();
        }

        @Test
        @DisplayName("Deve retornar erro se REVISAO e validador retornar mensagem (propagado do serviço)")
        void deveRetornarErroSeRevisaoEValidadorFalhar() {
            var req = new CriarProcessoRequest("T", TipoProcesso.REVISAO, LocalDateTime.now(), List.of(1L));
            
            when(processoManutencaoService.criar(req)).thenThrow(new ErroProcesso("Erro"));

            assertThatThrownBy(() -> processoFacade.criar(req))
                    .isInstanceOf(ErroProcesso.class)
                    .hasMessage("Erro");
        }
    }

    @Nested
    @DisplayName("Atualização de Processo")
    class Atualizacao {
        @Test
        @DisplayName("Deve atualizar processo quando está em situação CRIADO")
        void deveAtualizarProcessoQuandoEmSituacaoCriado() {
            // Arrange
            Long id = 100L;
            Processo processo = ProcessoFixture.processoPadrao();
            processo.setCodigo(id);

            AtualizarProcessoRequest req = AtualizarProcessoRequest.builder()
                    .codigo(id)
                    .descricao("Nova Desc")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(LocalDateTime.now())
                    .unidades(List.of(1L))
                    .build();

            when(processoManutencaoService.atualizar(id, req)).thenReturn(processo);
            when(processoMapper.toDto(any())).thenReturn(ProcessoDto.builder().build());

            // Act
            ProcessoDto resultado = processoFacade.atualizar(id, req);

            // Assert
            assertThat(resultado).isNotNull();
            verify(processoManutencaoService).atualizar(id, req);
        }

        @Test
        @DisplayName("Deve lançar exceção quando atualizar e processo não encontrado (propagado)")
        void deveLancarExcecaoQuandoAtualizarEProcessoNaoEncontrado() {
            // Arrange
            var req = AtualizarProcessoRequest.builder().build();
            when(processoManutencaoService.atualizar(99L, req)).thenThrow(new ErroEntidadeNaoEncontrada("Processo", 99L));

            // Act & Assert
            assertThatThrownBy(() -> processoFacade.atualizar(99L, req))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve lançar exceção quando atualizar e processo não está em situação CRIADO (propagado)")
        void deveLancarExcecaoQuandoAtualizarEProcessoNaoCriado() {
            // Arrange
            Long id = 100L;
            var req = AtualizarProcessoRequest.builder().build();
            
            when(processoManutencaoService.atualizar(id, req))
                    .thenThrow(new ErroProcessoEmSituacaoInvalida("Apenas processos na situação 'CRIADO' podem ser editados."));

            // Act & Assert
            assertThatThrownBy(() -> processoFacade.atualizar(id, req))
                    .isInstanceOf(ErroProcessoEmSituacaoInvalida.class);
        }
    }

    @Nested
    @DisplayName("Exclusão de Processo")
    class Exclusao {
        @Test
        @DisplayName("Deve apagar processo quando está em situação CRIADO")
        void deveApagarProcessoQuandoEmSituacaoCriado() {
            // Arrange
            Long id = 100L;
            
            // Act
            processoFacade.apagar(id);

            // Assert
            verify(processoManutencaoService).apagar(id);
        }

        @Test
        @DisplayName("Deve lançar exceção quando apagar e processo não encontrado (propagado)")
        void deveLancarExcecaoQuandoApagarEProcessoNaoEncontrado() {
            // Arrange
            doThrow(new ErroEntidadeNaoEncontrada("Processo", 99L))
                    .when(processoManutencaoService).apagar(99L);

            // Act & Assert
            assertThatThrownBy(() -> processoFacade.apagar(99L))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve lançar exceção quando apagar e processo não está em situação CRIADO (propagado)")
        void deveLancarExcecaoQuandoApagarEProcessoNaoCriado() {
            // Arrange
            Long id = 100L;
            
            doThrow(new ErroProcessoEmSituacaoInvalida("Apenas processos na situação 'CRIADO' podem ser removidos."))
                    .when(processoManutencaoService).apagar(id);

            // Act & Assert
            assertThatThrownBy(() -> processoFacade.apagar(id))
                    .isInstanceOf(ErroProcessoEmSituacaoInvalida.class);
        }
    }
}