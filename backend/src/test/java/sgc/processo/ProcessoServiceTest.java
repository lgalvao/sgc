package sgc.processo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.subprocesso.SituacaoSubprocesso;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.CopiaMapaService;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.mapa.modelo.UnidadeMapaRepo;
import sgc.notificacao.NotificacaoServico;
import sgc.notificacao.NotificacaoModeloEmailService;
import sgc.processo.dto.*;
import sgc.processo.eventos.ProcessoCriadoEvento;
import sgc.processo.eventos.ProcessoFinalizadoEvento;
import sgc.processo.eventos.ProcessoIniciadoEvento;
import sgc.processo.modelo.ErroProcesso;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.ProcessoRepo;
import sgc.processo.modelo.UnidadeProcessoRepo;
import sgc.sgrh.SgrhService;
import sgc.subprocesso.modelo.Movimentacao;
import sgc.subprocesso.modelo.MovimentacaoRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import sgc.processo.modelo.TipoProcesso;
import sgc.unidade.modelo.TipoUnidade;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProcessoServiceTest {
    private static final String PROCESSO_DE_TESTE = "Processo de Teste";
    private static final String MAPEAMENTO = "MAPEAMENTO";
    private static final String PROCESSO_NAO_ENCONTRADO_999 = "Processo não encontrado: 999";
    private static final String PROCESSO_ATUALIZADO = "Processo Atualizado";
    private static final String REVISAO = "REVISAO";
    @Mock
    private ProcessoRepo processoRepo;

    @Mock
    private UnidadeRepo unidadeRepo;

    @Mock
    private UnidadeProcessoRepo unidadeProcessoRepo;

    @Mock
    private SubprocessoRepo subprocessoRepo;

    @Mock
    private MapaRepo mapaRepo;

    @Mock
    private MovimentacaoRepo movimentacaoRepo;

    @Mock
    private UnidadeMapaRepo unidadeMapaRepo;

    @Mock
    private CopiaMapaService servicoDeCopiaDeMapa;

    @Mock
    private org.springframework.context.ApplicationEventPublisher publicadorDeEventos;

    @Mock
    private NotificacaoServico notificacaoServico;

    @Mock
    private NotificacaoModeloEmailService notificacaoModeloEmailService;

    @Mock
    private SgrhService sgrhService;

    @Mock
    private ProcessoConversor processoConversor;

    @Mock
    private ProcessoDetalheMapperCustomizado processoDetalheMapperCustomizado;

    private ProcessoService processoService;

    @BeforeEach
    void setUp() {
        processoService = new ProcessoService(
                processoRepo,
                unidadeRepo,
                unidadeProcessoRepo,
                subprocessoRepo,
                mapaRepo,
                movimentacaoRepo,
                unidadeMapaRepo,
                servicoDeCopiaDeMapa,
                publicadorDeEventos,
                notificacaoServico,
                notificacaoModeloEmailService,
                sgrhService,
                processoConversor,
                processoDetalheMapperCustomizado
        );
    }

    @Test
    void criar_ProcessoValido_CriaComSucesso() {
        var dataLimite = LocalDate.now().plusDays(30);
        var requisicao = new CriarProcessoReq(PROCESSO_DE_TESTE, MAPEAMENTO, dataLimite, List.of(1L));

        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setDescricao(PROCESSO_DE_TESTE);
        processo.setSituacao(SituacaoProcesso.CRIADO);
        processo.setDataCriacao(LocalDateTime.now());

        var dto = ProcessoDto.builder()
            .codigo(1L)
            .dataCriacao(LocalDateTime.now())
            .descricao(PROCESSO_DE_TESTE)
            .situacao(SituacaoProcesso.CRIADO)
            .build();

        when(processoRepo.save(any(Processo.class))).thenReturn(processo);
        when(processoConversor.toDTO(any(Processo.class))).thenReturn(dto);

        ProcessoDto resultado = processoService.criar(requisicao);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getCodigo());
        assertEquals(PROCESSO_DE_TESTE, resultado.getDescricao());
        verify(processoRepo).save(any(Processo.class));
        verify(publicadorDeEventos).publishEvent(any(ProcessoCriadoEvento.class));
    }

    @Test
    void criar_SemDescricao_LancaConstraintViolationException() {
        var requisicao = new CriarProcessoReq("", MAPEAMENTO, LocalDate.now().plusDays(30), List.of(1L));

        var exception = assertThrows(jakarta.validation.ConstraintViolationException.class,
            () -> processoService.criar(requisicao));
        
        assertEquals("A descrição do processo é obrigatória.", exception.getMessage());
    }

    @Test
    void criar_SemUnidades_LancaConstraintViolationException() {
        var requisicao = new CriarProcessoReq(PROCESSO_DE_TESTE, MAPEAMENTO, LocalDate.now().plusDays(30), Collections.emptyList());

        var exception = assertThrows(jakarta.validation.ConstraintViolationException.class,
            () -> processoService.criar(requisicao));
        
        assertEquals("Pelo menos uma unidade participante deve ser selecionada.", exception.getMessage());
    }

    @Test
    void criar_TipoRevisaoComUnidadeNaoEncontrada_LancaErroEntidadeNaoEncontrada() {
        var requisicao = new CriarProcessoReq(PROCESSO_DE_TESTE, REVISAO, LocalDate.now().plusDays(30), List.of(999L));

        when(unidadeRepo.findById(999L)).thenReturn(Optional.empty());

        var exception = assertThrows(ErroEntidadeNaoEncontrada.class,
            () -> processoService.criar(requisicao));
        
        assertEquals("Unidade com código 999 não foi encontrada.", exception.getMessage());
    }

    @Test
    void atualizar_ProcessoExisteAtualizaComSucesso() {
        var requisicao = new AtualizarProcessoReq(1L, PROCESSO_ATUALIZADO, REVISAO, LocalDate.now().plusDays(45), List.of(1L));

        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setDescricao("Processo Original");
        processo.setSituacao(SituacaoProcesso.CRIADO);

        var dto = ProcessoDto.builder()
            .codigo(1L)
            .descricao(PROCESSO_ATUALIZADO)
            .situacao(SituacaoProcesso.CRIADO)
            .build();

        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
        when(processoRepo.save(any(Processo.class))).thenReturn(processo);
        when(processoConversor.toDTO(any(Processo.class))).thenReturn(dto);

        ProcessoDto resultado = processoService.atualizar(1L, requisicao);

        assertNotNull(resultado);
        assertEquals(PROCESSO_ATUALIZADO, resultado.getDescricao());
        verify(processoRepo).save(any(Processo.class));
    }

    @Test
    void atualizar_ProcessoNaoEncontrado_LancaErroEntidadeNaoEncontrada() {
        var requisicao = new AtualizarProcessoReq(999L, "Desc", "TIPO", null, List.of(1L));
        
        when(processoRepo.findById(999L)).thenReturn(Optional.empty());

        var exception = assertThrows(ErroEntidadeNaoEncontrada.class,
            () -> processoService.atualizar(999L, requisicao));
        
        assertEquals(PROCESSO_NAO_ENCONTRADO_999, exception.getMessage());
    }

    @Test
    void atualizar_ProcessoNaoCriado_LancaIllegalStateException() {
        var requisicao = new AtualizarProcessoReq(1L, "Desc", "TIPO", null, List.of(1L));

        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));

        var exception = assertThrows(IllegalStateException.class,
            () -> processoService.atualizar(1L, requisicao));
        
        assertEquals("Apenas processos na situação 'CRIADO' podem ser editados.", exception.getMessage());
    }

    @Test
    void apagar_ProcessoExisteRemoveComSucesso() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setSituacao(SituacaoProcesso.CRIADO);

        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));

        processoService.apagar(1L);

        verify(processoRepo).deleteById(1L);
    }

    @Test
    void apagar_ProcessoNaoEncontrado_LancaErroEntidadeNaoEncontrada() {
        when(processoRepo.findById(999L)).thenReturn(Optional.empty());

        var exception = assertThrows(ErroEntidadeNaoEncontrada.class,
            () -> processoService.apagar(999L));
        
        assertEquals(PROCESSO_NAO_ENCONTRADO_999, exception.getMessage());
    }

    @Test
    void apagar_ProcessoNaoCriado_LancaIllegalStateException() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));

        var exception = assertThrows(IllegalStateException.class,
            () -> processoService.apagar(1L));
        
        assertEquals("Apenas processos na situação 'CRIADO' podem ser removidos.", exception.getMessage());
    }

    @Test
    void obterPorId_ProcessoExiste_RetornaOptionalComDto() {
        Processo processo = new Processo();
        processo.setCodigo(1L);

        var dto = ProcessoDto.builder().codigo(1L).build();

        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
        when(processoConversor.toDTO(processo)).thenReturn(dto);

        var resultado = processoService.obterPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getCodigo());
    }

    @Test
    void obterPorId_ProcessoNaoExiste_RetornaOptionalVazio() {
        when(processoRepo.findById(1L)).thenReturn(Optional.empty());

        var resultado = processoService.obterPorId(1L);

        assertFalse(resultado.isPresent());
    }


    @Test
    void obterDetalhes_PerfilAdmin_RetornaDetalhes() {
        Processo processo = new Processo();
        processo.setCodigo(1L);

        var detalhes = ProcessoDetalheDto.builder().codigo(1L).build();

        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
        when(unidadeProcessoRepo.findByProcessoCodigo(1L)).thenReturn(Collections.emptyList());
        when(subprocessoRepo.findByProcessoCodigoWithUnidade(1L)).thenReturn(Collections.emptyList());
        when(processoDetalheMapperCustomizado.toDetailDTO(any(), any(), any())).thenReturn(detalhes);

        ProcessoDetalheDto resultado = processoService.obterDetalhes(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getCodigo());
    }

    @Test
    void iniciarProcessoMapeamento_Valido_IniciaComSucesso() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setSituacao(SituacaoProcesso.CRIADO);
        processo.setTipo(TipoProcesso.MAPEAMENTO);
        processo.setDataLimite(LocalDate.now().plusDays(30));

        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setNome("Unidade Teste");
        unidade.setTipo(TipoUnidade.OPERACIONAL);

        Mapa mapa = new Mapa();
        mapa.setCodigo(100L);

        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
        when(unidadeRepo.findById(1L)).thenReturn(Optional.of(unidade));
        when(unidadeProcessoRepo.findUnidadesInProcessosAtivos(List.of(1L))).thenReturn(Collections.emptyList());
        when(mapaRepo.save(any(Mapa.class))).thenReturn(mapa);
        when(subprocessoRepo.save(any(Subprocesso.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(movimentacaoRepo.save(any(Movimentacao.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(processoRepo.save(any(Processo.class))).thenReturn(processo);
        when(processoConversor.toDTO(any(Processo.class))).thenAnswer(invocation -> {
            Processo p = invocation.getArgument(0);
            return ProcessoDto.builder()
                .codigo(p.getCodigo())
                .situacao(p.getSituacao())
                .build();
        });

        ProcessoDto resultado = processoService.iniciarProcessoMapeamento(1L, List.of(1L));

        assertNotNull(resultado);
        assertEquals(1L, resultado.getCodigo());
        verify(processoRepo).save(argThat(p -> p.getSituacao() == SituacaoProcesso.EM_ANDAMENTO));
        verify(publicadorDeEventos).publishEvent(any(ProcessoIniciadoEvento.class));

        ArgumentCaptor<Subprocesso> subprocessoCaptor = ArgumentCaptor.forClass(Subprocesso.class);
        verify(subprocessoRepo).save(subprocessoCaptor.capture());
        Subprocesso subprocessoSalvo = subprocessoCaptor.getValue();
        assertEquals(SituacaoSubprocesso.NAO_INICIADO, subprocessoSalvo.getSituacao());
    }

    @Test
    void iniciarProcessoMapeamento_ProcessoNaoEncontrado_LancaErroEntidadeNaoEncontrada() {
        when(processoRepo.findById(999L)).thenReturn(Optional.empty());

        var exception = assertThrows(ErroEntidadeNaoEncontrada.class,
            () -> processoService.iniciarProcessoMapeamento(999L, List.of(1L)));
        
        assertEquals("Processo não encontrado: 999", exception.getMessage());
    }

    @Test
    void iniciarProcessoMapeamento_SemUnidades_LancaIllegalArgumentException() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setSituacao(SituacaoProcesso.CRIADO);

        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));

        var exception = assertThrows(IllegalArgumentException.class,
            () -> processoService.iniciarProcessoMapeamento(1L, null));
        
        assertEquals("A lista de unidades é obrigatória para iniciar o processo de mapeamento.", exception.getMessage());
    }

    @Test
    void iniciarProcessoMapeamento_UnidadeEmProcessoAtivo_LancaErroProcesso() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setSituacao(SituacaoProcesso.CRIADO);

        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
        when(unidadeProcessoRepo.findUnidadesInProcessosAtivos(List.of(1L))).thenReturn(List.of(1L));

        var exception = assertThrows(ErroProcesso.class,
            () -> processoService.iniciarProcessoMapeamento(1L, List.of(1L)));
        
        assertTrue(exception.getMessage().contains("já participam de outro processo ativo"));
    }

    @Test
    void finalizar_ProcessoValido_FinalizaComSucesso() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

        Mapa mapa = new Mapa();
        mapa.setCodigo(100L);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(10L);
        subprocesso.setSituacao(SituacaoSubprocesso.MAPA_HOMOLOGADO);
        subprocesso.setMapa(mapa); // Adiciona o mapa necessário
        
        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setSigla("UT");
        subprocesso.setUnidade(unidade);

        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
        when(subprocessoRepo.findByProcessoCodigo(1L)).thenReturn(List.of(subprocesso));
        when(processoRepo.save(any(Processo.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(processoConversor.toDTO(any(Processo.class))).thenAnswer(invocation -> {
            Processo p = invocation.getArgument(0);
            return ProcessoDto.builder()
                .codigo(p.getCodigo())
                .situacao(p.getSituacao())
                .build();
        });

        ProcessoDto resultado = processoService.finalizar(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getCodigo());
        verify(processoRepo).save(argThat(p -> p.getSituacao() == SituacaoProcesso.FINALIZADO));
        verify(publicadorDeEventos).publishEvent(any(ProcessoFinalizadoEvento.class));
    }

    @Test
    void finalizar_ProcessoNaoEncontrado_LancaErroEntidadeNaoEncontrada() {
        when(processoRepo.findById(999L)).thenReturn(Optional.empty());

        var exception = assertThrows(ErroEntidadeNaoEncontrada.class,
            () -> processoService.finalizar(999L));
        
        assertEquals(PROCESSO_NAO_ENCONTRADO_999, exception.getMessage());
    }

    @Test
    void finalizar_SemSubprocessosHomologados_LancaErroProcesso() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

        Subprocesso subprocesso = new Subprocesso();
        subprocesso.setCodigo(10L);
        subprocesso.setSituacao(SituacaoSubprocesso.NAO_INICIADO); // não homologado

        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setSigla("UT");
        subprocesso.setUnidade(unidade);

        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
        when(subprocessoRepo.findByProcessoCodigo(1L)).thenReturn(List.of(subprocesso));

        var exception = assertThrows(
            ErroProcesso.class,
            () -> processoService.finalizar(1L)
        );

        assertTrue(exception.getMessage().contains("Não é possível encerrar o processo"));
    }
    
    // Testes para iniciarProcessoRevisao

    @Test
    void iniciarProcessoRevisao_Valido_IniciaComSucesso() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setSituacao(SituacaoProcesso.CRIADO);
        processo.setTipo(TipoProcesso.REVISAO);
        processo.setDataLimite(LocalDate.now().plusDays(30));

        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setTipo(TipoUnidade.OPERACIONAL);

        Mapa mapaNovo = new Mapa();
        mapaNovo.setCodigo(200L);

        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
        when(unidadeProcessoRepo.findUnidadesInProcessosAtivos(List.of(1L))).thenReturn(Collections.emptyList());
        when(unidadeMapaRepo.findCodigosUnidadesComMapaVigente(List.of(1L))).thenReturn(List.of(1L));
        when(unidadeRepo.findById(1L)).thenReturn(Optional.of(unidade));
        when(mapaRepo.save(any(Mapa.class))).thenReturn(mapaNovo);
        when(subprocessoRepo.save(any(Subprocesso.class))).thenAnswer(inv -> inv.getArgument(0));
        when(processoRepo.save(any(Processo.class))).thenReturn(processo);
        when(processoConversor.toDTO(processo)).thenReturn(ProcessoDto.builder().build());

        processoService.iniciarProcessoRevisao(1L, List.of(1L));

        verify(processoRepo).save(argThat(p -> p.getSituacao() == SituacaoProcesso.EM_ANDAMENTO));
        verify(mapaRepo).save(any(Mapa.class));
        verify(subprocessoRepo).save(any(Subprocesso.class));
        verify(publicadorDeEventos).publishEvent(any(ProcessoIniciadoEvento.class));
    }

    @Test
    void iniciarProcessoRevisao_ProcessoNaoCriado_LancaIllegalStateException() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);

        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));

        var exception = assertThrows(
            IllegalStateException.class,
            () -> processoService.iniciarProcessoRevisao(1L, List.of(1L))
        );

        assertEquals("Apenas processos na situação 'CRIADO' podem ser iniciados.", exception.getMessage());
    }
    
    @Test
    void iniciarProcessoRevisao_SemUnidades_LancaIllegalArgumentException() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setSituacao(SituacaoProcesso.CRIADO);

        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));

        var exception = assertThrows(
            IllegalArgumentException.class,
            () -> processoService.iniciarProcessoRevisao(1L, Collections.emptyList())
        );

        assertEquals("A lista de unidades é obrigatória para iniciar o processo de revisão.", exception.getMessage());
    }
    
    @Test
    void iniciarProcessoRevisao_UnidadeSemMapaVigente_LancaErroProcesso() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setSituacao(SituacaoProcesso.CRIADO);

        Unidade unidade = new Unidade();
        unidade.setCodigo(1L);
        unidade.setSigla("UT");

        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
        when(unidadeProcessoRepo.findUnidadesInProcessosAtivos(List.of(1L))).thenReturn(Collections.emptyList());
        when(unidadeMapaRepo.findCodigosUnidadesComMapaVigente(List.of(1L))).thenReturn(Collections.emptyList());
        when(unidadeRepo.findSiglasByCodigos(List.of(1L))).thenReturn(List.of("UT"));

        var exception = assertThrows(
            ErroProcesso.class,
            () -> processoService.iniciarProcessoRevisao(1L, List.of(1L))
        );

        assertEquals("As seguintes unidades não possuem mapa vigente e não podem participar de um processo de revisão: UT", exception.getMessage());
    }
    
    @Test
    void iniciarProcessoRevisao_UnidadeNaoEncontrada_LancaErroEntidadeNaoEncontrada() {
        Processo processo = new Processo();
        processo.setCodigo(1L);
        processo.setSituacao(SituacaoProcesso.CRIADO);

        when(processoRepo.findById(1L)).thenReturn(Optional.of(processo));
        when(unidadeProcessoRepo.findUnidadesInProcessosAtivos(List.of(999L))).thenReturn(Collections.emptyList());
        when(unidadeMapaRepo.findByUnidadeCodigoIn(List.of(999L))).thenReturn(Collections.emptyList());
        when(unidadeRepo.findAllById(List.of(999L))).thenReturn(Collections.emptyList());

        var exception = assertThrows(
            ErroProcesso.class,
            () -> processoService.iniciarProcessoRevisao(1L, List.of(999L))
        );

        assertEquals("As seguintes unidades não possuem mapa vigente e não podem participar de um processo de revisão: ", exception.getMessage());
    }
}