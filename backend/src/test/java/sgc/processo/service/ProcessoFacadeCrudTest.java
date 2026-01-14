package sgc.processo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import sgc.alerta.AlertaService;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.fixture.ProcessoFixture;
import sgc.fixture.UnidadeFixture;
import sgc.mapa.model.MapaRepo;
import sgc.mapa.service.CopiaMapaService;
import sgc.organizacao.UnidadeService;
import sgc.organizacao.UsuarioService;
import sgc.organizacao.model.Unidade;
import sgc.processo.dto.AtualizarProcessoRequest;
import sgc.processo.dto.CriarProcessoRequest;
import sgc.processo.dto.ProcessoDto;
import sgc.processo.erros.ErroProcesso;
import sgc.processo.erros.ErroProcessoEmSituacaoInvalida;
import sgc.processo.eventos.EventoProcessoAtualizado;
import sgc.processo.eventos.EventoProcessoCriado;
import sgc.processo.mapper.ProcessoMapper;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.mapper.SubprocessoMapper;
import sgc.subprocesso.model.SubprocessoMovimentacaoRepo;
import sgc.subprocesso.service.SubprocessoFacade;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("ProcessoFacade - CRUD Operations")
class ProcessoFacadeCrudTest {
    @Mock
    private ProcessoRepo processoRepo;
    @Mock
    private UnidadeService unidadeService;
    @Mock
    private SubprocessoFacade subprocessoFacade;
    @Mock
    private ApplicationEventPublisher publicadorEventos;
    @Mock
    private ProcessoMapper processoMapper;
    @Mock
    private ProcessoDetalheBuilder processoDetalheBuilder;
    @Mock
    private MapaRepo mapaRepo;
    @Mock
    private SubprocessoMovimentacaoRepo movimentacaoRepo;
    @Mock
    private SubprocessoMapper subprocessoMapper;
    @Mock
    private CopiaMapaService servicoDeCopiaDeMapa;
    @Mock
    private UsuarioService usuarioService;
    @Mock
    private ProcessoInicializador processoInicializador;
    @Mock
    private AlertaService alertaService;
    
    // Specialized services
    @Mock
    private ProcessoAcessoService processoAcessoService;
    @Mock
    private ProcessoValidador processoValidador;
    @Mock
    private ProcessoFinalizador processoFinalizador;
    @Mock
    private ProcessoConsultaService processoConsultaService;

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
            Unidade unidade = UnidadeFixture.unidadeComId(1L);

            when(unidadeService.buscarEntidadePorId(1L)).thenReturn(unidade);
            when(processoRepo.saveAndFlush(any()))
                    .thenAnswer(
                            i -> {
                                Processo p = i.getArgument(0);
                                p.setCodigo(100L);
                                return p;
                            });
            when(processoMapper.toDto(any())).thenReturn(ProcessoDto.builder().build());

            // Act
            ProcessoDto resultado = processoFacade.criar(req);

            // Assert
            assertThat(resultado).isNotNull();
            verify(processoRepo).saveAndFlush(argThat(p -> p.getDescricao().equals("Teste") &&
                    p.getTipo() == TipoProcesso.MAPEAMENTO &&
                    p.getSituacao() == SituacaoProcesso.CRIADO));
            verify(publicadorEventos).publishEvent(any(EventoProcessoCriado.class));
        }

        // NOTA: Testes de validação de descrição vazia/nula e unidades vazias
        // foram removidos pois a validação agora é feita via Bean Validation (@Valid)
        // no Controller, não mais no Service. Ver ProcessoControllerTest para esses
        // cenários.

        @Test
        @DisplayName("Deve lançar exceção quando unidade não encontrada")
        void deveLancarExcecaoQuandoUnidadeNaoEncontrada() {
            // Arrange
            CriarProcessoRequest req = new CriarProcessoRequest(
                    "Teste", TipoProcesso.MAPEAMENTO, LocalDateTime.now(), List.of(99L));
            when(unidadeService.buscarEntidadePorId(99L)).thenThrow(new ErroEntidadeNaoEncontrada("Unidade", 99L));

            // Act & Assert
            assertThatThrownBy(() -> processoFacade.criar(req))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class)
                    .hasMessageContaining("Unidade")
                    .hasNoCause();
        }

        @Test
        @DisplayName("Deve validar mapa para processo REVISAO")
        void deveValidarMapaParaRevisao() {
            CriarProcessoRequest req = new CriarProcessoRequest(
                    "Teste", TipoProcesso.REVISAO, LocalDateTime.now(), List.of(1L));

            Unidade u = UnidadeFixture.unidadeComId(1L);
            when(unidadeService.buscarEntidadePorId(1L)).thenReturn(u);
            when(processoValidador.getMensagemErroUnidadesSemMapa(anyList()))
                    .thenReturn(Optional.of("Unidades sem mapa vigente: U1"));

            assertThatThrownBy(() -> processoFacade.criar(req))
                    .isInstanceOf(ErroProcesso.class)
                    .hasMessageContaining("U1");
        }

        @Test
        @DisplayName("Deve retornar erro se REVISAO e validador retornar mensagem")
        void deveRetornarErroSeRevisaoEValidadorFalhar() {
            var req = new CriarProcessoRequest("T", TipoProcesso.REVISAO, LocalDateTime.now(), List.of(1L));
            Unidade u = new Unidade();
            u.setCodigo(1L);
            when(unidadeService.buscarEntidadePorId(1L)).thenReturn(u);
            when(processoValidador.getMensagemErroUnidadesSemMapa(anyList())).thenReturn(Optional.of("Erro"));

            assertThatThrownBy(() -> processoFacade.criar(req))
                    .isInstanceOf(ErroProcesso.class)
                    .hasMessage("Erro");
        }

        @Test
        @DisplayName("Deve falhar se unidade participamente for INTERMEDIARIA")
        void deveFalharSeUnidadeIntermediaria() {
            CriarProcessoRequest req = new CriarProcessoRequest(
                    "Teste", TipoProcesso.MAPEAMENTO, LocalDateTime.now(), List.of(1L));

            Unidade u = UnidadeFixture.unidadeComId(1L);
            u.setTipo(sgc.organizacao.model.TipoUnidade.INTERMEDIARIA);
            when(unidadeService.buscarEntidadePorId(1L)).thenReturn(u);

            assertThatThrownBy(() -> processoFacade.criar(req))
                    .isInstanceOf(sgc.comum.erros.ErroEstadoImpossivel.class);
        }

        @Test
        @DisplayName("Deve criar processo de REVISAO quando todas unidades tem mapa vigente")
        void deveCriarProcessoRevisaoQuandoUnidadesTemMapa() {
            CriarProcessoRequest req = new CriarProcessoRequest(
                    "Revisao", TipoProcesso.REVISAO, LocalDateTime.now(), List.of(1L));

            Unidade u = UnidadeFixture.unidadeComId(1L);
            when(unidadeService.buscarEntidadePorId(1L)).thenReturn(u);
            when(processoValidador.getMensagemErroUnidadesSemMapa(anyList())).thenReturn(Optional.empty()); // No error - all units have maps
            when(processoRepo.saveAndFlush(any())).thenReturn(new Processo());
            when(processoMapper.toDto(any())).thenReturn(ProcessoDto.builder().build());

            ProcessoDto resultado = processoFacade.criar(req);
            assertThat(resultado).isNotNull();
        }

        @Test
        @DisplayName("Deve criar processo de DIAGNOSTICO quando todas unidades tem mapa vigente")
        void deveCriarProcessoDiagnosticoQuandoUnidadesTemMapa() {
            CriarProcessoRequest req = new CriarProcessoRequest(
                    "Diagnostico", TipoProcesso.DIAGNOSTICO, LocalDateTime.now(), List.of(1L));

            Unidade u = UnidadeFixture.unidadeComId(1L);
            when(unidadeService.buscarEntidadePorId(1L)).thenReturn(u);
            when(processoValidador.getMensagemErroUnidadesSemMapa(anyList())).thenReturn(Optional.empty()); // No error - all units have maps
            when(processoRepo.saveAndFlush(any())).thenReturn(new Processo());
            when(processoMapper.toDto(any())).thenReturn(ProcessoDto.builder().build());

            ProcessoDto resultado = processoFacade.criar(req);
            assertThat(resultado).isNotNull();
        }

        @Test
        @DisplayName("criar: erro se REVISAO e unidades sem mapa")
        void criar_ErroRevisaoSemMapa() {
            CriarProcessoRequest req = new CriarProcessoRequest();
            req.setDescricao("Teste");
            req.setUnidades(List.of(1L));
            req.setTipo(TipoProcesso.REVISAO);

            Unidade u = new Unidade();
            u.setCodigo(1L);
            when(unidadeService.buscarEntidadePorId(1L)).thenReturn(u);
            when(processoValidador.getMensagemErroUnidadesSemMapa(any()))
                    .thenReturn(Optional.of("As seguintes unidades não possuem mapa vigente: SIGLA"));

            assertThatThrownBy(() -> processoFacade.criar(req))
                .isInstanceOf(ErroProcesso.class)
                .hasMessageContaining("não possuem mapa vigente");
        }

        @Test
        @DisplayName("criar: sucesso se REVISAO e unidades com mapa")
        void criar_SucessoRevisaoComMapa() {
            CriarProcessoRequest req = new CriarProcessoRequest();
            req.setDescricao("Teste");
            req.setUnidades(List.of(1L));
            req.setTipo(TipoProcesso.REVISAO);

            Unidade u = new Unidade();
            u.setCodigo(1L);
            when(unidadeService.buscarEntidadePorId(1L)).thenReturn(u);
            when(processoValidador.getMensagemErroUnidadesSemMapa(any())).thenReturn(Optional.empty());

            when(processoRepo.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));
            when(processoMapper.toDto(any())).thenReturn(ProcessoDto.builder().build());

            processoFacade.criar(req);

            verify(processoRepo).saveAndFlush(any());
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

            when(processoRepo.findById(id)).thenReturn(Optional.of(processo));
            when(unidadeService.buscarEntidadePorId(1L)).thenReturn(UnidadeFixture.unidadeComId(1L));
            when(processoRepo.saveAndFlush(any())).thenReturn(processo);
            when(processoMapper.toDto(any())).thenReturn(ProcessoDto.builder().build());

            // Act
            ProcessoDto resultado = processoFacade.atualizar(id, req);

            // Assert
            assertThat(resultado).isNotNull();
            verify(processoRepo).saveAndFlush(processo);
            verify(publicadorEventos).publishEvent(any(EventoProcessoAtualizado.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando atualizar e processo não encontrado")
        void deveLancarExcecaoQuandoAtualizarEProcessoNaoEncontrado() {
            // Arrange
            when(processoRepo.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            var req = new AtualizarProcessoRequest();
            assertThatThrownBy(() -> processoFacade.atualizar(99L, req))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve lançar exceção quando atualizar e processo não está em situação CRIADO")
        void deveLancarExcecaoQuandoAtualizarEProcessoNaoCriado() {
            // Arrange
            Long id = 100L;
            Processo processo = ProcessoFixture.processoEmAndamento();
            processo.setCodigo(id);

            when(processoRepo.findById(id)).thenReturn(Optional.of(processo));

            // Act & Assert
            var req = new AtualizarProcessoRequest();
            assertThatThrownBy(() -> processoFacade.atualizar(id, req))
                    .isInstanceOf(ErroProcessoEmSituacaoInvalida.class);
        }

        @Test
        @DisplayName("Deve atualizar descrição quando válida")
        void deveAtualizarDescricaoQuandoValida() {
            // Arrange
            Long id = 100L;
            Processo processo = ProcessoFixture.processoPadrao();
            processo.setCodigo(id);

            AtualizarProcessoRequest req = AtualizarProcessoRequest.builder()
                    .codigo(id)
                    .descricao("Nova Descrição Atualizada")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .unidades(List.of(1L))
                    .build();

            when(processoRepo.findById(id)).thenReturn(Optional.of(processo));
            when(unidadeService.buscarEntidadePorId(1L)).thenReturn(UnidadeFixture.unidadeComId(1L));
            when(processoRepo.saveAndFlush(any())).thenReturn(processo);
            when(processoMapper.toDto(any())).thenReturn(ProcessoDto.builder().build());

            // Act
            processoFacade.atualizar(id, req);

            // Assert
            verify(processoRepo).saveAndFlush(argThat(p ->
                    p.getDescricao().equals("Nova Descrição Atualizada")));
        }

        @Test
        @DisplayName("Deve atualizar tipo quando válido")
        void deveAtualizarTipoQuandoValido() {
            // Arrange
            Long id = 100L;
            Processo processo = ProcessoFixture.processoPadrao();
            processo.setCodigo(id);
            processo.setTipo(TipoProcesso.MAPEAMENTO);

            AtualizarProcessoRequest req = AtualizarProcessoRequest.builder()
                    .codigo(id)
                    .descricao("Desc")
                    .tipo(TipoProcesso.DIAGNOSTICO)
                    .unidades(List.of(1L))
                    .build();

            when(processoRepo.findById(id)).thenReturn(Optional.of(processo));
            when(unidadeService.buscarEntidadePorId(1L)).thenReturn(UnidadeFixture.unidadeComId(1L));
            when(processoValidador.getMensagemErroUnidadesSemMapa(anyList())).thenReturn(Optional.empty());
            when(processoRepo.saveAndFlush(any())).thenReturn(processo);
            when(processoMapper.toDto(any())).thenReturn(ProcessoDto.builder().build());

            // Act
            processoFacade.atualizar(id, req);

            // Assert
            verify(processoRepo).saveAndFlush(argThat(p ->
                    p.getTipo() == TipoProcesso.DIAGNOSTICO));
        }

        @Test
        @DisplayName("Deve atualizar datas quando válidas")
        void deveAtualizarDatasQuandoValidas() {
            // Arrange
            Long id = 100L;
            Processo processo = ProcessoFixture.processoPadrao();
            processo.setCodigo(id);

            LocalDateTime novaDataLimite = LocalDateTime.now().plusDays(30);

            AtualizarProcessoRequest req = AtualizarProcessoRequest.builder()
                    .codigo(id)
                    .descricao("Desc")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .dataLimiteEtapa1(novaDataLimite)
                    .unidades(List.of(1L))
                    .build();

            when(processoRepo.findById(id)).thenReturn(Optional.of(processo));
            when(unidadeService.buscarEntidadePorId(1L)).thenReturn(UnidadeFixture.unidadeComId(1L));
            when(processoRepo.saveAndFlush(any())).thenReturn(processo);
            when(processoMapper.toDto(any())).thenReturn(ProcessoDto.builder().build());

            // Act
            processoFacade.atualizar(id, req);

            // Assert
            verify(processoRepo).saveAndFlush(argThat(p ->
                    p.getDataLimite().equals(novaDataLimite)));
        }

        @Test
        @DisplayName("Deve manter dados não especificados")
        void deveManterDadosNaoEspecificados() {
            // Arrange
            Long id = 100L;
            Processo processo = ProcessoFixture.processoPadrao();
            processo.setCodigo(id);
            LocalDateTime dataOriginal = LocalDateTime.now().minusDays(5);
            processo.setDataLimite(dataOriginal);

            // Como dataLimiteEtapa1 é @NotNull no DTO, sempre deve ser fornecida
            // Este teste verifica que outros campos do processo são mantidos
            AtualizarProcessoRequest req = AtualizarProcessoRequest.builder()
                    .codigo(id)
                    .descricao("Nova Desc")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .unidades(List.of(1L))
                    .dataLimiteEtapa1(dataOriginal) // Mantendo a data original
                    .build();

            when(processoRepo.findById(id)).thenReturn(Optional.of(processo));
            when(unidadeService.buscarEntidadePorId(1L)).thenReturn(UnidadeFixture.unidadeComId(1L));
            when(processoRepo.saveAndFlush(any())).thenReturn(processo);
            when(processoMapper.toDto(any())).thenReturn(ProcessoDto.builder().build());

            // Act
            processoFacade.atualizar(id, req);

            // Assert - verifica que a data original foi mantida
            verify(processoRepo).saveAndFlush(argThat(p ->
                    p.getDataLimite().equals(dataOriginal)));
        }

        @Test
        @DisplayName("Deve publicar evento de atualização")
        void devePublicarEventoDeAtualizacao() {
            // Arrange
            Long id = 100L;
            Processo processo = ProcessoFixture.processoPadrao();
            processo.setCodigo(id);

            AtualizarProcessoRequest req = AtualizarProcessoRequest.builder()
                    .codigo(id)
                    .descricao("Nova Desc")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .unidades(List.of(1L))
                    .build();

            when(processoRepo.findById(id)).thenReturn(Optional.of(processo));
            when(unidadeService.buscarEntidadePorId(1L)).thenReturn(UnidadeFixture.unidadeComId(1L));
            when(processoRepo.saveAndFlush(any())).thenReturn(processo);
            when(processoMapper.toDto(any())).thenReturn(ProcessoDto.builder().build());

            // Act
            processoFacade.atualizar(id, req);

            // Assert
            var captor = org.mockito.ArgumentCaptor.forClass(EventoProcessoAtualizado.class);
            verify(publicadorEventos).publishEvent(captor.capture());
            assertThat(captor.getValue().getProcesso().getCodigo()).isEqualTo(id);
        }

        @Test
        @DisplayName("Deve permitir adicionar unidades na atualização")
        void devePermitirAdicionarUnidadesNaAtualizacao() {
            // Arrange
            Long id = 100L;
            Processo processo = ProcessoFixture.processoPadrao();
            processo.setCodigo(id);

            Unidade novaUnidade = UnidadeFixture.unidadeComId(2L);

            AtualizarProcessoRequest req = AtualizarProcessoRequest.builder()
                    .codigo(id)
                    .descricao("Desc")
                    .tipo(TipoProcesso.MAPEAMENTO)
                    .unidades(List.of(2L))
                    .build();

            when(processoRepo.findById(id)).thenReturn(Optional.of(processo));
            when(unidadeService.buscarEntidadePorId(2L)).thenReturn(novaUnidade);
            when(processoRepo.saveAndFlush(any())).thenReturn(processo);
            when(processoMapper.toDto(any())).thenReturn(ProcessoDto.builder().build());

            // Act
            processoFacade.atualizar(id, req);

            // Assert
            verify(unidadeService).buscarEntidadePorId(2L);
            verify(processoRepo).saveAndFlush(any());
        }

        @Test
        @DisplayName("atualizar: erro se processo não está CRIADO")
        void atualizar_ErroSeNaoCriado() {
            Processo p = new Processo();
            p.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
            when(processoRepo.findById(1L)).thenReturn(Optional.of(p));

            var req = new AtualizarProcessoRequest();
            assertThatThrownBy(() -> processoFacade.atualizar(1L, req))
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
            Processo processo = ProcessoFixture.processoPadrao();
            processo.setCodigo(id);
            when(processoRepo.findById(id)).thenReturn(Optional.of(processo));

            // Act
            processoFacade.apagar(id);

            // Assert
            verify(processoRepo).deleteById(id);
        }

        @Test
        @DisplayName("Deve lançar exceção quando apagar e processo não encontrado")
        void deveLancarExcecaoQuandoApagarEProcessoNaoEncontrado() {
            // Arrange
            when(processoRepo.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> processoFacade.apagar(99L))
                    .isInstanceOf(ErroEntidadeNaoEncontrada.class);
        }

        @Test
        @DisplayName("Deve lançar exceção quando apagar e processo não está em situação CRIADO")
        void deveLancarExcecaoQuandoApagarEProcessoNaoCriado() {
            // Arrange
            Long id = 100L;
            Processo processo = ProcessoFixture.processoEmAndamento();
            processo.setCodigo(id);
            when(processoRepo.findById(id)).thenReturn(Optional.of(processo));

            // Act & Assert
            assertThatThrownBy(() -> processoFacade.apagar(id))
                    .isInstanceOf(ErroProcessoEmSituacaoInvalida.class);
        }
    }
}
